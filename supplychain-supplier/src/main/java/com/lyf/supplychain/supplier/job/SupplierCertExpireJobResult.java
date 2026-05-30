package com.lyf.supplychain.supplier.job;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 供应商资质到期扫描任务执行结果。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
public class SupplierCertExpireJobResult {

    private final AtomicInteger scannedCount = new AtomicInteger();

    private final AtomicInteger expiredCount = new AtomicInteger();

    private final AtomicInteger noticeCount = new AtomicInteger();

    private final AtomicInteger failedCount = new AtomicInteger();

    public int getScannedCount() {
        return scannedCount.get();
    }

    public int getExpiredCount() {
        return expiredCount.get();
    }

    public int getNoticeCount() {
        return noticeCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public void incrementScanned() {
        scannedCount.incrementAndGet();
    }

    public void incrementExpired() {
        expiredCount.incrementAndGet();
    }

    public void incrementNotice() {
        noticeCount.incrementAndGet();
    }

    public void incrementFailed() {
        failedCount.incrementAndGet();
    }
}
