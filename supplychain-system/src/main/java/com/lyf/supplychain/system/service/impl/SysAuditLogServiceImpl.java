package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.feign.system.SystemAuditLogRecordRequest;
import com.lyf.supplychain.system.entity.SysAuditLog;
import com.lyf.supplychain.system.mapper.SysAuditLogMapper;
import com.lyf.supplychain.system.service.SysAuditLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作审计日志服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysAuditLogServiceImpl extends ServiceImpl<SysAuditLogMapper, SysAuditLog> implements SysAuditLogService {

    /**
     * 记录操作审计日志。
     *
     * @param request 审计日志记录请求
     * @return 审计日志ID
     */
    @Override
    public Long record(SystemAuditLogRecordRequest request) {
        SysAuditLog auditLog = new SysAuditLog();
        BeanUtils.copyProperties(request, auditLog);
        auditLog.setTenantId(request.getTenantId() == null ? 0L : request.getTenantId());
        auditLog.setUserId(request.getUserId() == null ? 0L : request.getUserId());
        auditLog.setUsername(request.getUsername() == null ? "SYSTEM" : request.getUsername());
        auditLog.setIpAddress(request.getIpAddress() == null ? "0.0.0.0" : request.getIpAddress());
        auditLog.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        auditLog.setOperateTime(request.getOperateTime() == null ? LocalDateTime.now() : request.getOperateTime());
        save(auditLog);
        return auditLog.getId();
    }
}
