package com.lyf.supplychain.supplier.model;

import lombok.Builder;
import lombok.Data;

/**
 * 通知发送命令。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
@Builder
public class NotificationCommand {

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
}
