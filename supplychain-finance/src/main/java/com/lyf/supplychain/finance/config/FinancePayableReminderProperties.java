package com.lyf.supplychain.finance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应付账款到期提醒配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@ConfigurationProperties(prefix = "supplychain.finance")
public class FinancePayableReminderProperties {

    private Integer payableWarningDays = 7;
}
