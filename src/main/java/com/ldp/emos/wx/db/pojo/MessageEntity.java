package com.ldp.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * message集合 记录消息
 */
@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {

    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;//防止重复使用

    @Indexed
    private Integer senderId;//发送者id

    //发送者头像
    private String senderPhoto = "https://ludp-wx-1304346353.cos.ap-nanjing.myqcloud.com/img/header/header/touxiang01.png";

    private String senderName;//发送者名字

    private String msg;//正文

    @Indexed
    private Date sendTime;//发送时间
}
