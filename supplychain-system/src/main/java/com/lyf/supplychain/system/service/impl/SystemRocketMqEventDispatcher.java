package com.lyf.supplychain.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.system.config.SystemEventOutboxProperties;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.SystemEventDispatcher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 系统事件投递器。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Component
@ConditionalOnBean(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "supplychain.event.outbox", name = "dispatcher-type", havingValue = "rocketmq")
public class SystemRocketMqEventDispatcher implements SystemEventDispatcher {

    private final RocketMQTemplate rocketMQTemplate;

    private final SystemEventOutboxProperties properties;

    private final ObjectMapper objectMapper;

    public SystemRocketMqEventDispatcher(RocketMQTemplate rocketMQTemplate,
                                         SystemEventOutboxProperties properties,
                                         ObjectMapper objectMapper) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 将 outbox 事件投递到 RocketMQ，由 system 消费者异步处理。
     *
     * @param event outbox 事件
     * @return true=投递到 MQ 成功
     */
    @Override
    public boolean dispatch(SysEventOutbox event) {
        try {
            rocketMQTemplate.syncSend(properties.getRocketmq().getTopic(),
                    objectMapper.writeValueAsString(event),
                    properties.getRocketmq().getSendTimeoutMs());
            return true;
        } catch (Exception exception) {
            throw new IllegalStateException("RocketMQ 事件投递失败", exception);
        }
    }
}
