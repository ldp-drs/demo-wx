package com.ldp.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * token
 */
@Component
@Slf4j//日志注解
public class JwtUtil {

    @Value("${emos.jwt.secret}")//应用配置文件中的数据值
    private String secret;//密钥

    @Value("${emos.jwt.expire}")
    private int expire;//有效期

    //创建token令牌密钥
    public String createToken(int userId) {
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);//获取5天后的时间，即过期时间的日子
        Algorithm algorithm = Algorithm.HMAC256(secret);//将密钥封装成加密对象
        JWTCreator.Builder builder = JWT.create();
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);//获取token：userid+过期时间+密钥
        return token;
    }

    //通过令牌获取用户id
    public int getUserId(String token) {
        DecodedJWT jwt = JWT.decode(token);//对令牌字符串进行解码
        int userId = jwt.getClaim("userId").asInt();//获取用户id
        return userId;
    }

    //验证令牌有效性
    public void verifierToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);//创建算法对象:传入密钥
        JWTVerifier verifier = JWT.require(algorithm).build();//解密
        verifier.verify(token);//验证
    }
}
