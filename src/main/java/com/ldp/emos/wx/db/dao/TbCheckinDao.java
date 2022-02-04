package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {

    //判断当天是否已经签到
    public Integer haveCheckin(HashMap param);

    //保存签到记录 插入
    public void insert(TbCheckin entity);

    //查询员工签到情况、员工考勤日期总数
    public HashMap searchTodayCheckin(int userId);

    //统计用户总的签到天数
    public long searchCheckinDays(int userId);

    //本周的考勤情况
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

}