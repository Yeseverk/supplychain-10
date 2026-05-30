package com.lyf.supplychain.logistics.job;

import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.notification.ReliableNotificationCommand;
import com.lyf.supplychain.common.notification.ReliableNotificationEventPublisher;
import com.lyf.supplychain.logistics.service.LogisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 物流异常扫描任务测试。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@ExtendWith(MockitoExtension.class)
class LogisticsExceptionScanJobTest {

    @Mock
    private LogisticsService logisticsService;

    @Mock
    private ReliableNotificationEventPublisher notificationEventPublisher;

    @Test
    void runOnceShouldPublishReliableNotificationWhenExceptionExists() {
        when(logisticsService.scanExceptions()).thenReturn(3);
        LogisticsExceptionScanJob job = new LogisticsExceptionScanJob(logisticsService, notificationEventPublisher);

        int count = job.runOnce();

        assertThat(count).isEqualTo(3);
        ArgumentCaptor<ReliableNotificationCommand> captor = ArgumentCaptor.forClass(ReliableNotificationCommand.class);
        verify(notificationEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getSourceService()).isEqualTo(EventConstants.SourceService.LOGISTICS);
        assertThat(captor.getValue().getBizType()).isEqualTo(EventConstants.BizType.TMS_LOGISTICS_EXCEPTION);
        assertThat(captor.getValue().getContent()).contains("3");
    }
}
