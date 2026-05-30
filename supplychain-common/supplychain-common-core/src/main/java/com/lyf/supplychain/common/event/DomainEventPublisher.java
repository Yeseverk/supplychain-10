package com.lyf.supplychain.common.event;

/**
 * 领域事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface DomainEventPublisher {

    /**
     * 发布领域事件。
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);
}
