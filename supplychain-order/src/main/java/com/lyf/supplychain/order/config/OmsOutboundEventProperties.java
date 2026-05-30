package com.lyf.supplychain.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OMS 出库事件消费配置。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.order.outbound-event")
public class OmsOutboundEventProperties {

    private String mode = "feign";

    private String topic = "wms-outbound-topic";

    private String consumerGroup = "supplychain-order-outbound-consumer";

    private Long idempotentTtlDays = 7L;
}
