package com.ldp.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 接收前端需要进行分页的数据
 */
@ApiModel
@Data
public class SearchMessageByPageForm {

    @NotNull
    @Min(1)
    private Integer page;//页数

    @NotNull
    @Range(min = 1, max = 40)
    private Integer length;//显示长度
}
