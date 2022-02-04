package com.ldp.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.ldp.emos.wx.common.util.R;
import com.ldp.emos.wx.config.shiro.JwtUtil;
import com.ldp.emos.wx.controller.form.*;
import com.ldp.emos.wx.db.pojo.TbMeeting;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.MettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/meeting")
@Api("会议模块网络接口")
public class MeetingController {

    @Autowired
    private JwtUtil jwtUtil;//token

    @Autowired
    private MettingService meetingService;

    /**
     * 查询会议列表分页数据
     *
     * @param form
     * @return
     */
    @PostMapping("/searchMyMeetingListByPage")
    @ApiOperation("查询会议列表分页数据")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        int page = form.getPage();//页数
        int length = form.getLength();//长度
        long start = (page - 1) * length;
        HashMap map = new HashMap();//写入要传递的数据信息
        map.put("userId", userId);
        map.put("start", start);
        map.put("length", length);
        ArrayList list = meetingService.searchMyMeetingListByPage(map);//将数据传递过去执行，并获取结果
        return R.ok().put("result", list);//返回结果给前端
    }

    @PostMapping("/insertMeeting")
    @ApiOperation("添加会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT"}, logical = Logical.OR)
    public R insertMeeting(@Valid @RequestBody InsertMeetingForm form, @RequestHeader("token") String token) {
        if (form.getType() == 2 && (form.getPlace() == null || form.getPlace() == "")) {//判断会议类型并判断是否为空
            throw new EmosException("线下会议地点不能为空");
        }
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");//开始时间(小时分钟秒)
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");//结束时间(小时分钟秒)
        if (d2.isBeforeOrEquals(d1)) {//比较时间大小 判断结束时间是否大于开始时间
            throw new EmosException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray(form.getMembers())) {//判断JSON数据是否是数组格式
            throw new EmosException("members不是JSON数组");
        }
        //填写会议记录信息
        TbMeeting entity = new TbMeeting();
        entity.setUuid(UUID.randomUUID().toString(true));
        entity.setTitle(form.getTitle());
        entity.setCreatorId((long) jwtUtil.getUserId(token));
        entity.setDate(form.getDate());
        entity.setPlace(form.getPlace());
        entity.setStart(form.getStart() + ":00");
        entity.setEnd(form.getEnd() + ":00");
        entity.setType((short) form.getType());
        entity.setMembers(form.getMembers());
        entity.setDesc(form.getDesc());
        entity.setStatus((short) 3);
        meetingService.insertMessting(entity);//插入会议记录
        return R.ok().put("result", "success");
    }

    @PostMapping("/searchMeetingById")
    @ApiOperation("根据id查询会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:SELECT"}, logical = Logical.OR)
    public R searchMeetingById(@Valid @RequestBody SearchMeetingByIdFrom from, @RequestHeader("token") String token) {
        HashMap map = meetingService.searchMeetingById(from.getId());//根据Id查询会议信息
        return R.ok().put("result", map);
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R updateMeetingInfo(@Valid @RequestBody UpdateMeetingInfoForm form) {
        if (form.getType() == 2 && (form.getPlace() == null || form.getPlace() == "")) {
            throw new EmosException("线下会议地点不能为空");
        }
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.isBeforeOrEquals(d1)) {
            throw new EmosException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("members不是JSON数组");
        }
        HashMap param = new HashMap();
        param.put("title", form.getTitle());
        param.put("date", form.getDate());
        param.put("place", form.getPlace());
        param.put("start", form.getStart());
        param.put("end", form.getEnd());
        param.put("type", form.getType());
        param.put("members", form.getMembers());
        param.put("desc", form.getDesc());
        param.put("id", form.getId());
        param.put("instanceId", form.getInstanceId());
        param.put("status", 1);
        meetingService.updateMeetingInfo(param);
        return R.ok().put("result", "success");
    }

    @PostMapping("deleteMeetingById")
    @ApiOperation("根据ID删除会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:DELETE"}, logical = Logical.OR)
    public R deleteMeetingById(@Valid @RequestBody DeleteMeetingByIdForm form) {
        meetingService.deleteMeetingById(form.getId());
        return R.ok().put("result", "success");
    }
}
