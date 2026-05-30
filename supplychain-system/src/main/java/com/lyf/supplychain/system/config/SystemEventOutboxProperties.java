package com.lyf.supplychain.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统可靠事件 outbox 配置属性。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Data
@ConfigurationProperties(prefix = "supplychain.event.outbox")
public class SystemEventOutboxProperties {

    private Integer dispatchBatchSize = 100;

    private Integer retryBatchSize = 100;

    private Integer maxRetryCount = 5;

    private String dispatcherType = "local";

    private Rocketmq rocketmq = new Rocketmq();

    /**
     * RocketMQ 投递配置。
     *
     * @author liyunfei
     * @date 2026-05-24
     */
    @Data
    public static class Rocketmq {

        private String topic = "supplychain-event-outbox-topic";

        private String consumerGroup = "supplychain-system-event-consumer";

        private Long sendTimeoutMs = 3000L;
    }
}
