package com.lyf.supplychain.purchase.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购询价综合评分结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseInquiryScore {

    private BigDecimal priceScore;

    private BigDecimal deliveryScore;

    private BigDecimal supplierGradeScore;

    private BigDecimal totalScore;
}
