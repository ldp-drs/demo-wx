package com.ldp.emos.wx.service;

import com.ldp.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MettingService {

    //插入会议记录
    void insertMessting(TbMeeting entity);

    //会议分页查询语句
    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    //根据Id查询会议信息
    HashMap searchMeetingById(int id);

    //修改会议记录信息
    void updateMeetingInfo(HashMap param);

    //删除会议记录
    void deleteMeetingById(int id);
}
