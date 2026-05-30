package com.lyf.supplychain.supplier.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商月度评分结果。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
public class SupplierMonthlyScore {

    private boolean purchaseDataEnough;

    private BigDecimal deliveryScore = BigDecimal.ZERO;

    private BigDecimal qualityScore = BigDecimal.ZERO;

    private BigDecimal responseScore = BigDecimal.ZERO;

    private BigDecimal priceScore = BigDecimal.ZERO;

    private BigDecimal totalScore = BigDecimal.ZERO;

    private String grade = "C";
}
