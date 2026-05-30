package com.lyf.supplychain.purchase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * 采购审批金额阈值配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@ConfigurationProperties(prefix = "supplychain.purchase")
public class PurchaseApprovalProperties {

    private BigDecimal approveFreeAmount = new BigDecimal("10000");

    private BigDecimal managerApproveAmount = new BigDecimal("50000");
}
