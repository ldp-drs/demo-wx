package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbHolidaysDao {

    //判断当天是否是节假日
    public Integer searchTodayIsHolidays();

    //查询特殊节假日
    public ArrayList<String> searchHolidaysInRange(HashMap param);
}