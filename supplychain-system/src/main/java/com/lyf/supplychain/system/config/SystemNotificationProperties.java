package com.lyf.supplychain.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统统一通知配置属性。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@ConfigurationProperties(prefix = "supplychain.notification")
public class SystemNotificationProperties {

    private Boolean mailEnabled = false;

    private Boolean websocketEnabled = true;

    private String defaultFrom = "no-reply@supplychain.local";
}
