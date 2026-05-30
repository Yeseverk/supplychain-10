package com.lyf.supplychain.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购询价比价结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseInquiryCompareVO {

    private Long inquiryId;

    private String inquiryNo;

    private Long supplierId;

    private String supplierName;

    private String supplierGrade;

    private BigDecimal quoteAmount;

    private Integer deliveryDays;

    private LocalDate quoteExpireDate;

    private BigDecimal priceScore;

    private BigDecimal deliveryScore;

    private BigDecimal supplierGradeScore;

    private BigDecimal totalScore;

    private Boolean recommended;
}
