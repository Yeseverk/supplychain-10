package com.lyf.supplychain.supplier.job;

import lombok.Data;

/**
 * 供应商绩效评分任务执行结果。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
public class SupplierPerformanceJobResult {

    private String scoreMonth;

    private int scannedCount;

    private int scoredCount;

    private int skippedCount;

    private int changedCount;

    private int failedCount;

    public void incrementScanned() {
        scannedCount++;
    }

    public void incrementScored() {
        scoredCount++;
    }

    public void incrementSkipped() {
        skippedCount++;
    }

    public void incrementChanged() {
        changedCount++;
    }

    public void incrementFailed() {
        failedCount++;
    }
}
