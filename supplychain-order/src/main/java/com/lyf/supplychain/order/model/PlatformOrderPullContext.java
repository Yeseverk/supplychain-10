package com.lyf.supplychain.order.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台订单拉取上下文。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@Builder
public class PlatformOrderPullContext {

    private String platform;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer batchSize;
}
