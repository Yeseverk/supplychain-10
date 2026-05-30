package com.lyf.supplychain.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.warehouse.config.WmsOutboundEventProperties;
import com.lyf.supplychain.warehouse.service.impl.RocketMqOutboundCompletedEventPublisher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * RocketMQ 出库完成事件发布器测试。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
class RocketMqOutboundCompletedEventPublisherTest {

    @Test
    void publishShouldSendOutboundEventToConfiguredTopic() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        WmsOutboundEventProperties properties = new WmsOutboundEventProperties();
        properties.setTopic("wms-outbound-topic");
        properties.setSendTimeoutMs(3000L);
        RocketMqOutboundCompletedEventPublisher publisher =
                new RocketMqOutboundCompletedEventPublisher(rocketMQTemplate, properties, new ObjectMapper());
        WmsOutboundCompletedEvent event = new WmsOutboundCompletedEvent();
        event.setEventId("evt-1");
        event.setOutboundNo("OUT-001");
        event.setOrderNo("SO-001");

        publisher.publish(event);

        verify(rocketMQTemplate).syncSend(eq("wms-outbound-topic"), anyString(), eq(3000L));
    }
}
