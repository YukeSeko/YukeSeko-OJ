package com.wzy.common.constant;

/**
 * rabbitmq 常量
 * @author YukeSeko
 * @Since 2023/10/10 15:33
 */
public interface RabbitMqConstant {

    /**
     * 消息队列交换机
     */
    String exchange = "code_exchange";

    /**
     * 消息队列路由键
     */
    String routing_key = "code_judge";

    /**
     * 消息队列名称
     */
    String queue_name = "code_queue";

    /**
     * 消息队列redis key
     */
    String redis_key = "oj:mq:";

    /**
     * 消息投递redis成功状态
     */
    String redis_deliver_success = "deliver_success";

    /**
     * 消息投递redis失败状态
     */
    String redis_deliver_fail = "deliver_failed";
}
