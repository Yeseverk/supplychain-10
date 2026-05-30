package com.lyf.supplychain.system.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统可靠事件 outbox 配置。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Configuration
@EnableConfigurationProperties(SystemEventOutboxProperties.class)
public class SystemEventOutboxConfig {
}
