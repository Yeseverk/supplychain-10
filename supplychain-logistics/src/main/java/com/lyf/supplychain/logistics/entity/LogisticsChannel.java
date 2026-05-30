package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物流渠道实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_channel")
public class LogisticsChannel extends BaseEntity {

    private Long carrierId;
    private String channelCode;
    private String channelName;
    private Integer channelType;
    private String countryCodes;
    private BigDecimal minWeightG;
    private BigDecimal maxWeightG;
    private Integer maxLengthMm;
    private Integer maxGirthMm;
    private Integer allowBattery;
    private Integer allowLiquid;
    private Integer allowPowder;
    private Integer allowFood;
    private BigDecimal minDays;
    private BigDecimal maxDays;
    private Integer volumeFactor;
    private BigDecimal declaredValueLimit;
    private Integer status;
    private Integer sortOrder;
    private String remark;
}
