package com.ldp.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * message_ref集合 判断用户是否已读等
 */
@Document(collection = "message_ref")//文档的注解：关联类的集合
@Data
public class MessageRefEntity implements Serializable {

    @Id
    private String _id;//主键

    @Indexed
    private String messageId;//message记录的id

    @Indexed
    private Integer receiverId;//接收人ID

    @Indexed
    private Boolean readFlag;//是否已读

    @Indexed
    private Boolean lastFlag;//是否为新接收的消息
}
