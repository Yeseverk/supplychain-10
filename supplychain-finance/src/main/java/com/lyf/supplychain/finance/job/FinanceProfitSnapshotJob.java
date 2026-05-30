package com.lyf.supplychain.finance.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 利润快照分析 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class FinanceProfitSnapshotJob {

    private final FinanceSettlementBiService financeSettlementBiService;

    private final ReliableNotificationEventPublisher notificationEventPublisher;

    public FinanceProfitSnapshotJob(FinanceSettlementBiService financeSettlementBiService,
                                    ReliableNotificationEventPublisher notificationEventPublisher) {
        this.financeSettlementBiService = financeSettlementBiService;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    /**
     * 每日触发利润分析与亏损预警。
     */
    @XxlJob("financeProfitSnapshotJob")
    public void execute() {
        int warningCount = runOnce();
        String message = "利润快照分析完成，亏损预警数量=" + warningCount;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 执行一次利润预警扫描，并在发现亏损 SKU 时发布可靠通知事件。
     *
     * @return 亏损预警数量
     */
    public int runOnce() {
        List<Map<String, Object>> warnings = financeSettlementBiService.lossWarnings();
        int warningCount = warnings.size();
        if (warningCount > 0) {
            notificationEventPublisher.publish(ReliableNotificationCommand.builder()
                    .tenantId(0L)
                    .receiverType(EventConstants.Notification.RECEIVER_TYPE_ROLE)
                    .receiverKey(EventConstants.ReceiverRole.FINANCE_MANAGER)
                    .title("FMS亏损预警")
                    .content("当前发现" + warningCount + "个亏损SKU，请检查广告费、退款率和采购成本。")
                    .bizType(EventConstants.BizType.FMS_LOSS_WARNING)
                    .bizId("daily-profit")
                    .priority(EventConstants.Notification.PRIORITY_HIGH)
                    .sourceService(EventConstants.SourceService.FINANCE)
                    .build());
        }
        return warningCount;
    }
}
