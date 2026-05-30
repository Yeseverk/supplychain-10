package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.entity.SupplierRiskEvent;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.mapper.SupplierRiskEventMapper;
import com.lyf.supplychain.supplier.mapper.SupplierTenantConfigMapper;
import com.lyf.supplychain.supplier.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 多供应商策略风险扫描任务测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class SupplierMultiSupplierRiskJobTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierRiskEventMapper riskEventMapper;

    @Mock
    private SupplierTenantConfigMapper tenantConfigMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    void executeRiskScanShouldCreateRiskEventWhenCategoryHasOnlyOneSupplier() {
        SupplierMultiSupplierRiskJob job = new SupplierMultiSupplierRiskJob(
                supplierMapper,
                riskEventMapper,
                tenantConfigMapper,
                notificationService,
                Clock.fixed(Instant.parse("2026-05-18T01:00:00Z"), ZoneId.of("Asia/Shanghai"))
        );
        when(supplierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                supplier(1L, 100L, "[10001]", "A")
        ));
        when(riskEventMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        SupplierRiskScanJobResult result = job.executeRiskScan();

        assertThat(result.getCategoryCount()).isEqualTo(1);
        assertThat(result.getCreatedCount()).isEqualTo(1);
        ArgumentCaptor<SupplierRiskEvent> captor = ArgumentCaptor.forClass(SupplierRiskEvent.class);
        verify(riskEventMapper).insert(captor.capture());
        assertThat(captor.getValue().getCategoryId()).isEqualTo(10001L);
        assertThat(captor.getValue().getRiskType()).isEqualTo("SUPPLIER_COUNT_LOW");
    }

    @Test
    void executeRiskScanShouldCloseExistingEventWhenRiskResolved() {
        SupplierMultiSupplierRiskJob job = new SupplierMultiSupplierRiskJob(
                supplierMapper,
                riskEventMapper,
                tenantConfigMapper,
                notificationService,
                Clock.fixed(Instant.parse("2026-05-18T01:00:00Z"), ZoneId.of("Asia/Shanghai"))
        );
        when(supplierMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                supplier(1L, 100L, "[10001]", "A"),
                supplier(2L, 100L, "[10001]", "B")
        ));
        SupplierRiskEvent existing = new SupplierRiskEvent();
        existing.setId(10L);
        existing.setTenantId(100L);
        existing.setCategoryId(10001L);
        existing.setRiskType("SUPPLIER_COUNT_LOW");
        existing.setStatus(1);
        when(riskEventMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        SupplierRiskScanJobResult result = job.executeRiskScan();

        assertThat(result.getResolvedCount()).isEqualTo(1);
        verify(riskEventMapper).update(any(SupplierRiskEvent.class), any(LambdaUpdateWrapper.class));
    }

    private Supplier supplier(Long id, Long tenantId, String categoryIds, String grade) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setTenantId(tenantId);
        supplier.setSupplierName("测试供应商" + id);
        supplier.setCategoryIds(categoryIds);
        supplier.setGrade(grade);
        return supplier;
    }
}
