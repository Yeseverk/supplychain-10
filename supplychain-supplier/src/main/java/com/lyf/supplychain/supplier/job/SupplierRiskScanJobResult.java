package com.lyf.supplychain.supplier.job;

import lombok.Data;

/**
 * 多供应商风险扫描任务执行结果。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Data
public class SupplierRiskScanJobResult {

    private int categoryCount;

    private int createdCount;

    private int existsCount;

    private int resolvedCount;

    private int failedCount;

    public void incrementCategory() {
        categoryCount++;
    }

    public void incrementCreated() {
        createdCount++;
    }

    public void incrementExists() {
        existsCount++;
    }

    public void incrementResolved() {
        resolvedCount++;
    }

    public void incrementFailed() {
        failedCount++;
    }
}
