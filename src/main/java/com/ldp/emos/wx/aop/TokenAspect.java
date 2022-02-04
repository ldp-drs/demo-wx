package com.ldp.emos.wx.aop;

import com.ldp.emos.wx.common.util.R;
import com.ldp.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AOP切面类，判断有没有新的token生成
 */
@Aspect//切面类注解
@Component
public class TokenAspect {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    //TODO 切点，拦截哪些方法的调用,拦截controller里面所有类的所有方法
    @Pointcut("execution(public * com.ldp.emos.wx.controller.*.*(..)))")
    public void aspect() {

    }

    //TODO 环绕事件 方法调用前的参数可以拦截，返回的值也可以拦截
    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        R r = (R) point.proceed();//方法执行结果
        String token = threadLocalToken.getToken();//获取token
        if (token != null) {//如果ThreadLocal中存在token，说明是更新的Token
            r.put("token", token);//往响应中放置Token
            threadLocalToken.clear();//清除ThreadLocal里的token
        }
        return r;
    }

}
