package com.lyf.supplychain.finance.job;

import com.lyf.supplychain.finance.model.FinancePayableDueReminderResult;
import com.lyf.supplychain.finance.service.FinancePayableReminderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 应付账款到期提醒 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class FinancePayableDueReminderJob {

    private final FinancePayableReminderService reminderService;

    public FinancePayableDueReminderJob(FinancePayableReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * XXL-JOB 入口，每天扫描应付账款到期情况。
     */
    @XxlJob("financePayableDueReminderJob")
    public void execute() {
        FinancePayableDueReminderResult result = reminderService.scanDuePayables();
        String message = "应付账款到期提醒完成，扫描=" + result.getScannedCount()
                + "，通知=" + result.getNotifiedCount()
                + "，逾期更新=" + result.getOverdueUpdatedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
