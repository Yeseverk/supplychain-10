package com.lyf.supplychain.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API 网关启动类。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@SpringBootApplication
public class SupplychainGatewayApplication {

    /**
     * 启动 API 网关服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainGatewayApplication.class, args);
    }
}
