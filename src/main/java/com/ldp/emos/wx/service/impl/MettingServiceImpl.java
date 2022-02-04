package com.ldp.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ldp.emos.wx.db.dao.TbMeetingDao;
import com.ldp.emos.wx.db.dao.TbUserDao;
import com.ldp.emos.wx.db.pojo.TbMeeting;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.MettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Service
public class MettingServiceImpl implements MettingService {

    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    private TbUserDao userDao;

    @Value("${emos.code}")
    private String code;

    @Value("${workflow.url}")
    private String workflow;

    @Value("${emos.recieveNotify}")
    private String recieveNotify;

    //插入会议记录
    @Override
    public void insertMessting(TbMeeting entity) {
        int row = meetingDao.insertMeeting(entity);//执行语句并获取执行的条数
        if (row != 1) {
            throw new EmosException("会议添加失败");
        }
        // 开启审批工作流
        startMeetingWorkflow(entity.getUuid(), entity.getCreatorId().intValue(), entity.getDate(), entity.getStart());
    }

    //会议分页查询语句
    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> list = meetingDao.searchMyMeetingListByPage(param);//调用并执行分页语句
        String date = null;
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null;
        JSONArray array = null;
        //通过日期对会议进行分组，同日期的放在一个列表下，不同的则新建一个组列表
        for (HashMap map : list) {
            String temp = map.get("date").toString();//获取每个会议记录的日期
            if (!temp.equals(date)) {//判断日期是否相等,不相等则创建一个新的列表组进行存放信息
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date", date);//写入新组的日期
                array = new JSONArray();
                resultMap.put("list", array);//写入新组的列表
                resultList.add(resultMap);//写入要返回的结果
            }
            array.put(map);//将信息写入组中
        }
        return resultList;
    }

    //发出HTTP请求到工作流项目
    private void startMeetingWorkflow(String uuid, int creatorId, String date, String start) {
        HashMap info = userDao.searchUserInfo(creatorId);//获取用户基本信息
        //发送http请求信息
        JSONObject json = new JSONObject();
        json.set("url", recieveNotify);
        json.set("uuid", uuid);
        json.set("openId", info.get("openId"));
        json.set("code", code);
        json.set("date", date);
        json.set("start", start);
        String[] roles = info.get("roles").toString().split("，");//获取角色信息
        if (!ArrayUtil.contains(roles, "总经理")) {//通过数组查询里面是否包含总经理，取反则是判断是否是总经理
            Integer managerId = userDao.searchDeptManagerId(creatorId);//查询部员所在部门的部门经理Id
            json.set("managerId", managerId);
            Integer gmId = userDao.searchGmId();//查询总经理id
            json.set("gmId", gmId);
            boolean bool = meetingDao.searchMeetingMembersInSameDept(uuid);//查询某一个会议的参会人是不是同一个部门的
            json.set("sameDept", bool);
        }
        String url = workflow + "/workflow/startMeetingProcess";
        //发送请求
        //HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
//        if (resp.getStatus() == 200 || resp == null) {//判断返回值
//            json = JSONUtil.parseObj(resp.body());//获取json数据
//            String instanceId = json.getStr("instanceId");//获取会议id
//            String instanceId = "1285d17f13d44501a450308e4ea243fe";//获取会议id
        String instanceId = uuid;//获取会议id
        HashMap param = new HashMap();
        param.put("uuid", uuid);
        param.put("instanceId", instanceId);
        int row = meetingDao.updateMeetingInstanceId(param);//更新某一条会议记录的insertid
        if (row != 1) {
            throw new EmosException("保存会议工作流实例ID失败");
        }
//        } else {
//            throw new EmosException("程序出错");
//        }
    }

    //根据Id查询会议信息
    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = meetingDao.searchMeetingById(id);//根据会议Id查询会议记录
        ArrayList<HashMap> list = meetingDao.searchMeetingMembers(id);//根据会议Id查询会议的参会人
        map.put("members", list);//将参会人id写入会议记录中
        return map;
    }

    //修改会议记录信息
    @Override
    public void updateMeetingInfo(HashMap param) {
        //提取数据
        int id = (int) param.get("id");
        String date = param.get("date").toString();
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();

        //查询修改前的会议记录
        HashMap oldMeeting = meetingDao.searchMeetingById(id);
        String uuid = oldMeeting.get("uuid").toString();//获取修改前的uuid
        Integer creatorId = Integer.parseInt(oldMeeting.get("creatorId").toString());//获取创建会议人id

        int row = meetingDao.updateMeetingInfo(param);//更新会议记录
        if (row != 1) {
            throw new EmosException("会议更新失败");
        }

        //会议更新成功后删除以前的工作流
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被修改");
        json.set("uuid", uuid);
        json.set("code", code);
//        String url = workflow + "/workflow/deleteProcessById";
        //发送HTTP请求
//        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
//        if (resp.getStatus() != 200) {
//            log.error("删除工作流失败");
//            throw new EmosException("删除工作流失败");
//        }

        //创建新的工作流
        startMeetingWorkflow(uuid, creatorId, date, start);
    }

    //删除会议记录
    @Override
    public void deleteMeetingById(int id) {
        HashMap meeting = meetingDao.searchMeetingById(id);//查询会议信息
        String uuid = meeting.get("uuid").toString();//提取信息
        String instanceId = meeting.get("instanceId").toString();
        DateTime date = DateUtil.parseDate(meeting.get("date") + " " + meeting.get("start"));
        DateTime now = DateUtil.date();//获取系统时间
        //会议开始前20分钟，不能删除会议
        if (now.isAfterOrEquals(date.offset(DateField.MINUTE, -20))) {//往前偏移20分钟
            throw new EmosException("距离会议开始不足20分钟，不能删除会议");
        }
        int row = meetingDao.deleteMeetingById(id);//执行语句删除会议记录
        if (row != 1) {
            throw new EmosException("会议删除失败");
        }

        //删除会议工作流
        JSONObject json = new JSONObject();
        json.set("instanceId", instanceId);
        json.set("reason", "会议被取消");
        json.set("code", code);
        json.set("uuid", uuid);
//        String url = workflow + "/workflow/deleteProcessById";
//        HttpResponse resp = HttpRequest.post(url).header("content-type", "application/json").body(json.toString()).execute();
//        if (resp.getStatus() != 200) {
//            log.error("删除工作流失败");
//            throw new EmosException("删除工作流失败");
//        }
    }
}
