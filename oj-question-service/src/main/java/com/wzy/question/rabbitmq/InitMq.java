package com.wzy.question.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.wzy.common.constant.RabbitMqConstant;

/**
 * @author YukeSeko
 * @Since 2023/10/10 15:40
 */
public class InitMq {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("10.1.201.207");
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitMqConstant.exchange,"direct",true);

            //创建队列
            channel.queueDeclare(RabbitMqConstant.queue_name,true,false,false,null);
            channel.queueBind(RabbitMqConstant.queue_name,RabbitMqConstant.exchange,RabbitMqConstant.routing_key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
