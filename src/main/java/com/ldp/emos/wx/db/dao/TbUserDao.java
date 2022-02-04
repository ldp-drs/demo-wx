package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {
    public boolean haveRootUser();

    public int insert(HashMap param);

    public Integer searchIdByOpenId(String openId);

    public Set<String>searchUserPermissions(int userId);

    public TbUser searchById(int userId);

    public HashMap searchNameAndDept(int userId);

    //查询用户的入职日期
    public String searchUserHiredate(int userId);

    //查询用户的概要信息
    public HashMap searchUserSummary(int userId);

    //查询员工数据
    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    //根据userId查询用户信息
    public ArrayList<HashMap> searchMembers(List param);

    // 查询用户基本信息
    public HashMap searchUserInfo(int userId);

    //查询部员所在部门的部门经理Id
    public int searchDeptManagerId(int id);

    //查询公司总经理id
    public int searchGmId();
}