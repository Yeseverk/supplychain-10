package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {

    private String poNo;
    private Long reqId;
    private Long inquiryId;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private String currency;
    private BigDecimal exchangeRate;
    private Integer paymentType;
    private Integer paymentDays;
    private BigDecimal paidAmount;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDate confirmedDate;
    private LocalDate actualDeliveryDate;
    private Integer status;
    private String logisticsCompany;
    private String trackingNo;
    private String contractNo;
    private String invoiceNo;
    private String remark;
}
