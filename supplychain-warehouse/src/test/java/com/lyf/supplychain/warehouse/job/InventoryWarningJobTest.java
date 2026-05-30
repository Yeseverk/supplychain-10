package com.lyf.supplychain.warehouse.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存预警任务测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@ExtendWith(MockitoExtension.class)
class InventoryWarningJobTest {

    @Mock
    private WmsInventoryService inventoryService;

    @Mock
    private ReliableNotificationEventPublisher notificationEventPublisher;

    @Test
    void scanWarningsShouldPublishReliableNotificationWhenWarningExists() {
        Inventory inventory = new Inventory();
        inventory.setSkuId(1001L);
        inventory.setSkuCode("SKU-1001");
        when(inventoryService.warnings()).thenReturn(List.of(inventory));
        InventoryWarningJob job = new InventoryWarningJob(inventoryService, notificationEventPublisher);

        job.scanWarnings();

        ArgumentCaptor<ReliableNotificationCommand> captor = ArgumentCaptor.forClass(ReliableNotificationCommand.class);
        verify(notificationEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getSourceService()).isEqualTo(EventConstants.SourceService.WAREHOUSE);
        assertThat(captor.getValue().getBizType()).isEqualTo(EventConstants.BizType.WMS_INVENTORY_WARNING);
        assertThat(captor.getValue().getContent()).contains("1");
    }
}
