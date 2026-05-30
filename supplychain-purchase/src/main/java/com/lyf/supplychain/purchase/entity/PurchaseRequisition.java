package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购申请单主表实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_requisition")
public class PurchaseRequisition extends BaseEntity {

    private String reqNo;
    private Integer reqSource;
    private String title;
    private Long warehouseId;
    private LocalDate expectDate;
    private BigDecimal totalAmount;
    private Integer priority;
    private Integer status;
    private Integer approvalLevel;
    private String approvalRole;
    private Long applyUserId;
    private String applyUserName;
    private LocalDateTime applyTime;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private String remark;
}
