package com.lyf.supplychain.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运单费用记录实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("logistics_fee_record")
public class LogisticsFeeRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long waybillId;
    private String waybillNo;
    private BigDecimal baseFee;
    private BigDecimal fuelSurcharge;
    private BigDecimal peakSurcharge;
    private BigDecimal remoteFee;
    private BigDecimal oversizeFee;
    private BigDecimal insuranceFee;
    private BigDecimal otherFee;
    private BigDecimal estimatedTotal;
    private BigDecimal actualTotal;
    private String currency;
    private BigDecimal billingWeightG;
    private Long rateId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
