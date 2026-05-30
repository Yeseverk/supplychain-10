package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费预估请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class FeeEstimateRequest {

    @NotNull(message = "渠道不能为空")
    private Long channelId;
    @NotBlank(message = "目的国不能为空")
    private String countryCode;
    @NotNull(message = "实际重量不能为空")
    private BigDecimal actualWeightG;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private BigDecimal declaredValue = BigDecimal.ZERO;
}
