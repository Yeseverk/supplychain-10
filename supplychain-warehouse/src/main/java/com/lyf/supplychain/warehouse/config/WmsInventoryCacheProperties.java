package com.lyf.supplychain.warehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WMS 库存缓存配置。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.warehouse.inventory-cache")
public class WmsInventoryCacheProperties {

    private Boolean enabled = true;

    private Long ttlMinutes = 60L;

    private Long emptyTtlMinutes = 5L;

    private Long randomSeconds = 300L;

    private Long lockSeconds = 10L;
}
