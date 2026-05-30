package com.lyf.supplychain.supplier.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.supplier.constant.NotificationConstants;
import com.lyf.supplychain.supplier.model.NotificationCommand;
import com.lyf.supplychain.supplier.service.NotificationService;
import org.springframework.stereotype.Service;

/**
 * 通知发送服务实现。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final ReliableNotificationEventPublisher notificationEventPublisher;

    public NotificationServiceImpl(ReliableNotificationEventPublisher notificationEventPublisher) {
        this.notificationEventPublisher = notificationEventPublisher;
    }

    /**
     * 将通知转换为可靠事件，由 system 统一事件入口落库后分发站内信、WebSocket 和邮件。
     *
     * @param command 通知发送命令
     */
    @Override
    public void send(NotificationCommand command) {
        if (command == null) {
            return;
        }
        SystemMessageSendRequest request = buildMessageRequest(command);
        notificationEventPublisher.publish(ReliableNotificationCommand.builder()
                .tenantId(request.getTenantId())
                .receiverId(request.getReceiverId())
                .receiverType(request.getReceiverType())
                .receiverKey(request.getReceiverKey())
                .title(request.getTitle())
                .content(request.getContent())
                .bizType(request.getBizType())
                .bizId(request.getBizId())
                .priority(request.getPriority())
                .mailTo(request.getMailTo())
                .mailSubject(request.getMailSubject())
                .mailContent(request.getMailContent())
                .sourceService(EventConstants.SourceService.SUPPLIER)
                .build());
    }

    /**
     * 构建系统消息请求，保留原有站内信和邮件字段。
     *
     * @param command 通知发送命令
     * @return 系统消息请求
     */
    private SystemMessageSendRequest buildMessageRequest(NotificationCommand command) {
        SystemMessageSendRequest request = new SystemMessageSendRequest();
        request.setTenantId(ObjectUtil.defaultIfNull(command.getTenantId(), 0L));
        request.setReceiverId(ObjectUtil.defaultIfNull(command.getReceiverId(), 0L));
        request.setReceiverType(StrUtil.blankToDefault(command.getReceiverType(), NotificationConstants.RECEIVER_TYPE_ROLE));
        request.setReceiverKey(command.getReceiverKey());
        request.setTitle(StrUtil.blankToDefault(command.getTitle(), "系统通知"));
        request.setContent(StrUtil.blankToDefault(command.getContent(), ""));
        request.setBizType(StrUtil.blankToDefault(command.getBizType(), "SYSTEM"));
        request.setBizId(command.getBizId());
        request.setPriority(StrUtil.blankToDefault(command.getPriority(), NotificationConstants.PRIORITY_NORMAL));
        request.setMailTo(command.getMailTo());
        request.setMailSubject(command.getMailSubject());
        request.setMailContent(command.getMailContent());
        return request;
    }
}
