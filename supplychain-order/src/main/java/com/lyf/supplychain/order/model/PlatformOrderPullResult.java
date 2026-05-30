package com.lyf.supplychain.order.model;

import lombok.Builder;
import lombok.Data;

/**
 * 平台订单拉取结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@Builder
public class PlatformOrderPullResult {

    private String platform;

    private Integer pulledCount;

    private Integer importedCount;

    private Integer failedCount;
}
