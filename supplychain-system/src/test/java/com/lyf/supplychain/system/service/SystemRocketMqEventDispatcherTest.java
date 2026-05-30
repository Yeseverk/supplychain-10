package com.lyf.supplychain.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.system.config.SystemEventOutboxProperties;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.impl.SystemRocketMqEventDispatcher;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * RocketMQ 事件投递器测试。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@ExtendWith(MockitoExtension.class)
class SystemRocketMqEventDispatcherTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Test
    void dispatchShouldSendOutboxEventToRocketMqTopic() {
        SystemEventOutboxProperties properties = new SystemEventOutboxProperties();
        properties.getRocketmq().setTopic("supplychain-event-test-topic");
        properties.getRocketmq().setSendTimeoutMs(3000L);
        SystemRocketMqEventDispatcher dispatcher = new SystemRocketMqEventDispatcher(
                rocketMQTemplate, properties, new ObjectMapper());
        SysEventOutbox event = new SysEventOutbox();
        event.setId(1L);
        event.setEventId("event-1");
        event.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        event.setPayload("{}");

        boolean dispatched = dispatcher.dispatch(event);

        assertThat(dispatched).isTrue();
        verify(rocketMQTemplate).syncSend(eq("supplychain-event-test-topic"), anyString(), eq(3000L));
    }
}
