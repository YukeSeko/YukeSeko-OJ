package com.wzy.judge.listener;

import com.rabbitmq.client.Channel;
import com.wzy.common.constant.RabbitMqConstant;
import com.wzy.judge.judge.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class JudgeListener {

    @Resource
    private JudgeService judgeService;

    /**
     * 指定程序监听的消息队列和确认机制
     *
     * @param message
     * @param channel
     * @param deliveryTag 重新投递次数
     */
    @SneakyThrows
    @RabbitListener(queues = {RabbitMqConstant.queue_name}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String replace = message.replace("\"", "");
        log.info("receiveMessage message = {}", replace);
        long questionSubmitId = Long.parseLong(replace);
        try {
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            //发生异常，重新入队
            if (deliveryTag <= 3) {
                channel.basicNack(deliveryTag, false, true);
            }else {
                //重新投递该消息超过三次，放弃重新投递，直接抛弃
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }

}
