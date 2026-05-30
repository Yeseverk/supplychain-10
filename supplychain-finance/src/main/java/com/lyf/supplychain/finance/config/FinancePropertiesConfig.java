package com.lyf.supplychain.finance.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 财务业务配置注册。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Configuration
@EnableConfigurationProperties(FinancePayableReminderProperties.class)
public class FinancePropertiesConfig {
}
