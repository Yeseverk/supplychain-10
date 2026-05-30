package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商联系人实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@TableName("supplier_contact")
public class SupplierContact {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long supplierId;

    private String contactName;

    private String position;

    private String phone;

    private String email;

    private String wechat;

    private String whatsapp;

    private String department;

    private Integer isPrimary;

    private Integer contactType;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableLogic
    private Integer isDeleted;
}
