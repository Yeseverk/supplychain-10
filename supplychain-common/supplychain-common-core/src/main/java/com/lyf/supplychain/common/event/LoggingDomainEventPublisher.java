package com.lyf.supplychain.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 日志型领域事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@Component
@ConditionalOnMissingBean(DomainEventPublisher.class)
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    /**
     * 记录事件日志，后续可替换为 RocketMQ 发布实现。
     *
     * @param event 领域事件
     */
    @Override
    public void publish(DomainEvent event) {
        log.info("发布领域事件，eventId={}，eventType={}，sourceService={}，tenantId={}，bizId={}",
                event.getEventId(), event.getEventType(), event.getSourceService(), event.getTenantId(), event.getBizId());
    }
}
