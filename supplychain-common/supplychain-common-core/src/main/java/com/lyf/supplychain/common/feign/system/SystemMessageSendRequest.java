package com.lyf.supplychain.common.feign.system;

import lombok.Data;

/**
 * 系统站内信发送请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class SystemMessageSendRequest {

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
