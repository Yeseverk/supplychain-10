package com.lyf.supplychain.purchase.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购自动补货执行结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PurchaseReplenishmentResult {

    private int scannedCount;

    private int generatedCount;

    private int skippedCount;

    private int failedCount;

    private List<Long> generatedReqIds = new ArrayList<>();

    /**
     * 记录扫描数量。
     */
    public void incrementScanned() {
        scannedCount++;
    }

    /**
     * 记录生成成功的采购申请。
     *
     * @param reqId 采购申请ID
     */
    public void addGenerated(Long reqId) {
        generatedCount++;
        generatedReqIds.add(reqId);
    }

    /**
     * 记录跳过数量。
     */
    public void incrementSkipped() {
        skippedCount++;
    }

    /**
     * 记录失败数量。
     */
    public void incrementFailed() {
        failedCount++;
    }
}
