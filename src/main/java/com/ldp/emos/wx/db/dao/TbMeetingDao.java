package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbMeeting;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbMeetingDao {

    //插入会议记录
    public int insertMeeting(TbMeeting meeting);

    //会议分页查询语句
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    //查询某一个会议的参会人是不是同一个部门的
    public boolean searchMeetingMembersInSameDept(String uuid);

    //更新某一条会议记录的insertid
    public int updateMeetingInstanceId(HashMap map);

    //根据会议Id查询会议记录
    public HashMap searchMeetingById(int id);

    //根据会议Id查询会议的参会人
    public ArrayList<HashMap> searchMeetingMembers(int id);

    //更新会议信息
    public int updateMeetingInfo(HashMap param);

    //删除会议记录
    public int deleteMeetingById(int id);
}