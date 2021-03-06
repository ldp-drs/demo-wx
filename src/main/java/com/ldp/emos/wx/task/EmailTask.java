package com.ldp.emos.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")//多例对象注解
public class EmailTask implements Serializable {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${emos.email.system}")
    private String mailbox;

    //发送信息
    @Async//异步执行注解
    public void sendAsync(SimpleMailMessage message) {
        message.setFrom(mailbox);//发件人
        javaMailSender.send(message);//发送邮件
    }
}
