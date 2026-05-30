package com.lyf.supplychain.purchase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 采购服务 XXL-JOB 配置属性。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    private Boolean enabled = false;

    private Admin admin = new Admin();

    private String accessToken;

    private Executor executor = new Executor();

    @Data
    public static class Admin {

        private String addresses;
    }

    @Data
    public static class Executor {

        private String appname;

        private String address;

        private String ip;

        private Integer port = 9998;

        private String logPath = "logs/xxl-job/jobhandler";

        private Integer logRetentionDays = 30;
    }
}
