package com.lyf.supplychain.system.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.impl.SystemNotificationEventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RocketMQ 系统事件消费者测试。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@ExtendWith(MockitoExtension.class)
class SystemRocketMqEventConsumerTest {

    @Mock
    private SystemNotificationEventHandler notificationEventHandler;

    @Test
    void onMessageShouldDeserializeEventAndCallNotificationHandler() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SystemRocketMqEventConsumer consumer = new SystemRocketMqEventConsumer(notificationEventHandler, objectMapper);
        SysEventOutbox event = new SysEventOutbox();
        event.setId(1L);
        event.setEventId("event-1");
        event.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        event.setPayload("{}");
        when(notificationEventHandler.handle(org.mockito.ArgumentMatchers.any(SysEventOutbox.class))).thenReturn(true);

        consumer.onMessage(objectMapper.writeValueAsString(event));

        verify(notificationEventHandler).handle(argThat(argument -> "event-1".equals(argument.getEventId())));
    }
}
