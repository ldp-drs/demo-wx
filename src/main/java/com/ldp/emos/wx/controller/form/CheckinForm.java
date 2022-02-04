package com.ldp.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 获取前端传过来的签到数据
 */
@Data
@ApiModel
public class CheckinForm {
    private String address;//详细地址

    private String country;//国家

    private String province;//省份

    private String city;//城市

    private String district;//区域

}
