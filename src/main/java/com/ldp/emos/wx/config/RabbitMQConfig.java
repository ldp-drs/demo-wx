package com.ldp.emos.wx.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 同步接收消息需要创建，异步则不需要
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 连接RabbitMQ配置
     *
     * @return
     */
    @Bean
    public ConnectionFactory getFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.43.101");//RabbitMQ安装的主机IP地址
        factory.setPort(5672);//RabbitMQ端口号
        return factory;
    }



}
