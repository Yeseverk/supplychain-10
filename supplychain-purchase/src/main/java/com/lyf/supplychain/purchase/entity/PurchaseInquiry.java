package com.lyf.supplychain.purchase.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 询价单实体。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@TableName("purchase_inquiry")
public class PurchaseInquiry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private String inquiryNo;
    private Long reqId;
    private Long supplierId;
    private String supplierName;
    private Integer status;
    private LocalDateTime sendTime;
    private LocalDateTime quoteDeadline;
    private LocalDateTime quotedTime;
    private BigDecimal responseHours;
    private BigDecimal totalQuoteAmt;
    private Integer quoteValidDays;
    private LocalDate quoteExpireDate;
    private String remark;
    private String supplierRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableLogic
    private Integer isDeleted;
}
