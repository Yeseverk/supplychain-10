package com.lyf.supplychain.supplier.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.supplier.entity.SupplierCert;
import com.lyf.supplychain.supplier.mapper.SupplierCertMapper;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import com.lyf.supplychain.supplier.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 供应商资质到期提醒任务测试。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@ExtendWith(MockitoExtension.class)
class SupplierCertExpireJobTest {

    @Mock
    private SupplierCertMapper certMapper;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private NotificationService notificationService;

    @Test
    void scanShouldExpireOverdueCertAndNotifyWarningDays() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-18T01:00:00Z"), ZoneId.of("Asia/Shanghai"));
        ThreadPoolTaskExecutor executor = taskExecutor();
        SupplierCertExpireJob job = new SupplierCertExpireJob(certMapper, supplierMapper, notificationService, executor, clock);
        when(certMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                cert(1L, LocalDate.of(2026, 5, 17)),
                cert(2L, LocalDate.of(2026, 6, 17)),
                cert(3L, LocalDate.of(2026, 5, 25)),
                cert(4L, LocalDate.of(2026, 5, 19)),
                cert(5L, LocalDate.of(2026, 5, 23))
        ));
        when(certMapper.update(any(SupplierCert.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        SupplierCertExpireJobResult result = job.scanCertExpire(0, 1);

        assertThat(result.getScannedCount()).isEqualTo(5);
        assertThat(result.getExpiredCount()).isEqualTo(1);
        assertThat(result.getNoticeCount()).isEqualTo(3);
        assertThat(result.getFailedCount()).isZero();
        verify(certMapper).update(any(SupplierCert.class), any(LambdaUpdateWrapper.class));
        executor.shutdown();
    }

    @Test
    void scanWithCompletableFutureShouldKeepOriginalImplementationAvailable() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-18T01:00:00Z"), ZoneId.of("Asia/Shanghai"));
        ThreadPoolTaskExecutor executor = taskExecutor();
        SupplierCertExpireJob job = new SupplierCertExpireJob(certMapper, supplierMapper, notificationService, executor, clock);
        when(certMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                cert(1L, LocalDate.of(2026, 5, 17)),
                cert(2L, LocalDate.of(2026, 6, 17)),
                cert(3L, LocalDate.of(2026, 5, 25)),
                cert(4L, LocalDate.of(2026, 5, 19)),
                cert(5L, LocalDate.of(2026, 5, 23))
        ));
        when(certMapper.update(any(SupplierCert.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        SupplierCertExpireJobResult result = job.scanCertExpireWithCompletableFuture(0, 1);

        assertThat(result.getScannedCount()).isEqualTo(5);
        assertThat(result.getExpiredCount()).isEqualTo(1);
        assertThat(result.getNoticeCount()).isEqualTo(3);
        assertThat(result.getFailedCount()).isZero();
        verify(certMapper).update(any(SupplierCert.class), any(LambdaUpdateWrapper.class));
        executor.shutdown();
    }

    @Test
    void scanShouldAppendShardConditionWhenShardTotalGreaterThanOne() {
        ThreadPoolTaskExecutor executor = taskExecutor();
        SupplierCertExpireJob job = new SupplierCertExpireJob(certMapper, supplierMapper, notificationService, executor, Clock.systemDefaultZone());
        when(certMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        job.scanCertExpire(2, 4);

        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(certMapper).selectList(captor.capture());
        String shardSqlSegment = captor.getValue().getExpression().getNormal()
                .get(captor.getValue().getExpression().getNormal().size() - 1)
                .getSqlSegment();
        assertThat(shardSqlSegment).contains("MOD(id");
        executor.shutdown();
    }

    private ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("supplier-cert-test-");
        executor.initialize();
        return executor;
    }

    private SupplierCert cert(Long id, LocalDate expireDate) {
        SupplierCert cert = new SupplierCert();
        cert.setId(id);
        cert.setTenantId(101L);
        cert.setSupplierId(1001L);
        cert.setCertName("营业执照");
        cert.setExpireDate(expireDate);
        cert.setIsExpired(0);
        cert.setIsDeleted(0);
        return cert;
    }
}
