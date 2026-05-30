package com.lyf.supplychain.system.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.SystemNotificationService;
import org.springframework.stereotype.Component;

/**
 * 系统通知事件处理器。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Component
public class SystemNotificationEventHandler {

    private final SystemNotificationService notificationService;

    private final ObjectMapper objectMapper;

    public SystemNotificationEventHandler(SystemNotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理系统通知事件。
     *
     * @param event outbox 事件
     * @return true=已处理通知事件，false=非通知事件暂不处理
     */
    public boolean handle(SysEventOutbox event) {
        if (!EventConstants.EventType.SYSTEM_NOTIFICATION.equals(event.getEventType())) {
            return false;
        }
        notificationService.send(readMessageRequest(event));
        return true;
    }

    private SystemMessageSendRequest readMessageRequest(SysEventOutbox event) {
        try {
            return objectMapper.readValue(event.getPayload(), SystemMessageSendRequest.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("系统通知事件载荷解析失败", exception);
        }
    }
}
