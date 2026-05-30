package com.lyf.supplychain.system.job;

import com.lyf.supplychain.system.config.SystemEventOutboxProperties;
import com.lyf.supplychain.system.service.SystemEventOutboxService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 系统可靠事件待投递扫描任务。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Component
public class SystemEventOutboxDispatchJob {

    private final SystemEventOutboxService eventOutboxService;

    private final SystemEventOutboxProperties properties;

    public SystemEventOutboxDispatchJob(SystemEventOutboxService eventOutboxService,
                                        SystemEventOutboxProperties properties) {
        this.eventOutboxService = eventOutboxService;
        this.properties = properties;
    }

    /**
     * 扫描待投递事件并执行本地投递。
     */
    @XxlJob("systemEventOutboxDispatchJob")
    public void execute() {
        int dispatched = eventOutboxService.dispatchPending(properties.getDispatchBatchSize());
        String message = "系统可靠事件待投递扫描完成，dispatched=" + dispatched;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
