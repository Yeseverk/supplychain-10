package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流渠道保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class ChannelRequest {

    @NotNull(message = "物流商不能为空")
    private Long carrierId;
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;
    @NotBlank(message = "渠道名称不能为空")
    private String channelName;
    @NotNull(message = "渠道类型不能为空")
    private Integer channelType;
    @NotBlank(message = "适用国家不能为空")
    private String countryCodes;
    private BigDecimal minWeightG = BigDecimal.ZERO;
    @NotNull(message = "最大重量不能为空")
    private BigDecimal maxWeightG;
    private Integer maxLengthMm;
    private Integer maxGirthMm;
    private Integer allowBattery = 0;
    private Integer allowLiquid = 0;
    private Integer allowPowder = 0;
    private Integer allowFood = 1;
    @NotNull(message = "最短时效不能为空")
    private BigDecimal minDays;
    @NotNull(message = "最长时效不能为空")
    private BigDecimal maxDays;
    private Integer volumeFactor = 5000;
    private BigDecimal declaredValueLimit;
    private Integer sortOrder = 0;
    private String remark;
}
