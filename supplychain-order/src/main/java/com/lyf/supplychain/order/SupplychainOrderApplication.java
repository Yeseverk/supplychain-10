package com.lyf.supplychain.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单管理轻量边界服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
@EnableFeignClients(basePackages = "com.lyf.supplychain.common.feign")
@MapperScan("com.lyf.supplychain.order.mapper")
public class SupplychainOrderApplication {

    /**
     * 启动订单管理轻量边界服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainOrderApplication.class, args);
    }
}
