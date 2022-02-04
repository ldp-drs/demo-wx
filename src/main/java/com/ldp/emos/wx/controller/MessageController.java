package com.ldp.emos.wx.controller;

import com.ldp.emos.wx.common.util.R;
import com.ldp.emos.wx.config.shiro.JwtUtil;
import com.ldp.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.ldp.emos.wx.controller.form.SearchMessageByIdForm;
import com.ldp.emos.wx.controller.form.SearchMessageByPageForm;
import com.ldp.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.ldp.emos.wx.service.MessageService;
import com.ldp.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * 信息管理控制器
 */
@RestController
@RequestMapping("/message")
@Api("消息模块网络接口")
public class MessageController {

    @Autowired
    private JwtUtil jwtUtil;//token工具类

    @Autowired
    private MessageService messageService;//业务层

    @Autowired
    private MessageTask messageTask;

    /**
     * 获取分页消息列表
     *
     * @param form
     * @param token
     * @return
     */
    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页消息列表")
    public R searchMessageByPage(@Valid @RequestBody SearchMessageByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);//获取userid
        int page = form.getPage();//获取分页数据
        int length = form.getLength();//获取分页长度
        long start = (page - 1) * length;//计算当页起始显示的数据
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);//调用Service方法执行
        return R.ok().put("result", list);
    }

    /**
     * 根据ID查询消息
     *
     * @param form
     * @return
     */
    @PostMapping("searchMessageById")
    @ApiOperation("根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form) {
        return R.ok().put("result", messageService.searchMessageById(form.getId()));
    }

    /**
     * 未读消息更新成已读消息
     *
     * @param form
     * @return
     */
    @PostMapping("/updateUnreadMessage")
    @ApiOperation("未读消息更新成已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form) {
        long rows = messageService.updateUnreadMessage(form.getId());
        System.out.println("1111:" + rows);
        return R.ok().put("result", rows == 1 ? true : false);
    }

    /**
     * 删除消息
     *
     * @param form
     * @return
     */
    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form) {
        long rows = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result", rows == 1 ? true : false);
    }

    /**
     * 刷新用户的消息
     *
     * @param token
     * @return
     */
    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户的消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        messageTask.receiveAysnc(userId + "");//异步接收消息
        long lastRows = messageService.searchLastCount(userId);//查询接收了多少条消息
        long unreadRows = messageService.searchUnreadCount(userId);//查询未读数据
        return R.ok().put("lastRows", lastRows).put("unreadRows", unreadRows);
    }
}
