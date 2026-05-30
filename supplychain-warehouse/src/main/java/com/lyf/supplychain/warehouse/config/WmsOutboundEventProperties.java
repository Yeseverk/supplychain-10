package com.lyf.supplychain.warehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WMS 出库事件配置。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.warehouse.outbound-event")
public class WmsOutboundEventProperties {

    private String mode = "feign";

    private String topic = "wms-outbound-topic";

    private Long sendTimeoutMs = 3000L;
}
