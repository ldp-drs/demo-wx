package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysConfigDao {

    //获取签到打卡的所有时间名以及时间
    public List<SysConfig> selectAllParam();
}