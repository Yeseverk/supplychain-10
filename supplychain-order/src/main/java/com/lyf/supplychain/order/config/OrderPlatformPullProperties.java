package com.lyf.supplychain.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 平台订单主动拉取配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@Component
@ConfigurationProperties(prefix = "supplychain.order.platform-pull")
public class OrderPlatformPullProperties {

    /**
     * 是否启用主动拉单任务。
     */
    private Boolean enabled = true;

    /**
     * 需要主动拉取的平台列表。
     */
    private List<String> platforms = new ArrayList<>(List.of("AMAZON", "EBAY"));

    /**
     * 每次回看分钟数，防止平台订单延迟写入导致漏单。
     */
    private Long lookbackMinutes = 10L;

    /**
     * 单个平台单次最大拉取数量。
     */
    private Integer batchSize = 50;

    /**
     * 是否启用模拟平台 API。
     */
    private Boolean mockEnabled = true;
}
