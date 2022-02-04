package com.ldp.emos.wx.service;

import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {

    //向message插入数据
    public String insertMessage(MessageEntity entity);

    //向ref集合插入数据
    public String inserRef(MessageRefEntity entity);

    //查询未读消息的数量
    public long searchUnreadCount(int userId);

    //查询接收的最新消息的数量
    public long searchLastCount(int userId);

    //查询分页数据
    public List<HashMap> searchMessageByPage(int userId, long start, int length);

    //根据id查询信息
    public HashMap searchMessageById(String id);

    //把消息的未读状态改成已读状态
    public long updateUnreadMessage(String id);

    //根据id删除message信息
    public long deleteMessageRefById(String id);

    //根据id删除ref集合消息
    public long deleteUserMessageRef(int userId);
}
