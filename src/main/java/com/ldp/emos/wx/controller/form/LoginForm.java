package com.ldp.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 表单类：接收移动端提交的登录请求
 */
@ApiModel
@Data
public class LoginForm {

    @NotBlank(message = "临时授权不能为空")
    private String code;
}
