package com.lyf.supplychain.order.model;

import lombok.Builder;
import lombok.Data;

/**
 * 平台拉取到的原始订单。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
@Builder
public class PlatformPulledOrder {

    private String platform;

    private String platformOrderNo;

    private String rawData;
}
