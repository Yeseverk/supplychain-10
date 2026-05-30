package com.lyf.supplychain.system.service;

import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.config.SystemNotificationProperties;
import com.lyf.supplychain.system.service.impl.SystemNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统统一通知服务测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@ExtendWith(MockitoExtension.class)
class SystemNotificationServiceTest {

    @Mock
    private MessageCenterService messageCenterService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendShouldPersistMessagePushWebSocketAndSendMail() {
        SystemNotificationProperties properties = new SystemNotificationProperties();
        properties.setWebsocketEnabled(true);
        properties.setMailEnabled(true);
        properties.setDefaultFrom("no-reply@supplychain.local");
        SystemNotificationService service = new SystemNotificationServiceImpl(
                messageCenterService,
                messagingTemplate,
                Optional.of(mailSender),
                properties
        );
        when(messageCenterService.sendInternal(any(SystemMessageSendRequest.class))).thenReturn(7001L);

        Long messageId = service.send(buildRequest());

        assertThat(messageId).isEqualTo(7001L);
        verify(messageCenterService).sendInternal(any(SystemMessageSendRequest.class));
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/tenant/101/notifications"), any(Object.class));
        verify(messagingTemplate).convertAndSendToUser(org.mockito.ArgumentMatchers.eq("501"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"), any(Object.class));
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getSubject()).isEqualTo("供应商审核通过");
    }

    private SystemMessageSendRequest buildRequest() {
        SystemMessageSendRequest request = new SystemMessageSendRequest();
        request.setTenantId(101L);
        request.setReceiverId(501L);
        request.setReceiverType("USER");
        request.setReceiverKey("501");
        request.setTitle("供应商审核通过");
        request.setContent("供应商已经审核通过");
        request.setBizType("SUPPLIER_AUDIT");
        request.setBizId("1001");
        request.setPriority("HIGH");
        request.setMailTo("buyer@example.com");
        request.setMailSubject("供应商审核通过");
        request.setMailContent("供应商已经审核通过");
        return request;
    }
}
