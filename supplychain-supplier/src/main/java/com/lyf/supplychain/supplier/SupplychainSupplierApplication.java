package com.lyf.supplychain.supplier;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 供应商管理服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@EnableFeignClients(basePackages = "com.lyf.supplychain")
@EnableAsync
@MapperScan("com.lyf.supplychain.supplier.mapper")
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
public class SupplychainSupplierApplication {

    /**
     * 启动供应商管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainSupplierApplication.class, args);
    }
}
