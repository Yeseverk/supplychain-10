package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    @NotNull(message = "操作人用户ID不能为空")
    private Long userId;

    @NotBlank(message = "操作人用户名不能为空")
    private String username;

    @NotBlank(message = "操作模块不能为空")
    private String module;

    @NotBlank(message = "操作动作不能为空")
    private String action;

    @NotBlank(message = "请求方法不能为空")
    private String method;

    private String requestParams;

    private Integer responseCode;

    @NotBlank(message = "操作来源IP不能为空")
    private String ipAddress;

    private String userAgent;

    private Integer durationMs;

    private Integer status;

    private String errorMsg;

    private LocalDateTime operateTime;
}
