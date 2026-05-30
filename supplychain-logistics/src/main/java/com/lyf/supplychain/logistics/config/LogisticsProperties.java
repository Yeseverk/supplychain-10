package com.lyf.supplychain.logistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 物流模块业务配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@ConfigurationProperties(prefix = "supplychain.logistics")
public class LogisticsProperties {

    /**
     * 物流轨迹 Webhook 签名密钥。
     */
    private String webhookSecret;

    /**
     * 创建运单的 Redis 分布式锁过期时间。
     */
    private Duration waybillLockTtl = Duration.ofSeconds(30);

    /**
     * 轨迹拉取配置。
     */
    private TrackPull trackPull = new TrackPull();

    /**
     * 轨迹拉取配置。
     *
     * @author liyunfei
     * @date 2026-05-25
     */
    @Data
    public static class TrackPull {

        /**
         * 单次任务最多处理的运单数量。
         */
        private Integer batchSize = 200;

        /**
         * 每个物流商并发拉取任务数。
         */
        private Integer carrierConcurrency = 4;

        /**
         * 单个轨迹拉取任务等待超时时间。
         */
        private Duration taskTimeout = Duration.ofSeconds(10);
    }
}
