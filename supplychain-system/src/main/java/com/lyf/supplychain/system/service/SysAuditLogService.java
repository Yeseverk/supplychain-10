package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.feign.system.SystemAuditLogRecordRequest;
import com.lyf.supplychain.system.entity.SysAuditLog;

/**
 * 操作审计日志服务接口。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysAuditLogService extends IService<SysAuditLog> {

    /**
     * 记录操作审计日志。
     *
     * @param request 审计日志记录请求
     * @return 审计日志ID
     */
    Long record(SystemAuditLogRecordRequest request);
}
