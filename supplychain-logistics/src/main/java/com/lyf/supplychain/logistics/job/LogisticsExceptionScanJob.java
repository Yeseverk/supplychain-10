package com.lyf.supplychain.logistics.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.logistics.service.LogisticsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 物流异常扫描 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class LogisticsExceptionScanJob {

    private final LogisticsService logisticsService;

    private final ReliableNotificationEventPublisher notificationEventPublisher;

    public LogisticsExceptionScanJob(LogisticsService logisticsService,
                                     ReliableNotificationEventPublisher notificationEventPublisher) {
        this.logisticsService = logisticsService;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    /**
     * 扫描长时间未更新轨迹的异常运单。
     */
    @XxlJob("logisticsExceptionScanJob")
    public void execute() {
        int count = runOnce();
        String message = "物流异常扫描完成，异常运单数=" + count;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }

    /**
     * 执行一次物流异常扫描，并在发现异常时发布可靠通知事件。
     *
     * @return 异常运单数量
     */
    public int runOnce() {
        int count = logisticsService.scanExceptions();
        if (count > 0) {
            notificationEventPublisher.publish(ReliableNotificationCommand.builder()
                    .tenantId(0L)
                    .receiverType(EventConstants.Notification.RECEIVER_TYPE_ROLE)
                    .receiverKey(EventConstants.ReceiverRole.LOGISTICS_MANAGER)
                    .title("TMS物流异常预警")
                    .content("当前发现" + count + "票物流异常运单，请及时联系物流商跟进。")
                    .bizType(EventConstants.BizType.TMS_LOGISTICS_EXCEPTION)
                    .bizId("daily-scan")
                    .priority(EventConstants.Notification.PRIORITY_HIGH)
                    .sourceService(EventConstants.SourceService.LOGISTICS)
                    .build());
        }
        return count;
    }
}
