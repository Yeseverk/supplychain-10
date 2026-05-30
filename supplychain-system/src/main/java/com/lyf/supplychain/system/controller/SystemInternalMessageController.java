package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.service.SystemNotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统消息内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping("/internal/system")
public class SystemInternalMessageController {

    private final SystemNotificationService notificationService;

    public SystemInternalMessageController(SystemNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 通过内部接口发送站内信。
     *
     * @param request 站内信发送请求
     * @return 消息ID
     */
    @PostMapping("/messages")
    public R<Long> send(@RequestBody SystemMessageSendRequest request) {
        return R.ok(notificationService.send(request));
    }
}
