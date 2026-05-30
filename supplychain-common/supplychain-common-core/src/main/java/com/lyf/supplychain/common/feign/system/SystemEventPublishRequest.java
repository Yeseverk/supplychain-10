package com.lyf.supplychain.common.feign.system;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统可靠事件发布请求。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Data
public class SystemEventPublishRequest {

    private Long tenantId;

    private String eventId;

    private String eventType;

    private String sourceService;

    private String bizType;

    private String bizId;

    private String idempotentKey;

    private String payload;

    private LocalDateTime occurredTime;
}
