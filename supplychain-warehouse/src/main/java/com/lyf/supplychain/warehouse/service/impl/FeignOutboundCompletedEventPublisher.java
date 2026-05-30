package com.lyf.supplychain.warehouse.service.impl;

import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.common.feign.order.OrderFeignClient;
import com.lyf.supplychain.common.feign.order.OrderOutboundCallbackRequest;
import com.lyf.supplychain.warehouse.service.OutboundCompletedEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Feign 出库完成事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Component
@ConditionalOnProperty(prefix = "supplychain.warehouse.outbound-event", name = "mode",
        havingValue = "feign", matchIfMissing = true)
public class FeignOutboundCompletedEventPublisher implements OutboundCompletedEventPublisher {

    private final OrderFeignClient orderFeignClient;

    public FeignOutboundCompletedEventPublisher(OrderFeignClient orderFeignClient) {
        this.orderFeignClient = orderFeignClient;
    }

    /**
     * 使用 Feign 同步通知 OMS，作为未启用 MQ 时的本地开发兜底。
     *
     * @param event 出库完成事件
     */
    @Override
    public void publish(WmsOutboundCompletedEvent event) {
        OrderOutboundCallbackRequest callback = new OrderOutboundCallbackRequest();
        callback.setOutboundId(event.getOutboundId());
        callback.setOutboundNo(event.getOutboundNo());
        callback.setOrderId(event.getOrderId());
        callback.setOrderNo(event.getOrderNo());
        callback.setOutboundDate(event.getOutboundDate());
        orderFeignClient.outboundCallback(callback);
    }
}
