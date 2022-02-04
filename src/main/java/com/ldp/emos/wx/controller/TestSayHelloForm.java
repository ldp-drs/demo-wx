package com.ldp.emos.wx.controller;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel//出现在swagger的注解
@Data//需要get/set的注解
public class TestSayHelloForm {

//    @NotBlank//不能为空不能为空字符串
//    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$")//正则表达式验证：汉子
    @ApiModelProperty("姓名")//描述注解
    private String name;

}
