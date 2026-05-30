package com.lyf.supplychain.finance.service;

import com.lyf.supplychain.finance.model.FinancePayableDueReminderResult;

/**
 * 应付账款到期提醒服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface FinancePayableReminderService {

    /**
     * 扫描即将到期或已逾期的应付账款并发送提醒。
     *
     * @return 执行结果
     */
    FinancePayableDueReminderResult scanDuePayables();
}
