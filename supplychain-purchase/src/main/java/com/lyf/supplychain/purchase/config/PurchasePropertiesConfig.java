package com.lyf.supplychain.purchase.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 采购业务配置注册。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Configuration
@EnableConfigurationProperties({PurchaseReplenishmentProperties.class, PurchaseApprovalProperties.class})
public class PurchasePropertiesConfig {
}
