package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.purchase.model.PurchaseInquiryScore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 采购询价综合评分测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PurchaseInquiryScoreCalculatorTest {

    private final PurchaseInquiryScoreCalculator calculator = new PurchaseInquiryScoreCalculator();

    @Test
    void shouldScoreQuoteByPriceDeliveryAndSupplierGrade() {
        PurchaseInquiryScore score = calculator.calculate(
                new BigDecimal("80.00"),
                new BigDecimal("80.00"),
                5,
                "A"
        );

        assertThat(score.getPriceScore()).isEqualByComparingTo("100.00");
        assertThat(score.getDeliveryScore()).isEqualByComparingTo("90.00");
        assertThat(score.getSupplierGradeScore()).isEqualByComparingTo("90.00");
        assertThat(score.getTotalScore()).isEqualByComparingTo("94.00");
    }

    @Test
    void shouldGiveLowerPriceScoreWhenQuoteIsHigherThanBestPrice() {
        PurchaseInquiryScore score = calculator.calculate(
                new BigDecimal("80.00"),
                new BigDecimal("120.00"),
                20,
                "C"
        );

        assertThat(score.getPriceScore()).isEqualByComparingTo("66.67");
        assertThat(score.getDeliveryScore()).isEqualByComparingTo("60.00");
        assertThat(score.getSupplierGradeScore()).isEqualByComparingTo("60.00");
        assertThat(score.getTotalScore()).isEqualByComparingTo("62.67");
    }
}
