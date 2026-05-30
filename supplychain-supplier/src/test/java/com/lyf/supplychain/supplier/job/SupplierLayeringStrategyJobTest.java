package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierTenantConfig;
import com.lyf.supplychain.supplier.entity.SupplierWatchlist;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierTenantConfigMapper;
import com.lyf.supplychain.supplier.mapper.SupplierWatchlistMapper;
import com.lyf.supplychain.supplier.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商分层分级策略任务测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class SupplierLayeringStrategyJobTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierWatchlistMapper watchlistMapper;

    @Mock
    private SupplierTenantConfigMapper tenantConfigMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    void executeLayeringShouldCreateWatchlistForConfiguredGrades() {
        SupplierLayeringStrategyJob job = new SupplierLayeringStrategyJob(
                supplierMapper,
                watchlistMapper,
                tenantConfigMapper,
                notificationService,
                Clock.fixed(Instant.parse("2026-05-18T01:00:00Z"), ZoneId.of("Asia/Shanghai"))
        );
        Supplier supplier = supplier(1L, 100L, "B", new BigDecimal("68.00"));
        SupplierTenantConfig config = new SupplierTenantConfig();
        config.setConfigValue("B,C");
        when(supplierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(supplier));
        when(tenantConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(config);
        when(watchlistMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        SupplierLayeringJobResult result = job.executeLayering();

        assertThat(result.getScannedCount()).isEqualTo(1);
        assertThat(result.getWatchlistCreatedCount()).isEqualTo(1);
        ArgumentCaptor<SupplierWatchlist> captor = ArgumentCaptor.forClass(SupplierWatchlist.class);
        verify(watchlistMapper).insert(captor.capture());
        assertThat(captor.getValue().getSupplierId()).isEqualTo(1L);
        assertThat(captor.getValue().getCurrentGrade()).isEqualTo("B");
        assertThat(captor.getValue().getSystemSuggestion()).contains("改进通知");
    }

    private Supplier supplier(Long id, Long tenantId, String grade, BigDecimal score) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setTenantId(tenantId);
        supplier.setSupplierName("测试供应商");
        supplier.setGrade(grade);
        supplier.setScore(score);
        return supplier;
    }
}
