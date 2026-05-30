package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 平台 Webhook 请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class WebhookRequest {

    @NotBlank(message = "平台订单号不能为空")
    private String platformOrderNo;

    @NotBlank(message = "原始报文不能为空")
    private String rawData;

    private String signature;
}
