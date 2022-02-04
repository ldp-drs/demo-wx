package com.ldp.emos.wx.service;

import com.ldp.emos.wx.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface UserService {
    //添加用户方法
    int registerUser(String registerCode, String code, String nickname, String photo);

    //根据用户id获取用户的权限
    Set<String> searchUserPermissions(int userId);

    //用户登录模块
    Integer login(String code);

    TbUser searchById(int userId);

    //获取用户的入职日期
    String searchUserHiredate(int userId);

    //获取用户的概要信息
    HashMap searchUserSummary(int userId);

    //查询部门和员工的数据，并进行合并
    ArrayList<HashMap> searchUserGroupByDept(String keyword);

    //根据userId查询用户信息
    ArrayList<HashMap> searchMembers(List param);
}
