package com.lyf.supplychain.system.job;

import com.lyf.supplychain.system.config.SystemEventOutboxProperties;
import com.lyf.supplychain.system.service.SystemEventOutboxService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 系统可靠事件失败重试任务。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Component
public class SystemEventOutboxRetryJob {

    private final SystemEventOutboxService eventOutboxService;

    private final SystemEventOutboxProperties properties;

    public SystemEventOutboxRetryJob(SystemEventOutboxService eventOutboxService,
                                     SystemEventOutboxProperties properties) {
        this.eventOutboxService = eventOutboxService;
        this.properties = properties;
    }

    /**
     * 重试失败的可靠事件。
     */
    @XxlJob("systemEventOutboxRetryJob")
    public void execute() {
        int retried = eventOutboxService.retryFailed(properties.getMaxRetryCount(), properties.getRetryBatchSize());
        String message = "系统可靠事件失败重试完成，retried=" + retried;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
