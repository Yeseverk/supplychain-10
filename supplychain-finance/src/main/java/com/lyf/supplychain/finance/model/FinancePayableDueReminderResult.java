package com.lyf.supplychain.finance.model;

import lombok.Data;

/**
 * 应付账款到期提醒执行结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class FinancePayableDueReminderResult {

    private int scannedCount;

    private int notifiedCount;

    private int overdueUpdatedCount;

    /**
     * 记录扫描数量。
     */
    public void incrementScanned() {
        scannedCount++;
    }

    /**
     * 记录通知数量。
     */
    public void incrementNotified() {
        notifiedCount++;
    }

    /**
     * 记录逾期更新数量。
     */
    public void incrementOverdueUpdated() {
        overdueUpdatedCount++;
    }
}
