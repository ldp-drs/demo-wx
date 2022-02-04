package com.ldp.emos.wx.common.util;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义封装返回类型
 */
public class R extends HashMap<String, Object> {

    /**
     * 默认构造器
     */
    public R() {
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }

    /**
     * 可以循环调用的put方法
     */
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 静态ok方法
     */
    public static R ok() {
        return new R();
    }

    /**
     * 重载方法
     */
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    /**
     * 重载方法
     */
    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    /**
     * 静态error方法
     */
    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    /**
     * 重载方法
     */
    public static R error(String msg) {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }

    /**
     * 重载方法
     */
    public static R error() {
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }
}
