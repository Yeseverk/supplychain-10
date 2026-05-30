package com.lyf.supplychain.supplier.service;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 通知服务实现测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private ReliableNotificationEventPublisher notificationEventPublisher;

    @Test
    void sendShouldPublishReliableNotificationEventToSystemService() {
        NotificationService service = new NotificationServiceImpl(notificationEventPublisher);

        service.send(NotificationCommand.builder()
                .tenantId(100L)
                .receiverId(200L)
                .receiverType("USER")
                .receiverKey("200")
                .title("供应商审核通过")
                .content("供应商已审核通过")
                .bizType("SUPPLIER_AUDIT")
                .bizId("1")
                .priority("HIGH")
                .mailTo("supplier@example.com")
                .mailSubject("供应商审核通过")
                .mailContent("供应商已审核通过")
                .build());

        ArgumentCaptor<ReliableNotificationCommand> eventCaptor = ArgumentCaptor.forClass(ReliableNotificationCommand.class);
        verify(notificationEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getSourceService()).isEqualTo(EventConstants.SourceService.SUPPLIER);
        assertThat(eventCaptor.getValue().getTenantId()).isEqualTo(100L);
        assertThat(eventCaptor.getValue().getTitle()).isEqualTo("供应商审核通过");
        assertThat(eventCaptor.getValue().getMailTo()).isEqualTo("supplier@example.com");
    }

    @Test
    void sendShouldSwallowSystemNotificationFailure() {
        NotificationService service = new NotificationServiceImpl(notificationEventPublisher);

        service.send(NotificationCommand.builder()
                .tenantId(100L)
                .receiverType("ROLE")
                .receiverKey("ROLE_PURCHASE_MANAGER")
                .title("待审核供应商提醒")
                .content("有新的供应商待审核")
                .bizType("SUPPLIER_AUDIT")
                .bizId("1")
                .build());

        verify(notificationEventPublisher).publish(org.mockito.ArgumentMatchers.any(ReliableNotificationCommand.class));
    }
}
