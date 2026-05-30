package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.config.SystemNotificationProperties;
import com.lyf.supplychain.system.model.notification.SystemNotificationPayload;
import com.lyf.supplychain.system.service.MessageCenterService;
import com.lyf.supplychain.system.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 系统统一通知服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@Service
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private static final String RECEIVER_TYPE_USER = "USER";

    private static final String RECEIVER_TYPE_ROLE = "ROLE";

    private static final String DEFAULT_BIZ_TYPE = "SYSTEM";

    private static final String DEFAULT_PRIORITY = "NORMAL";

    private static final String TENANT_TOPIC_PREFIX = "/topic/tenant/";

    private static final String TENANT_TOPIC_SUFFIX = "/notifications";

    private static final String USER_QUEUE = "/queue/notifications";

    private final MessageCenterService messageCenterService;

    private final SimpMessagingTemplate messagingTemplate;

    private final JavaMailSender mailSender;

    private final SystemNotificationProperties properties;

    public SystemNotificationServiceImpl(MessageCenterService messageCenterService,
                                         SimpMessagingTemplate messagingTemplate,
                                         Optional<JavaMailSender> mailSender,
                                         SystemNotificationProperties properties) {
        this.messageCenterService = messageCenterService;
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender.orElse(null);
        this.properties = properties;
    }

    /**
     * 发送系统通知，站内信落库成功后再进行 WebSocket 和邮件分发。
     *
     * @param request 通知发送请求
     * @return 消息ID
     */
    @Override
    public Long send(SystemMessageSendRequest request) {
        Long messageId = messageCenterService.sendInternal(request);
        pushWebSocket(request, messageId);
        sendMail(request);
        return messageId;
    }

    private void pushWebSocket(SystemMessageSendRequest request, Long messageId) {
        if (!Boolean.TRUE.equals(properties.getWebsocketEnabled())) {
            return;
        }
        try {
            Long tenantId = ObjectUtil.defaultIfNull(request.getTenantId(), 0L);
            Long receiverId = ObjectUtil.defaultIfNull(request.getReceiverId(), 0L);
            String receiverType = StrUtil.blankToDefault(request.getReceiverType(), RECEIVER_TYPE_ROLE);
            SystemNotificationPayload payload = SystemNotificationPayload.builder()
                    .messageId(messageId)
                    .tenantId(tenantId)
                    .receiverId(receiverId)
                    .receiverType(receiverType)
                    .receiverKey(request.getReceiverKey())
                    .title(StrUtil.blankToDefault(request.getTitle(), "系统通知"))
                    .content(StrUtil.blankToDefault(request.getContent(), ""))
                    .bizType(StrUtil.blankToDefault(request.getBizType(), DEFAULT_BIZ_TYPE))
                    .bizId(request.getBizId())
                    .priority(StrUtil.blankToDefault(request.getPriority(), DEFAULT_PRIORITY))
                    .createTime(LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend(TENANT_TOPIC_PREFIX + tenantId + TENANT_TOPIC_SUFFIX, payload);
            if (RECEIVER_TYPE_USER.equals(receiverType) && ObjectUtil.isNotNull(receiverId) && receiverId > 0) {
                messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), USER_QUEUE, payload);
            }
        } catch (Exception exception) {
            log.error("系统通知 WebSocket 推送失败，messageId={}，bizType={}，bizId={}",
                    messageId, request.getBizType(), request.getBizId(), exception);
        }
    }

    private void sendMail(SystemMessageSendRequest request) {
        if (!Boolean.TRUE.equals(properties.getMailEnabled())
                || mailSender == null
                || StrUtil.isBlank(request.getMailTo())) {
            return;
        }
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(properties.getDefaultFrom());
            mailMessage.setTo(request.getMailTo());
            mailMessage.setSubject(StrUtil.blankToDefault(request.getMailSubject(), request.getTitle()));
            mailMessage.setText(StrUtil.blankToDefault(request.getMailContent(), request.getContent()));
            mailSender.send(mailMessage);
        } catch (Exception exception) {
            log.error("系统通知邮件发送失败，mailTo={}，bizType={}，bizId={}",
                    request.getMailTo(), request.getBizType(), request.getBizId(), exception);
        }
    }
}
