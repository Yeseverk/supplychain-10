package com.lyf.supplychain.logistics;

import com.lyf.supplychain.logistics.config.LogisticsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 物流管理服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
@EnableFeignClients(basePackages = "com.lyf.supplychain.common.feign")
@EnableConfigurationProperties(LogisticsProperties.class)
public class SupplychainLogisticsApplication {

    /**
     * 启动物流管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainLogisticsApplication.class, args);
    }
}
