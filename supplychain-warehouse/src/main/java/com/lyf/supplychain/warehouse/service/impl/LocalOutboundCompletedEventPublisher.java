package com.lyf.supplychain.warehouse.service.impl;

import com.lyf.supplychain.common.event.WmsOutboundCompletedEvent;
import com.lyf.supplychain.warehouse.service.OutboundCompletedEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Local Spring event publisher for outbound completed events.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "supplychain.warehouse.outbound-event", name = "mode", havingValue = "local")
public class LocalOutboundCompletedEventPublisher implements OutboundCompletedEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public LocalOutboundCompletedEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(WmsOutboundCompletedEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.info("Published local WMS outbound completed event, eventId={}, outboundNo={}, orderNo={}",
                event.getEventId(), event.getOutboundNo(), event.getOrderNo());
    }
}
