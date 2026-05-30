package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物流费率实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_rate")
public class LogisticsRate extends BaseEntity {

    private Long channelId;
    private String countryCode;
    private String zone;
    private String currency;
    private BigDecimal firstWeightG;
    private BigDecimal firstWeightPrice;
    private BigDecimal extraWeightG;
    private BigDecimal extraWeightPrice;
    private BigDecimal minCharge;
    private BigDecimal fuelRate;
    private BigDecimal peakRate;
    private BigDecimal remoteAreaFee;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
}
