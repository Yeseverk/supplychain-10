package com.lyf.supplychain.common.feign.system;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作审计日志记录请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SystemAuditLogRecordRequest {

    private Long tenantId;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String method;
    private String requestParams;
    private Integer responseCode;
    private String ipAddress;
    private String userAgent;
    private Integer durationMs;
    private Integer status;
    private String errorMsg;
    private LocalDateTime operateTime;
}
