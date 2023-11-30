package com.wzy.question.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author YukeSeko
 * @Since 2023/10/26 10:54
 */
@Component
@Slf4j
public class RabbitMqConfig {
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 设置消息从生产者发送至 rabbitmq broker 成功的回调 （保证信息到达 broker）
        // ack=true:消息成功发送到Exchange
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("ConfirmCallback:     " + "相关数据：" + correlationData);
            log.info("ConfirmCallback:     " + "确认是否到达交换机：" + ack);
            log.info("ConfirmCallback:     " + "原因：" + cause);
        });
        // 设置信息从交换机发送至 queue 失败的回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("ReturnCallback:     " + "消息：" + message);
            log.info("ReturnCallback:     " + "回应码：" + replyCode);
            log.info("ReturnCallback:     " + "回应信息：" + replyText);
            log.info("ReturnCallback:     " + "交换机：" + exchange);
            log.info("ReturnCallback:     " + "路由键：" + routingKey);
        });
        // 为 true 时，消息通过交换器无法匹配到队列时会返回给生产者，为 false 时，匹配不到会直接丢弃
        rabbitTemplate.setMandatory(true);
        // 设置发送时的转换 序列化
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

}
