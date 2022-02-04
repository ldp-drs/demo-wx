package com.ldp.emos.wx.service.impl;

import com.ldp.emos.wx.db.dao.MessageDao;
import com.ldp.emos.wx.db.dao.MessageRefDao;
import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.MessageRefEntity;
import com.ldp.emos.wx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageDao messageDao;//信息集合

    @Autowired
    private MessageRefDao messageRefDao;//信息状态集合

    //insertMessage 向message插入数据
    @Override
    public String insertMessage(MessageEntity entity) {
        return messageDao.insert(entity);
    }

    //inserRef  向ref集合插入数据
    @Override
    public String inserRef(MessageRefEntity entity) {
        return messageRefDao.insert(entity);
    }

    //searchUnreadCount 查询未读消息的数量
    @Override
    public long searchUnreadCount(int userId) {
        return messageRefDao.searchUnreadCount(userId);
    }

    //searchLastCount   查询接收的最新消息的数量
    @Override
    public long searchLastCount(int userId) {
        return messageRefDao.searchLastCount(userId);
    }

    //searchMessageByPage   查询分页数据
    @Override
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        return messageDao.searchMessageByPage(userId, start, length);
    }

    //searchMessageById     根据id查询信息
    @Override
    public HashMap searchMessageById(String id) {
        return messageDao.searchMessageById(id);
    }

    //updateUnreadMessage   把消息的未读状态改成已读状态
    @Override
    public long updateUnreadMessage(String id) {
        return messageRefDao.updateUnreadMessage(id);
    }

    //deleteMessageRefById  根据id删除message信息
    @Override
    public long deleteMessageRefById(String id) {
        return messageRefDao.deleteMessageRefById(id);
    }

    //deleteUserMessageRef  根据id删除ref集合消息
    @Override
    public long deleteUserMessageRef(int userId) {
        return messageRefDao.deleteUserMessageRef(userId);
    }
}
