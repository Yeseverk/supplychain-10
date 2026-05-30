package com.lyf.supplychain.finance.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 利润快照预警任务测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@ExtendWith(MockitoExtension.class)
class FinanceProfitSnapshotJobTest {

    @Mock
    private FinanceSettlementBiService financeSettlementBiService;

    @Mock
    private ReliableNotificationEventPublisher notificationEventPublisher;

    @Test
    void runOnceShouldPublishReliableNotificationWhenLossWarningExists() {
        when(financeSettlementBiService.lossWarnings()).thenReturn(List.of(Map.of("skuCode", "SKU-LOSS")));
        FinanceProfitSnapshotJob job = new FinanceProfitSnapshotJob(financeSettlementBiService, notificationEventPublisher);

        int count = job.runOnce();

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<ReliableNotificationCommand> captor = ArgumentCaptor.forClass(ReliableNotificationCommand.class);
        verify(notificationEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getSourceService()).isEqualTo(EventConstants.SourceService.FINANCE);
        assertThat(captor.getValue().getBizType()).isEqualTo(EventConstants.BizType.FMS_LOSS_WARNING);
        assertThat(captor.getValue().getContent()).contains("1");
    }
}
