package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道推荐请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class RecommendRequest {

    private Long orderId;
    @NotBlank(message = "目的国不能为空")
    private String countryCode;
    @NotNull(message = "实际重量不能为空")
    private BigDecimal actualWeightG;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Boolean hasBattery = false;
    private Boolean hasLiquid = false;
    private Boolean hasPowder = false;
    private BigDecimal declaredValue = BigDecimal.ZERO;
    private String declaredCurrency = "USD";
    private Integer maxDaysRequired;
}
