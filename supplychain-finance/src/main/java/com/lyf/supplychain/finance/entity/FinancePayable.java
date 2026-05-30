package com.lyf.supplychain.finance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付账款实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_payable")
public class FinancePayable extends BaseEntity {

    private String payableNo;
    private String sourceType;
    private String sourceBizNo;
    private Long poId;
    private String poNo;
    private Long supplierId;
    private String supplierName;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    @TableField(exist = false)
    private BigDecimal remainingAmount;
    private String currency;
    private Integer paymentDays;
    private LocalDate dueDate;
    private Integer status;
    private Integer overdueDays;
    private String remark;
}
