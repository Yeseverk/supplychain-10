package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.system.entity.SysAuditLog;
import com.lyf.supplychain.system.service.SysAuditLogService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作审计日志 CRUD 控制器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@RestController
@RequestMapping("/system/audit-logs")
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SYS_AUDIT_LIST)
public class SysAuditLogController extends BaseCrudController<SysAuditLog> {

    public SysAuditLogController(SysAuditLogService service) {
        super(service);
    }
}
