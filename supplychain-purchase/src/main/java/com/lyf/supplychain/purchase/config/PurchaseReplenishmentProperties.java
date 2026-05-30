package com.lyf.supplychain.purchase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 采购自动补货配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@ConfigurationProperties(prefix = "supplychain.purchase.replenishment")
public class PurchaseReplenishmentProperties {

    /**
     * 自动补货申请人ID。
     */
    private Long applyUserId = 0L;

    /**
     * 自动补货申请人名称。
     */
    private String applyUserName = "系统自动补货";

    /**
     * 默认提前期天数。
     */
    private Integer leadTimeDays = 7;

    /**
     * 默认补货倍数。
     */
    private Integer safetyStockMultiplier = 2;
}
