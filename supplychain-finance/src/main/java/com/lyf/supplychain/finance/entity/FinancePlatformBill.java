package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 平台结算账单实体。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_platform_bill")
public class FinancePlatformBill extends BaseEntity {

    private String billNo;
    private String platform;
    private String storeId;
    private String storeName;
    private String platformBillId;
    private LocalDate settlementStart;
    private LocalDate settlementEnd;
    private String currency;
    private BigDecimal totalSales;
    private BigDecimal totalRefund;
    private BigDecimal referralFee;
    private BigDecimal fbaFee;
    private BigDecimal storageFee;
    private BigDecimal advertisingFee;
    private BigDecimal otherFee;
    private BigDecimal netAmount;
    private BigDecimal cnyAmount;
    private BigDecimal exchangeRate;
    private Integer status;
    private String sourceFileUrl;
    private LocalDateTime importTime;
    private Long importUserId;
}
