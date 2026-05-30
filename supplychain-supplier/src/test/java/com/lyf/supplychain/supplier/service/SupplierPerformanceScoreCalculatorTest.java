package com.lyf.supplychain.supplier.service;

import com.lyf.supplychain.supplier.model.SupplierMonthlyScore;
import com.lyf.supplychain.supplier.model.SupplierPerformanceMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 供应商绩效评分计算器测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
class SupplierPerformanceScoreCalculatorTest {

    private final SupplierPerformanceScoreCalculator calculator = new SupplierPerformanceScoreCalculator();

    @Test
    void calculateShouldApplyDay02ScoringRules() {
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(10);
        metrics.setDeliveredOnTime(8);
        metrics.setQualityTotal(5);
        metrics.setQualityPassed(4);
        metrics.setResponseHoursAvg(new BigDecimal("6.00"));
        metrics.setPriceComparison(new BigDecimal("0.9300"));

        SupplierMonthlyScore score = calculator.calculate(metrics);

        assertThat(score.getDeliveryScore()).isEqualByComparingTo("20.00");
        assertThat(score.getQualityScore()).isEqualByComparingTo("20.00");
        assertThat(score.getResponseScore()).isEqualByComparingTo("20.00");
        assertThat(score.getPriceScore()).isEqualByComparingTo("22.00");
        assertThat(score.getTotalScore()).isEqualByComparingTo("82.00");
        assertThat(score.getGrade()).isEqualTo("A");
        assertThat(score.isPurchaseDataEnough()).isTrue();
    }

    @Test
    void calculateShouldUseDefaultScoresWhenQualityOrResponseDataMissing() {
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(2);
        metrics.setDeliveredOnTime(2);
        metrics.setQualityTotal(0);
        metrics.setQualityPassed(0);
        metrics.setResponseHoursAvg(null);
        metrics.setPriceComparison(new BigDecimal("1.0800"));

        SupplierMonthlyScore score = calculator.calculate(metrics);

        assertThat(score.getDeliveryScore()).isEqualByComparingTo("25.00");
        assertThat(score.getQualityScore()).isEqualByComparingTo("25.00");
        assertThat(score.getResponseScore()).isEqualByComparingTo("25.00");
        assertThat(score.getPriceScore()).isEqualByComparingTo("6.00");
        assertThat(score.getTotalScore()).isEqualByComparingTo("81.00");
        assertThat(score.getGrade()).isEqualTo("A");
    }

    @Test
    void calculateShouldMarkPurchaseDataNotEnoughWhenNoPurchaseOrder() {
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(0);
        metrics.setDeliveredOnTime(0);

        SupplierMonthlyScore score = calculator.calculate(metrics);

        assertThat(score.isPurchaseDataEnough()).isFalse();
        assertThat(score.getTotalScore()).isEqualByComparingTo("0.00");
        assertThat(score.getGrade()).isEqualTo("C");
    }
}
