package com.wzy.question.rabbitmq;

import com.wzy.common.constant.RabbitMqConstant;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务目标：
 * 1、处理发送失败的消息
 * 2、处理已经投递，但是长时间未消费的消息（解决消息堆积问题）
 */
@Component
@EnableAsync
@EnableScheduling
@Slf4j
public class HandleFailedMessage {
    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private MessageProducer messageProducer;

    private static final String message_lock = "message:fail:lock";

    @Scheduled(cron = "*/30 * * * * ?")
    public void sendFailedMessage() {
        RLock lock = redissonClient.getLock(message_lock);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock) {
                Set<String> keys = redisTemplate.keys(RabbitMqConstant.redis_key.concat("*"));
                for (String key : keys) {
                    String s = redisTemplate.opsForValue().get(key);
                    if (s != null && s.equals(RabbitMqConstant.redis_deliver_fail)) {
                        //存在发送失败的消息，尝试重新向mq中发送消息
                        messageProducer.sendMessage(RabbitMqConstant.exchange, RabbitMqConstant.routing_key, key);
                        //删除redis中该键值对
                        redisTemplate.delete(key);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务:获取失败生产者发送消息redis出现bug===");
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
