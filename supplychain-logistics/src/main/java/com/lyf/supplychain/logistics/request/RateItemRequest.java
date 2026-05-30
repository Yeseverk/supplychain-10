package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物流费率明细请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class RateItemRequest {

    @NotBlank(message = "国家不能为空")
    private String countryCode;
    private String zone;
    private String currency = "CNY";
    @NotNull(message = "首重不能为空")
    private BigDecimal firstWeightG;
    @NotNull(message = "首重价格不能为空")
    private BigDecimal firstWeightPrice;
    private BigDecimal extraWeightG = BigDecimal.valueOf(500);
    @NotNull(message = "续重价格不能为空")
    private BigDecimal extraWeightPrice;
    private BigDecimal minCharge = BigDecimal.ZERO;
    private BigDecimal fuelRate = BigDecimal.ZERO;
    private BigDecimal peakRate = BigDecimal.ZERO;
    private BigDecimal remoteAreaFee = BigDecimal.ZERO;
    private LocalDate effectiveDate = LocalDate.now();
    private LocalDate expireDate;
}
