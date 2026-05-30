package com.lyf.supplychain.supplier.job;

import lombok.Data;

/**
 * 供应商分层分级任务执行结果。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
public class SupplierLayeringJobResult {

    private int scannedCount;

    private int watchlistCreatedCount;

    private int watchlistExistsCount;

    private int failedCount;

    public void incrementScanned() {
        scannedCount++;
    }

    public void incrementWatchlistCreated() {
        watchlistCreatedCount++;
    }

    public void incrementWatchlistExists() {
        watchlistExistsCount++;
    }

    public void incrementFailed() {
        failedCount++;
    }
}
