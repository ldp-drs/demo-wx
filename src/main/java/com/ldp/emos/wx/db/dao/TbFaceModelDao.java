package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    //获取用户人脸模型
    public String searchFaceModel(int userId);

    //插入用户人脸模型数据
    public void insert(TbFaceModel faceModelEntity);

    //删除用户人脸模型
    public int deleteFaceModel(int userId);
}