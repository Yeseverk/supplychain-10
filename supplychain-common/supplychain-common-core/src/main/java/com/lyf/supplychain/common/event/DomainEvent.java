package com.lyf.supplychain.common.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 领域事件通用模型。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@Builder
public class DomainEvent {

    private String eventId;

    private String eventType;

    private String sourceService;

    private Long tenantId;

    private String bizId;

    private String payload;

    @Builder.Default
    private LocalDateTime occurredTime = LocalDateTime.now();
}
