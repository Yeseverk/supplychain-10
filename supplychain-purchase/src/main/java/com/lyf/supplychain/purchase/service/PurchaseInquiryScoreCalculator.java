package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.purchase.model.PurchaseInquiryScore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 采购询价综合评分计算器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class PurchaseInquiryScoreCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private static final BigDecimal PRICE_WEIGHT = new BigDecimal("0.40");

    private static final BigDecimal DELIVERY_WEIGHT = new BigDecimal("0.30");

    private static final BigDecimal SUPPLIER_GRADE_WEIGHT = new BigDecimal("0.30");

    /**
     * 按价格、交期和供应商评级计算综合得分。
     *
     * @param bestQuoteAmount 当前采购申请下最低报价
     * @param quoteAmount     当前供应商报价
     * @param deliveryDays    交货天数
     * @param supplierGrade   供应商评级
     * @return 综合评分
     */
    public PurchaseInquiryScore calculate(BigDecimal bestQuoteAmount,
                                          BigDecimal quoteAmount,
                                          Integer deliveryDays,
                                          String supplierGrade) {
        PurchaseInquiryScore score = new PurchaseInquiryScore();
        score.setPriceScore(priceScore(bestQuoteAmount, quoteAmount));
        score.setDeliveryScore(deliveryScore(deliveryDays));
        score.setSupplierGradeScore(supplierGradeScore(supplierGrade));
        score.setTotalScore(score.getPriceScore().multiply(PRICE_WEIGHT)
                .add(score.getDeliveryScore().multiply(DELIVERY_WEIGHT))
                .add(score.getSupplierGradeScore().multiply(SUPPLIER_GRADE_WEIGHT))
                .setScale(2, RoundingMode.HALF_UP));
        return score;
    }

    private BigDecimal priceScore(BigDecimal bestQuoteAmount, BigDecimal quoteAmount) {
        if (bestQuoteAmount == null || quoteAmount == null || quoteAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return bestQuoteAmount.multiply(HUNDRED)
                .divide(quoteAmount, 2, RoundingMode.HALF_UP)
                .min(HUNDRED);
    }

    private BigDecimal deliveryScore(Integer deliveryDays) {
        if (deliveryDays == null) {
            return new BigDecimal("50.00");
        }
        if (deliveryDays <= 3) {
            return HUNDRED;
        }
        if (deliveryDays <= 7) {
            return new BigDecimal("90.00");
        }
        if (deliveryDays <= 15) {
            return new BigDecimal("75.00");
        }
        if (deliveryDays <= 30) {
            return new BigDecimal("60.00");
        }
        return new BigDecimal("40.00");
    }

    private BigDecimal supplierGradeScore(String supplierGrade) {
        return switch (supplierGrade == null ? "C" : supplierGrade) {
            case "S" -> HUNDRED;
            case "A" -> new BigDecimal("90.00");
            case "B" -> new BigDecimal("75.00");
            case "C" -> new BigDecimal("60.00");
            default -> new BigDecimal("50.00");
        };
    }
}
