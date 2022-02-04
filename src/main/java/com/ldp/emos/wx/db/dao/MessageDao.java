package com.ldp.emos.wx.db.dao;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.MessageRefEntity;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 信息集合
 */
@Repository
public class MessageDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    //MongoDB插入数据
    public String insert(MessageEntity entity) {
        Date sendTime = entity.getSendTime();//获取发送时间
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);//偏移8个小时
        entity.setSendTime(sendTime);//将时间再次写入
        entity = mongoTemplate.save(entity);//将实体类保存并赋值给message记录类
        return entity.get_id();//返回id
    }

    /**
     * MongoDB分页查询
     *
     * @param userId
     * @param start  起始页
     * @param length 多少条
     * @return
     */
    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        JSONObject json = new JSONObject();
        json.set("$toString", "$_id");//将_id值使用toString方法转换成字符串值
        Aggregation aggregation = Aggregation.newAggregation(//构建一个特殊的对象，用来专门做分页查询的
                Aggregation.addFields().addField("id").withValue(json).build(),//声明一个对象
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),//给两个集合创建连接
                Aggregation.match(Criteria.where("ref.receiverId").is(userId)),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),//降序排序
                Aggregation.skip(start),//分页
                Aggregation.limit(length)//长度
        );
        //获取结果
        AggregationResults<HashMap> results = mongoTemplate.aggregate(aggregation, "message", HashMap.class);
        List<HashMap> list = results.getMappedResults();//提取数据
        list.forEach(one -> {
            List<MessageRefEntity> refList = (List<MessageRefEntity>) one.get("ref");
            MessageRefEntity entity = refList.get(0);//取出数据
            Boolean readFlag = entity.getReadFlag();//判断已读还是未读
            String refId = entity.get_id();//获取信息的id
            one.put("readFlag", readFlag);//写入数据
            one.put("refId", refId);
            one.remove("ref");//删除引用字段
            one.remove("_id");
            Date sendTime = (Date) one.get("sendTime");//取出时间
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);//时间偏移，将时间恢复到北京时间
            String today = DateUtil.today();
            if (today.equals(DateUtil.date(sendTime).toDateStr())) {//判断两个日期是否相等，相等的话则只显示时间，不显示日期
                one.put("sendTime", DateUtil.format(sendTime, "HH:mm"));
            } else {
                one.put("sendTime", DateUtil.format(sendTime, "yyyy/MM/dd"));
            }
        });
        return list;
    }

    //根据id查询数据
    public HashMap searchMessageById(String id) {
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");//
        Date sendTime = (Date) map.get("sendTime");//取出时间
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);//时间偏移，将时间恢复到北京时间
        map.replace("sendTime", DateUtil.format(sendTime, "yyyy-MM-dd HH:mm"));
        return map;
    }
}
