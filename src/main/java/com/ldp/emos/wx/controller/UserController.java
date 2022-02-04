package com.ldp.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.ldp.emos.wx.common.util.R;
import com.ldp.emos.wx.config.shiro.JwtUtil;
import com.ldp.emos.wx.controller.form.LoginForm;
import com.ldp.emos.wx.controller.form.RegisterForm;
import com.ldp.emos.wx.controller.form.SearchMembersForm;
import com.ldp.emos.wx.controller.form.SearchUserGroupByDeptForm;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 处理移动端提交的请求
 */
@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {

    @Autowired
    private UserService userService;//调用业务层的代码，所以需要业务层的引用

    @Autowired
    private JwtUtil jwtUtil;//生成token令牌类

    @Autowired
    private RedisTemplate redisTemplate;//将token令牌写到缓存中

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;//缓存过期时间

    //用户注册方法
    @PostMapping("/register")
    @ApiOperation("注册用户")
    //@Valid 后端验证注解
    //RegisterForm 接收前端用户传来的数据
    public R register(@Valid @RequestBody RegisterForm form) {
        //执行插入语句，并获取主键值
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(id);//通过主键值生成token字符串
        Set<String> permsSet = userService.searchUserPermissions(id);//通过id获取用户的权限列表
        saveCacheToken(token, id);//TODO 往缓存中写入token
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);//返回数据（提示，token，权限列表）
    }

    //TODO 往缓存中写入token
    private void saveCacheToken(String token, int userId) {
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }

    //TODO 用户登录方法
    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form) {
        int id = userService.login(form.getCode());//传入临时字符串到业务层，然后获取id
        String token = jwtUtil.createToken(id);//通过id创建token令牌
        Set<String> permsSet = userService.searchUserPermissions(id);//通过id获取用户的权限列表
        saveCacheToken(token, id);//往缓存中写入token
        return R.ok("登录成功").put("token", token).put("permission", permsSet);
    }

    //查询用户的概要信息
    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户概要信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);//获取用户id
        HashMap map = userService.searchUserSummary(userId);//发送到sevice层获取数据
        return R.ok().put("result", map);//将获取到的数据返回
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELEC"}, logical = Logical.OR)//权限注解，{权限，关系（或||），默认是与（&&）关系}
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);
    }

    //根据userId查询用户信息
    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"}, logical = Logical.OR)//权限判断
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (!JSONUtil.isJsonArray(form.getMembers())) {//判断前端提交的数据是否是JSON数组
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);//将数据转换成list对象
        ArrayList list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }
}
