package com.lyf.supplychain.warehouse.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 库存预警定时任务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Component
public class InventoryWarningJob {

    private static final Logger log = LoggerFactory.getLogger(InventoryWarningJob.class);

    private final WmsInventoryService inventoryService;

    private final ReliableNotificationEventPublisher notificationEventPublisher;

    public InventoryWarningJob(WmsInventoryService inventoryService,
                               ReliableNotificationEventPublisher notificationEventPublisher) {
        this.inventoryService = inventoryService;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    /**
     * 扫描库存预警，避免重复通知可由预警事件表兜底。
     */
    @XxlJob("wmsInventoryWarningJob")
    public void scanWarnings() {
        List<Inventory> warnings = inventoryService.warnings();
        int warningCount = warnings.size();
        if (warningCount > 0) {
            publishWarningNotice(warnings);
        }
        log.info("WMS库存预警扫描完成，warningCount={}", warningCount);
    }

    private void publishWarningNotice(List<Inventory> warnings) {
        Inventory first = warnings.get(0);
        notificationEventPublisher.publish(ReliableNotificationCommand.builder()
                .tenantId(0L)
                .receiverType(EventConstants.Notification.RECEIVER_TYPE_ROLE)
                .receiverKey(EventConstants.ReceiverRole.WAREHOUSE_MANAGER)
                .title("WMS库存预警")
                .content("当前发现" + warnings.size() + "条库存预警，请及时处理补货或库容调整。")
                .bizType(EventConstants.BizType.WMS_INVENTORY_WARNING)
                .bizId(String.valueOf(first.getSkuId()))
                .priority(EventConstants.Notification.PRIORITY_HIGH)
                .sourceService(EventConstants.SourceService.WAREHOUSE)
                .build());
    }
}
