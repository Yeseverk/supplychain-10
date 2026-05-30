package com.lyf.supplychain.supplier.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商月度绩效原始指标。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
public class SupplierPerformanceMetrics {

    private Integer totalOrders = 0;

    private Integer deliveredOnTime = 0;

    private Integer qualityPassed = 0;

    private Integer qualityTotal = 0;

    private BigDecimal responseHoursAvg;

    private BigDecimal priceComparison;
}
