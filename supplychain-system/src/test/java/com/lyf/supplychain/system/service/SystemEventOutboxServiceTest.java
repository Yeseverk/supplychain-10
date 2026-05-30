package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.mapper.SysEventOutboxMapper;
import com.lyf.supplychain.system.service.impl.SystemEventOutboxServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统可靠事件服务测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@ExtendWith(MockitoExtension.class)
class SystemEventOutboxServiceTest {

    @Mock
    private SysEventOutboxMapper eventOutboxMapper;

    @Mock
    private SystemEventDispatcher eventDispatcher;

    @Test
    void publishShouldOnlyPersistPendingEventWithoutDispatchingImmediately() throws Exception {
        SystemEventOutboxService service = new SystemEventOutboxServiceImpl(
                eventOutboxMapper, eventDispatcher);
        when(eventOutboxMapper.insert(any(SysEventOutbox.class))).thenAnswer(invocation -> {
            SysEventOutbox event = invocation.getArgument(0);
            event.setId(7001L);
            return 1;
        });

        Long eventId = service.publish(buildNotificationEvent());

        assertThat(eventId).isEqualTo(7001L);
        verify(eventDispatcher, never()).dispatch(any(SysEventOutbox.class));
    }

    @Test
    void publishShouldReturnExistingEventIdWhenIdempotentKeyDuplicated() throws Exception {
        SystemEventOutboxService service = new SystemEventOutboxServiceImpl(
                eventOutboxMapper, eventDispatcher);
        when(eventOutboxMapper.insert(any(SysEventOutbox.class))).thenThrow(new DuplicateKeyException("duplicated"));
        SysEventOutbox existing = new SysEventOutbox();
        existing.setId(7002L);
        when(eventOutboxMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        Long eventId = service.publish(buildNotificationEvent());

        assertThat(eventId).isEqualTo(7002L);
        verify(eventDispatcher, never()).dispatch(any(SysEventOutbox.class));
    }

    @Test
    void retryFailedShouldDispatchFailedEventsAgain() throws Exception {
        SystemEventOutboxService service = new SystemEventOutboxServiceImpl(
                eventOutboxMapper, eventDispatcher);
        SysEventOutbox event = new SysEventOutbox();
        event.setId(7003L);
        event.setTenantId(101L);
        event.setEventId("event-retry-1");
        event.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        event.setBizType("SUPPLIER_RISK");
        event.setBizId("risk-2");
        event.setPayload(buildNotificationEvent().getPayload());
        event.setStatus(EventConstants.Status.FAILED);
        event.setRetryCount(1);
        when(eventOutboxMapper.selectList(any(Wrapper.class))).thenReturn(List.of(event));
        when(eventOutboxMapper.update(any(SysEventOutbox.class), any(Wrapper.class))).thenReturn(1);
        when(eventDispatcher.dispatch(any(SysEventOutbox.class))).thenReturn(true);

        int count = service.retryFailed(5, 10);

        assertThat(count).isEqualTo(1);
        verify(eventDispatcher).dispatch(any(SysEventOutbox.class));
    }

    @Test
    void dispatchPendingShouldScanPendingEventsAndDispatchByAdapter() throws Exception {
        SystemEventOutboxService service = new SystemEventOutboxServiceImpl(
                eventOutboxMapper, eventDispatcher);
        SysEventOutbox event = new SysEventOutbox();
        event.setId(7004L);
        event.setEventId("event-pending-1");
        event.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        event.setPayload(buildNotificationEvent().getPayload());
        event.setStatus(EventConstants.Status.PENDING);
        when(eventOutboxMapper.selectList(any(Wrapper.class))).thenReturn(List.of(event));
        when(eventDispatcher.dispatch(any(SysEventOutbox.class))).thenReturn(true);

        int count = service.dispatchPending(20);

        assertThat(count).isEqualTo(1);
        verify(eventDispatcher).dispatch(event);
    }

    private SystemEventPublishRequest buildNotificationEvent() throws Exception {
        SystemMessageSendRequest message = new SystemMessageSendRequest();
        message.setTenantId(101L);
        message.setReceiverType("ROLE");
        message.setReceiverKey("ROLE_PURCHASE_MANAGER");
        message.setTitle("供应商风险提醒");
        message.setContent("分类下健康供应商数量不足");
        message.setBizType("SUPPLIER_RISK");
        message.setBizId("risk-1");
        message.setPriority("HIGH");
        message.setMailTo("buyer@example.com");

        SystemEventPublishRequest request = new SystemEventPublishRequest();
        request.setTenantId(101L);
        request.setEventId("event-risk-1");
        request.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
        request.setSourceService(EventConstants.SourceService.SUPPLIER);
        request.setBizType("SUPPLIER_RISK");
        request.setBizId("risk-1");
        request.setIdempotentKey("SUPPLIER_RISK:risk-1");
        request.setPayload(new ObjectMapper().writeValueAsString(message));
        request.setOccurredTime(LocalDateTime.of(2026, 5, 21, 9, 0));
        return request;
    }
}
