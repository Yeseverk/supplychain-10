package com.lyf.supplychain.purchase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 采购管理服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@EnableFeignClients(basePackages = "com.lyf.supplychain")
@MapperScan("com.lyf.supplychain.purchase.mapper")
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
public class SupplychainPurchaseApplication {

    /**
     * 启动采购管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainPurchaseApplication.class, args);
    }
}
