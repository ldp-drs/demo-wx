package com.ldp.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * shiro过滤器
 */
@Component
@Scope("prototype")//使用该注解就会变成多例对象
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;//过期时间

    @Autowired
    private JwtUtil jwtUtil;//校验令牌的有效性

    @Autowired
    private RedisTemplate redisTemplate;//用于缓存token令牌数据

    //TODO 拦截请求后，用于把令牌支付串封装成令牌对象
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;//转换类型
        String token = getRequestToken(req);//获取token
        if (StrUtil.isBlank(token)) {//判断是否为空
            return null;
        }
        return new OAuth2Token(token);
    }

    //TODO 判断哪种请求不处理，哪种请求给shiro处理
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {//判断请求是否是options
            return true;//不处理，直接放行
        }
        return false;//除了options请求之外，所有请求都要被shiro处理
    }

    //TODO 上面的方法执行后拦截的请求则要被该方法进行处理，否则不处理
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;//转换类型
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");//请求类型
        resp.setCharacterEncoding("UTF-8");//字符集
        //TODO 跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        threadLocalToken.clear();//清楚threadLocal的数据
        String token = getRequestToken(req);//获取token字符串

//        int userId = jwtUtil.getUserId(token);//通过token获取userid
//        token = jwtUtil.createToken(userId);//通过userid生成新的token

        if (StrUtil.isBlank(token)) {//判断token是否为空
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效令牌");
            return false;
        }
        try {
            jwtUtil.verifierToken(token);//token验证
        } catch (TokenExpiredException e) {
            if (redisTemplate.hasKey(token)) {//判断缓存里是否还有token
                redisTemplate.delete(token);//删除老的令牌token
                int userId = jwtUtil.getUserId(token);//通过token获取userid
                token = jwtUtil.createToken(userId);//通过userid生成新的token
                //TODO 保存到redis缓存里 token,userid,过期时间,单位为天
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);//threadLocal媒介类也需要保存token
            } else {//如果没有token，则让用户重新登录
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }
        } catch (Exception e) {
            //令牌不正确
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        boolean bool = executeLogin(request, response);//间接执行realm类进行验证
        return bool;
    }

    //TODO shiro验证的时候进行判断用户没有登录或者登录失败
    //TODO 认证失败时执行
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;//转换类型
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");//请求类型
        resp.setCharacterEncoding("UTF-8");//字符集
        //TODO 跨域请求
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        try {
            resp.getWriter().print(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;//认证失败
    }

    //TODO 拦截请求和响应的方法
    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    //获取令牌字符串
    private String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");//获取token
        if (StrUtil.isBlank(token)) {//判断token是否为空
            token = request.getParameter("token");//获取token
        }
        return token;
    }
}
