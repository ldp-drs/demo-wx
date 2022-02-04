package com.ldp.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * token令牌封装类
 */
public class OAuth2Token implements AuthenticationToken {

    private String token;

    //构造器
    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
