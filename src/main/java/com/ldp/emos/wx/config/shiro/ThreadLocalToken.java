package com.ldp.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * Token媒介类
 */
@Component
public class ThreadLocalToken {

    private ThreadLocal local = new ThreadLocal();

    //写入token方法
    public void setToken(String token) {
        local.set(token);
    }

    //读取token方法
    public String getToken() {
        return (String) local.get();
    }

    //清空token方法
    public void clear() {
        local.remove();
    }
}
