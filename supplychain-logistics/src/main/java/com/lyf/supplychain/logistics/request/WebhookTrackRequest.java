package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轨迹 Webhook 请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class WebhookTrackRequest {

    @NotBlank(message = "运单号不能为空")
    private String trackingNo;
    @NotBlank(message = "原始状态不能为空")
    private String rawStatus;
    private String location;
    private String locationCountry;
    private LocalDateTime trackTime;
    private String signature;
}
