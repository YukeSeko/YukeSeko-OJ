package com.wzy.question.rabbitmq;

import com.wzy.common.constant.RabbitMqConstant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author YukeSeko
 * @Since 2023/10/10 15:55
 */
@SpringBootTest
class MessageProducerTest {

    @Resource
    private MessageProducer messageProducer;
    @Test
    void sendMessage() {
        messageProducer.sendMessage(RabbitMqConstant.exchange,RabbitMqConstant.routing_key,"1234567");
    }
}