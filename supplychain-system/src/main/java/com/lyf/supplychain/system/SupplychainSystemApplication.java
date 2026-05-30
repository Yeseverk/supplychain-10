package com.lyf.supplychain.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系统管理服务启动类。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@EnableFeignClients(basePackages = "com.lyf.supplychain")
@MapperScan("com.lyf.supplychain.system.mapper")
@SpringBootApplication(scanBasePackages = "com.lyf.supplychain")
public class SupplychainSystemApplication {

    /**
     * 启动系统管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SupplychainSystemApplication.class, args);
    }
}
