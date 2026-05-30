package com.lyf.supplychain.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统服务 XXL-JOB 执行器配置属性。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    private Boolean enabled = false;

    private Admin admin = new Admin();

    private String accessToken;

    private Executor executor = new Executor();

    /**
     * XXL-JOB Admin 配置。
     *
     * @author liyunfei
     * @date 2026-05-20
     */
    @Data
    public static class Admin {

        private String addresses;
    }

    /**
     * XXL-JOB 执行器配置。
     *
     * @author liyunfei
     * @date 2026-05-20
     */
    @Data
    public static class Executor {

        private String appname;

        private String address;

        private String ip;

        private Integer port = 9998;

        private String logPath = "logs/xxl-job/system";

        private Integer logRetentionDays = 30;
    }
}
