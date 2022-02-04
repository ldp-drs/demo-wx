package com.ldp.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 结束前端传来的信息
 */
@ApiModel
@Data
public class UpdateUnreadMessageForm {

    @NotBlank
    private String id;
}
