package com.ldp.emos.wx.db.dao;

import com.ldp.emos.wx.db.pojo.MessageRefEntity;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * 信息状态集合
 */
@Repository
public class MessageRefDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * ref集合插入
     *
     * @param entity
     * @return
     */
    public String insert(MessageRefEntity entity) {
        entity = mongoTemplate.save(entity);
        return entity.get_id();
    }

    //查询未读消息的数量
    public long searchUnreadCount(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("readFlag").is(false).and("receiverId").is(userId));//添加查询的条件
        long count = mongoTemplate.count(query, MessageRefEntity.class);//执行语句并获取返回结果
        return count;
    }

    //查询接收消息的数量
    public long searchLastCount(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastFlag").is(true).and("receiverId").is(userId));//查询条件
        Update update = new Update();
        update.set("lastFlag", false);//将上面的消息设置为不是最新的消息
        //修改message_ref集合里的信息并获取被修改的数量，就相当于获取最新消息的数量
        UpdateResult result = mongoTemplate.updateMulti(query, update, "message_ref");
        long rows = result.getMatchedCount();//获取被修改的数量
        return rows;

    }

    /**
     * 修改消息状态
     *
     * @param id 消息的id
     * @return
     */
    public long updateUnreadMessage(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));//查询条件
        Update update = new Update();
        update.set("readFlag", true);//将消息设置为已读
        //修改message_ref集合里的信息并获取被修改的数量，就相当于获取最新消息的数量
        UpdateResult result = mongoTemplate.updateFirst(query, update, "message_ref");
        long rows = result.getMatchedCount();//获取被修改的数量
        return rows;
    }

    /**
     * 根据主键值删除消息记录
     *
     * @param id 消息id
     * @return
     */
    public long deleteMessageRefById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));//查询条件
        //修改message_ref集合里的信息并获取被修改的数量，就相当于获取最新消息的数量
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();//获取被修改的数量
        return rows;
    }

    //删除用户的所有消息
    public long deleteUserMessageRef(int userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverId").is(userId));//删除的条件
        //修改message_ref集合里的信息并获取被修改的数量，就相当于获取最新消息的数量
        DeleteResult result = mongoTemplate.remove(query, "message_ref");
        long rows = result.getDeletedCount();//获取被修改的数量
        return rows;
    }
}
