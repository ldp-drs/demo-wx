package com.ldp.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {
    //检测当天是否可以签到
    String validCanCheckIn(int userId, String date);

    //签到方法
    void checkin(HashMap param);

    //创建人脸模型方法
    void createFaceModel(int userId, String path);

    //查询员工当天的签到情况、员工考勤日期总数
    public HashMap searchTodayCheckin(int userId);

    //统计用户总的签到天数
    public long searchCheckinDays(int userId);

    //本周的考勤情况
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    //查询当月考勤方法
    ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
