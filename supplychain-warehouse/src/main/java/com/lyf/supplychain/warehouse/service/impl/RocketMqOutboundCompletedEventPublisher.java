package com.lyf.supplychain.warehouse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.warehouse.config.WmsOutboundEventProperties;
import com.lyf.supplychain.warehouse.service.OutboundCompletedEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 出库完成事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Slf4j
@Component
@ConditionalOnBean(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "supplychain.warehouse.outbound-event", name = "mode", havingValue = "rocketmq")
public class RocketMqOutboundCompletedEventPublisher implements OutboundCompletedEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;
    private final WmsOutboundEventProperties properties;
    private final ObjectMapper objectMapper;

    public RocketMqOutboundCompletedEventPublisher(RocketMQTemplate rocketMQTemplate,
                                                   WmsOutboundEventProperties properties,
                                                   ObjectMapper objectMapper) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送 WMS 出库完成消息。
     *
     * @param event 出库完成事件
     */
    @Override
    public void publish(WmsOutboundCompletedEvent event) {
        String message;
        try {
            message = objectMapper.writeValueAsString(event);
        } catch (Exception exception) {
            throw new IllegalStateException("WMS出库完成事件序列化失败", exception);
        }
        rocketMQTemplate.syncSend(properties.getTopic(), message, properties.getSendTimeoutMs());
        log.info("WMS出库完成事件已发送，topic={}，eventId={}，outboundNo={}，orderNo={}",
                properties.getTopic(), event.getEventId(), event.getOutboundNo(), event.getOrderNo());
    }
}
