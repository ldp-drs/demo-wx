package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbDeptDao {

    //查询部门数据
    public ArrayList<HashMap> searchDeptMembers(String keyword);
}