package com.lyf.supplychain.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品管理轻量边界服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
@EnableFeignClients(basePackages = "com.lyf.supplychain")
public class SupplychainProductApplication {

    /**
     * 启动商品管理轻量边界服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainProductApplication.class, args);
    }
}
