package com.lyf.supplychain.system.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.impl.SystemNotificationEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 系统事件消费者。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "supplychain.event.outbox", name = "dispatcher-type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "${supplychain.event.outbox.rocketmq.topic:supplychain-event-outbox-topic}",
        consumerGroup = "${supplychain.event.outbox.rocketmq.consumer-group:supplychain-system-event-consumer}",
        maxReconsumeTimes = 16
)
public class SystemRocketMqEventConsumer implements RocketMQListener<String> {

    private final SystemNotificationEventHandler notificationEventHandler;

    private final ObjectMapper objectMapper;

    public SystemRocketMqEventConsumer(SystemNotificationEventHandler notificationEventHandler, ObjectMapper objectMapper) {
        this.notificationEventHandler = notificationEventHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * 消费系统 outbox 事件。
     *
     * @param message RocketMQ 消息体
     */
    @Override
    public void onMessage(String message) {
        SysEventOutbox event = readEvent(message);
        boolean handled = notificationEventHandler.handle(event);
        if (!handled) {
            log.warn("RocketMQ 系统事件未匹配处理器，eventId={}，eventType={}", event.getEventId(), event.getEventType());
        }
    }

    private SysEventOutbox readEvent(String message) {
        try {
            return objectMapper.readValue(message, SysEventOutbox.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("RocketMQ 系统事件反序列化失败", exception);
        }
    }
}
