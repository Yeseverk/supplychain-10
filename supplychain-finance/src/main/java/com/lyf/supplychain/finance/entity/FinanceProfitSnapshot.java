package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SKU 利润快照实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@TableName("finance_profit_snapshot")
public class FinanceProfitSnapshot {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Integer snapshotType;
    private LocalDate snapshotDate;
    private Long skuId;
    private String skuCode;
    private String platform;
    private String storeId;
    private String countryCode;
    private String currency;
    private BigDecimal exchangeRate;
    private Integer orderCount;
    private Integer salesQty;
    private BigDecimal grossRevenue;
    private BigDecimal grossRevenueCny;
    private BigDecimal purchaseCost;
    private BigDecimal logisticsFee;
    private BigDecimal platformFee;
    private BigDecimal fbaStorageFee;
    private BigDecimal advertisingFee;
    private BigDecimal refundLoss;
    private BigDecimal vatFee;
    private BigDecimal otherCost;
    private BigDecimal totalCost;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;
    private BigDecimal grossMargin;
    private BigDecimal netMargin;
    private LocalDateTime createTime;
}
