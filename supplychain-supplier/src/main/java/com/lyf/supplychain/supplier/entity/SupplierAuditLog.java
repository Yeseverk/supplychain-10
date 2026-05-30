package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商审核操作日志实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@TableName("supplier_audit_log")
public class SupplierAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long supplierId;

    private Integer fromStatus;

    private Integer toStatus;

    private String action;

    private String auditRemark;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operateTime;
}
