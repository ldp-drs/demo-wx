package com.ldp.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 接收前端传来要查询的id
 */
@ApiModel
@Data
public class DeleteMessageRefByIdForm {

    @NotBlank
    private String id;//接收前端传来要查询的id
}
