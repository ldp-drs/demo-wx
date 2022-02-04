package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbCity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbCityDao {
    //查询城市编码
    public String searchCode(String city);
}