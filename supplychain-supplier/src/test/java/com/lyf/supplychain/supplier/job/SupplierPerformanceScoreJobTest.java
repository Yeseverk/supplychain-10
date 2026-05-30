package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierScoreLog;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierPerformanceDataMapper;
import com.lyf.supplychain.supplier.mapper.SupplierScoreLogMapper;
import com.lyf.supplychain.supplier.model.SupplierPerformanceMetrics;
import com.lyf.supplychain.supplier.service.NotificationService;
import com.lyf.supplychain.supplier.service.SupplierPerformanceScoreCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商绩效评分定时任务测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class SupplierPerformanceScoreJobTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierScoreLogMapper scoreLogMapper;

    @Mock
    private SupplierPerformanceDataMapper performanceDataMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    void executeMonthlyScoreShouldWriteScoreLogAndUpdateSupplierWhenDataEnough() {
        Clock clock = Clock.fixed(Instant.parse("2025-02-01T00:30:00Z"), ZoneId.of("Asia/Shanghai"));
        SupplierPerformanceScoreJob job = new SupplierPerformanceScoreJob(
                supplierMapper,
                scoreLogMapper,
                performanceDataMapper,
                new SupplierPerformanceScoreCalculator(),
                notificationService,
                clock
        );
        Supplier supplier = approvedSupplier();
        supplier.setGrade("B");
        supplier.setScore(new BigDecimal("70.00"));
        when(supplierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(supplier));
        when(performanceDataMapper.selectDeliveryMetrics(eq(100L), eq(1L), eq("202501"))).thenReturn(deliveryMetrics());
        when(performanceDataMapper.selectQualityMetrics(eq(100L), eq(1L), eq("202501"))).thenReturn(qualityMetrics());
        when(performanceDataMapper.selectResponseHoursAvg(eq(100L), eq(1L), eq("202501"))).thenReturn(new BigDecimal("6.00"));
        when(performanceDataMapper.selectPriceComparison(eq(100L), eq(1L), eq("202501"))).thenReturn(new BigDecimal("0.9300"));

        SupplierPerformanceJobResult result = job.executeMonthlyScore();

        assertThat(result.getScoreMonth()).isEqualTo("202501");
        assertThat(result.getScannedCount()).isEqualTo(1);
        assertThat(result.getScoredCount()).isEqualTo(1);
        assertThat(result.getSkippedCount()).isZero();
        ArgumentCaptor<SupplierScoreLog> logCaptor = ArgumentCaptor.forClass(SupplierScoreLog.class);
        verify(scoreLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getTotalScore()).isEqualByComparingTo("82.00");
        assertThat(logCaptor.getValue().getGrade()).isEqualTo("A");
        assertThat(logCaptor.getValue().getPrevGrade()).isEqualTo("B");
        assertThat(logCaptor.getValue().getGradeChanged()).isEqualTo(1);
        verify(supplierMapper).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void executeMonthlyScoreShouldKeepPreviousScoreWhenNoPurchaseData() {
        Clock clock = Clock.fixed(Instant.parse("2025-02-01T00:30:00Z"), ZoneId.of("Asia/Shanghai"));
        SupplierPerformanceScoreJob job = new SupplierPerformanceScoreJob(
                supplierMapper,
                scoreLogMapper,
                performanceDataMapper,
                new SupplierPerformanceScoreCalculator(),
                notificationService,
                clock
        );
        Supplier supplier = approvedSupplier();
        supplier.setGrade("A");
        supplier.setScore(new BigDecimal("80.00"));
        when(supplierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(supplier));
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(0);
        metrics.setDeliveredOnTime(0);
        when(performanceDataMapper.selectDeliveryMetrics(eq(100L), eq(1L), eq("202501"))).thenReturn(metrics);
        when(performanceDataMapper.selectQualityMetrics(eq(100L), eq(1L), eq("202501"))).thenReturn(new SupplierPerformanceMetrics());

        SupplierPerformanceJobResult result = job.executeMonthlyScore();

        assertThat(result.getScannedCount()).isEqualTo(1);
        assertThat(result.getSkippedCount()).isEqualTo(1);
        ArgumentCaptor<SupplierScoreLog> logCaptor = ArgumentCaptor.forClass(SupplierScoreLog.class);
        verify(scoreLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getCalcRemark()).contains("本月无采购数据");
        assertThat(logCaptor.getValue().getTotalScore()).isEqualByComparingTo("80.00");
        assertThat(logCaptor.getValue().getGrade()).isEqualTo("A");
        verify(supplierMapper, never()).update(any(Supplier.class), any(LambdaUpdateWrapper.class));
    }

    private Supplier approvedSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setTenantId(100L);
        supplier.setSupplierName("广州市测试电子有限公司");
        supplier.setCreateTime(LocalDateTime.of(2024, 12, 1, 9, 0));
        return supplier;
    }

    private SupplierPerformanceMetrics deliveryMetrics() {
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setTotalOrders(10);
        metrics.setDeliveredOnTime(8);
        return metrics;
    }

    private SupplierPerformanceMetrics qualityMetrics() {
        SupplierPerformanceMetrics metrics = new SupplierPerformanceMetrics();
        metrics.setQualityTotal(5);
        metrics.setQualityPassed(4);
        return metrics;
    }
}
