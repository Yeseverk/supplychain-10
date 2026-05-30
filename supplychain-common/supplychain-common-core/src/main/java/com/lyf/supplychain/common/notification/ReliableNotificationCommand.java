package com.lyf.supplychain.common.notification;

import lombok.Builder;
import lombok.Data;

/**
 * 可靠通知事件发布命令。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Data
@Builder
public class ReliableNotificationCommand {

    private Long tenantId;

    private Long receiverId;

    private String receiverType;

    private String receiverKey;

    private String title;

    private String content;

    private String bizType;

    private String bizId;

    private String priority;

    private String mailTo;

    private String mailSubject;

    private String mailContent;

    private String sourceService;
}
