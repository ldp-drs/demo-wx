package com.ldp.emos.wx.task;

import com.ldp.emos.wx.db.pojo.MessageEntity;
import com.ldp.emos.wx.db.pojo.MessageRefEntity;
import com.ldp.emos.wx.exception.EmosException;
import com.ldp.emos.wx.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class MessageTask {

    @Autowired
    private ConnectionFactory factory;//连接类

    @Autowired
    private MessageService messageService;//Service

    /**
     * 发送消息方法
     *
     * @param topic  消息名字
     * @param entity 消息主体
     */
    public void send(String topic, MessageEntity entity) {
        String id = messageService.insertMessage(entity);//向message插入数据记录并获取id
        try (Connection connection = factory.newConnection();//创建连接对象
             Channel channel = connection.createChannel();//创建通道
        ) {
            //连接队列（队列名，消息持久化存储，不加锁所有用户都可以访问，自动删除队列，hashmap对象）
            channel.queueDeclare(topic, true, false, false, null);
            HashMap map = new HashMap();
            map.put("messageId", id);
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();//将map数据写入MQ中
            channel.basicPublish("", topic, properties, entity.getMsg().getBytes());//发送消息
            log.debug("消息发送成功");
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("向MQ发送消息失败");
        }
    }

    /**
     * 发异步执行发送消息方法
     *
     * @param topic  消息名字
     * @param entity 消息主体
     */
    @Async//异步执行注解
    public void sendAsync(String topic, MessageEntity entity) {
        send(topic, entity);//调用上面的方法即可
    }

    /**
     * 接收消息方法
     *
     * @param topic 消息名字
     */
    public int receive(String topic) {
        int i = 0;
        try (Connection connection = factory.newConnection();//创建连接对象
             Channel channel = connection.createChannel();//创建通道
        ) {
            //连接队列（队列名，消息持久化存储，不加锁所有用户都可以访问，自动删除队列，hashmap对象）
            channel.queueDeclare(topic, true, false, false, null);
            while (true) {
                GetResponse response = channel.basicGet(topic, false);//获取并接收消息
                if (response != null) {
                    AMQP.BasicProperties properties = response.getProps();//提取绑定的数据
                    Map<String, Object> map = properties.getHeaders();//提取请求头数据
                    String messageId = map.get("messageId").toString();//获取绑定的messageId
                    byte[] body = response.getBody();//获取消息主体内容
                    String message = new String(body);//将字节数组转换成字符串
                    log.debug("从RabbitMQ接收的消息：" + message);
                    MessageRefEntity entity = new MessageRefEntity();
                    entity.setMessageId(messageId);//将数据写入实体类中
                    entity.setReceiverId(Integer.parseInt(topic));
                    entity.setReadFlag(false);//消息默认未读
                    entity.setLastFlag(true);//设置为是最新接收的消息
                    messageService.inserRef(entity);//执行语句
                    long deliveryTag = response.getEnvelope().getDeliveryTag();//声明Ack应答参数
                    channel.basicAck(deliveryTag, false);//Ack应答
                    i++;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("执行异常", e);
            throw new EmosException("从MQ接收消息失败");
        }
        return i;
    }

    /**
     * 异步执行接收消息方法
     *
     * @param topic 消息名字
     */
    @Async//异步执行注解
    public int receiveAysnc(String topic) {
        return receive(topic);
    }

    /**
     * 删除队列方法
     *
     * @param topic 队列名字
     */
    public void deleteQueue(String topic) {
        try (Connection connection = factory.newConnection();//创建连接对象
             Channel channel = connection.createChannel();//创建通道
        ) {
            channel.queueDelete(topic);//根据队列名字删除队列
            log.debug("消息队列删除成功");
        } catch (Exception e) {
            log.error("删除队列失败", e);
            throw new EmosException("删除队列失败");
        }
    }

    /**
     * 异步执行删除队列方法
     *
     * @param topic 队列名字
     */
    @Async//异步执行注解
    public void deleteQueueAsync(String topic) {
        deleteQueue(topic);
    }
}