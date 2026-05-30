package com.lyf.supplychain.warehouse.service;

import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;

/**
 * 出库完成事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
public interface OutboundCompletedEventPublisher {

    /**
     * 发布出库完成事件。
     *
     * @param event 出库完成事件
     */
    void publish(WmsOutboundCompletedEvent event);
}
