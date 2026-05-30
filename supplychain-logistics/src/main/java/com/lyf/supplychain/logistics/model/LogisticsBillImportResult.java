package com.lyf.supplychain.logistics.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流商账单导入结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class LogisticsBillImportResult {

    private String billBatchNo;
    private Integer importedCount = 0;
    private Integer matchedCount = 0;
    private Integer autoConfirmedCount = 0;
    private Integer pendingReviewCount = 0;
    private Integer unmatchedCount = 0;
    private BigDecimal totalActualFee = BigDecimal.ZERO;
    private BigDecimal totalDiffAmount = BigDecimal.ZERO;
}
