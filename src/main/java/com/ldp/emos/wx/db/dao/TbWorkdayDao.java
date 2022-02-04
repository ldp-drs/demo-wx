package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbWorkdayDao {

    //判断当天是否是工作日
    public Integer searchTodayIsWorkday();

    //查询特殊工作日
    public ArrayList<String> searchWorkdayInRange(HashMap param);
}