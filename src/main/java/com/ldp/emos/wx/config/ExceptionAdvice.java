package com.ldp.emos.wx.config;

import com.ldp.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 捕获全局异常类，并进行精简处理响应给前端
 */
@Slf4j//日志注解
@RestControllerAdvice//捕获全局异常类注解
public class ExceptionAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String validExceptionHandler(Exception e) {
        log.error("执行异常", e);
        if (e instanceof MethodArgumentNotValidException) {//后端验证失败异常
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            //将错误信息返回给前台
            return exception.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof EmosException) {//Emos异常
            EmosException exception = (EmosException) e;
            return exception.getMsg();
        } else if (e instanceof UnauthorizedException) {//未授权异常
            return "你不具备有相关权限";
        } else {//普通异常
            return "后端执行异常";
        }
    }
}
