package com.lyf.supplychain.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 财务管理服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@EnableFeignClients(basePackages = "com.lyf.supplychain")
@MapperScan("com.lyf.supplychain.finance.mapper")
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
public class SupplychainFinanceApplication {

    /**
     * 启动财务管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainFinanceApplication.class, args);
    }
}
