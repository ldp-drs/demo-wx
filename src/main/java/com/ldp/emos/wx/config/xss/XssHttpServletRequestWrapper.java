package com.ldp.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 预防xss攻击配置类
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    //对数据进行转义
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);//将数据里的特殊符号和标签去掉，防止变成xss攻击语句
        }
        return value;
    }

    //对数组进行转义
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StrUtil.hasEmpty(value)) {
                    value = HtmlUtil.filter(value);//将数据里的特殊符号和标签去掉，防止变成xss攻击语句
                }
                values[i] = value;
            }
        }
        return values;
    }

    //对Map数组进行转义
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                String[] values = parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (!StrUtil.hasEmpty(value)) {
                        value = HtmlUtil.filter(value);//将数据里的特殊符号和标签去掉，防止变成xss攻击语句
                    }
                    values[i] = value;
                }
                map.put(key, values);
            }
        }
        return map;
    }

    //对Header请求头数据进行转义
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);//将数据里的特殊符号和标签去掉，防止变成xss攻击语句
        }
        return value;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();//读取第一行数据
        while (line != null) {
            body.append(line);//将数据拼接到body里
            line = buffer.readLine();//读取下一行的数据
        }
        buffer.close();
        reader.close();
        in.close();
        Map<String, Object> map = JSONUtil.parseObj(body.toString());//获取body里的数据
        Map<String, Object> result = new LinkedHashMap<>();//定义新的map用来存储转义后的数据
        for (String key : map.keySet()) {
            Object val = map.get(key);//通过key获取数据
            if (val instanceof String) {//判断是否是字符串语句
                if (!StrUtil.hasEmpty(val.toString())) {//将数据转为字符串
                    result.put(key, HtmlUtil.filter(val.toString()));//将数据进行转义
                }
            } else {
                result.put(key, val);
            }
        }
        String json = JSONUtil.toJsonStr(result);//将json赋值给字符串
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());//转为io流数据
        return new ServletInputStream() {//返回一个对象
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }
}
