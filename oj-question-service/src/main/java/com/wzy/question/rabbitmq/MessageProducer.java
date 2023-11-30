package com.wzy.question.rabbitmq;

import com.wzy.common.constant.RabbitMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author YukeSeko
 * @Since 2023/10/10 15:06
 */
@Slf4j
@Component
public class MessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 通用发送消息类
     *
     * @param exchange
     * @param routingKey
     * @param message
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
//        rabbitTemplate.setConfirmCallback((correlationData, b, s) -> {
//            if (b) {
//                log.info("消息成功发送");
//                //修改redis中消息状态
//                redisTemplate.opsForValue().set(RabbitMqConstant.redis_key +message,RabbitMqConstant.redis_deliver_success);
//            } else {
//                log.info("消息发送失败：" + correlationData + ", 出现异常：" + s);
//                redisTemplate.opsForValue().set(RabbitMqConstant.redis_key +message,RabbitMqConstant.redis_deliver_fail);
//            }
//        });
    }
}
