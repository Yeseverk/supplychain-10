package com.lyf.supplychain.supplier.service;

import com.lyf.supplychain.supplier.model.SupplierMonthlyScore;
import com.lyf.supplychain.supplier.model.SupplierPerformanceMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * 供应商绩效评分计算器。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Component
public class SupplierPerformanceScoreCalculator {

    private static final BigDecimal DIMENSION_FULL_SCORE = new BigDecimal("25.00");

    private static final BigDecimal SCORE_S = new BigDecimal("90.00");

    private static final BigDecimal SCORE_A = new BigDecimal("75.00");

    private static final BigDecimal SCORE_B = new BigDecimal("60.00");

    /**
     * 根据 Day02 评分规则计算月度绩效评分。
     *
     * @param metrics 月度原始指标
     * @return 月度评分结果
     */
    public SupplierMonthlyScore calculate(SupplierPerformanceMetrics metrics) {
        SupplierPerformanceMetrics safeMetrics = Optional.ofNullable(metrics).orElseGet(SupplierPerformanceMetrics::new);
        SupplierMonthlyScore score = new SupplierMonthlyScore();
        // 如果当月没有采购单 设置为没有足够的采购数据
        if (safeInteger(safeMetrics.getTotalOrders()) <= 0) {
            score.setPurchaseDataEnough(false);
            score.setTotalScore(scale(BigDecimal.ZERO));
            score.setGrade("C");
            return score;
        }
        score.setPurchaseDataEnough(true);
        // 计算准时交货得分
        score.setDeliveryScore(rateScore(safeMetrics.getDeliveredOnTime(), safeMetrics.getTotalOrders()));
        // 计算质量合格率
        score.setQualityScore(qualityScore(safeMetrics));
        // 计算平均响应时长
        score.setResponseScore(responseScore(safeMetrics.getResponseHoursAvg()));
        // 计算市场价格竞争力
        score.setPriceScore(priceScore(safeMetrics.getPriceComparison()));
        BigDecimal totalScore = score.getDeliveryScore()
                .add(score.getQualityScore())
                .add(score.getResponseScore())
                .add(score.getPriceScore());
        // 计算总分
        score.setTotalScore(scale(totalScore));
        // 计算评级
        score.setGrade(resolveGrade(score.getTotalScore()));
        return score;
    }

    private BigDecimal rateScore(Integer numerator, Integer denominator) {
        if (safeInteger(denominator) <= 0) {
            return scale(BigDecimal.ZERO);
        }
        BigDecimal rate = BigDecimal.valueOf(safeInteger(numerator))
                .divide(BigDecimal.valueOf(safeInteger(denominator)), 4, RoundingMode.HALF_UP);
        return scale(rate.multiply(DIMENSION_FULL_SCORE));
    }

    private BigDecimal qualityScore(SupplierPerformanceMetrics metrics) {
        if (safeInteger(metrics.getQualityTotal()) <= 0) {
            return DIMENSION_FULL_SCORE;
        }
        return rateScore(metrics.getQualityPassed(), metrics.getQualityTotal());
    }

    private BigDecimal responseScore(BigDecimal responseHoursAvg) {
        if (responseHoursAvg == null) {
            return DIMENSION_FULL_SCORE;
        }
        if (responseHoursAvg.compareTo(new BigDecimal("2")) <= 0) {
            return new BigDecimal("25.00");
        }
        if (responseHoursAvg.compareTo(new BigDecimal("8")) <= 0) {
            return new BigDecimal("20.00");
        }
        if (responseHoursAvg.compareTo(new BigDecimal("24")) <= 0) {
            return new BigDecimal("15.00");
        }
        if (responseHoursAvg.compareTo(new BigDecimal("48")) <= 0) {
            return new BigDecimal("10.00");
        }
        if (responseHoursAvg.compareTo(new BigDecimal("72")) <= 0) {
            return new BigDecimal("5.00");
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal priceScore(BigDecimal priceComparison) {
        if (priceComparison == null || priceComparison.compareTo(BigDecimal.ZERO) <= 0) {
            return DIMENSION_FULL_SCORE;
        }
        if (priceComparison.compareTo(new BigDecimal("0.90")) < 0) {
            return new BigDecimal("25.00");
        }
        if (priceComparison.compareTo(new BigDecimal("0.95")) <= 0) {
            return new BigDecimal("22.00");
        }
        if (priceComparison.compareTo(new BigDecimal("1.00")) <= 0) {
            return new BigDecimal("18.00");
        }
        if (priceComparison.compareTo(new BigDecimal("1.05")) <= 0) {
            return new BigDecimal("12.00");
        }
        if (priceComparison.compareTo(new BigDecimal("1.10")) <= 0) {
            return new BigDecimal("6.00");
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveGrade(BigDecimal totalScore) {
        if (totalScore.compareTo(SCORE_S) >= 0) {
            return "S";
        }
        if (totalScore.compareTo(SCORE_A) >= 0) {
            return "A";
        }
        if (totalScore.compareTo(SCORE_B) >= 0) {
            return "B";
        }
        return "C";
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
