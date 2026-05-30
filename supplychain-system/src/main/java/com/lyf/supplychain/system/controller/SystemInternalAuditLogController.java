package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemAuditLogRecordRequest;
import com.lyf.supplychain.system.service.SysAuditLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统操作审计日志内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@RestController
@RequestMapping("/internal/system")
public class SystemInternalAuditLogController {

    private final SysAuditLogService auditLogService;

    public SystemInternalAuditLogController(SysAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 记录操作审计日志。
     *
     * @param request 审计日志记录请求
     * @return 审计日志ID
     */
    @PostMapping("/audit-logs")
    public R<Long> record(@RequestBody SystemAuditLogRecordRequest request) {
        return R.ok(auditLogService.record(request));
    }
}
