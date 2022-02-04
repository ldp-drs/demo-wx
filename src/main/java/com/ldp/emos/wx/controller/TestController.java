package com.ldp.emos.wx.controller;

import com.ldp.emos.wx.common.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")//使用该注解后该方法就会出现在swagger上
    //@Valid：获取输入的值
    //@RequestBody 转换为对象
    public R sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return R.ok().put("message", "Hello" + form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT", "USER:ADD"}, logical = Logical.OR)
    public R addUser() {
        return R.ok("用户添加成功");
    }
}
