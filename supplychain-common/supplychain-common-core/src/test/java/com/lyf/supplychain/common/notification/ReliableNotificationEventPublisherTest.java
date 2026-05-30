package com.lyf.supplychain.common.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.feign.system.SystemEventFeignClient;
import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 可靠通知事件发布器测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@ExtendWith(MockitoExtension.class)
class ReliableNotificationEventPublisherTest {

    @Mock
    private SystemEventFeignClient systemEventFeignClient;

    @Test
    void publishShouldWrapNotificationAsReliableSystemEvent() {
        ReliableNotificationEventPublisher publisher = new ReliableNotificationEventPublisher(
                systemEventFeignClient, new ObjectMapper());
        when(systemEventFeignClient.publish(any(SystemEventPublishRequest.class))).thenReturn(R.ok(8001L));

        publisher.publish(ReliableNotificationCommand.builder()
                .tenantId(101L)
                .receiverType(EventConstants.Notification.RECEIVER_TYPE_ROLE)
                .receiverKey(EventConstants.ReceiverRole.WAREHOUSE_MANAGER)
                .title("库存预警")
                .content("SKU库存低于安全库存")
                .bizType(EventConstants.BizType.WMS_INVENTORY_WARNING)
                .bizId("sku-1001")
                .priority(EventConstants.Notification.PRIORITY_HIGH)
                .sourceService(EventConstants.SourceService.WAREHOUSE)
                .build());

        ArgumentCaptor<SystemEventPublishRequest> captor = ArgumentCaptor.forClass(SystemEventPublishRequest.class);
        verify(systemEventFeignClient).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(EventConstants.EventType.SYSTEM_NOTIFICATION);
        assertThat(captor.getValue().getSourceService()).isEqualTo(EventConstants.SourceService.WAREHOUSE);
        assertThat(captor.getValue().getPayload()).contains("库存预警", "SKU库存低于安全库存");
    }
}
