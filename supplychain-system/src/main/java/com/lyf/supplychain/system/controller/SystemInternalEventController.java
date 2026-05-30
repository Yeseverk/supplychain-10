package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import com.lyf.supplychain.system.service.SystemEventOutboxService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统可靠事件内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@RestController
@RequestMapping("/internal/system")
public class SystemInternalEventController {

    private final SystemEventOutboxService eventOutboxService;

    public SystemInternalEventController(SystemEventOutboxService eventOutboxService) {
        this.eventOutboxService = eventOutboxService;
    }

    /**
     * 接收业务模块发布的可靠事件。
     *
     * @param request 事件发布请求
     * @return 事件记录ID
     */
    @PostMapping("/events")
    public R<Long> publish(@RequestBody SystemEventPublishRequest request) {
        return R.ok(eventOutboxService.publish(request));
    }
}
