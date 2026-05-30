package com.lyf.supplychain.system.model.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知 WebSocket 推送载荷。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Builder
public class SystemNotificationPayload {

    private Long messageId;

    private Long tenantId;

    private Long receiverId;

    private String receiverType;

    private String receiverKey;

    private String title;

    private String content;

    private String bizType;

    private String bizId;

    private String priority;

    private LocalDateTime createTime;
}
