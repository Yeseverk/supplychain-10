package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商资质文件实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@TableName("supplier_cert")
public class SupplierCert {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long supplierId;

    private Integer certType;

    private String certName;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private LocalDate issueDate;

    private LocalDate expireDate;

    private Integer isExpired;

    private String certNo;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableLogic
    private Integer isDeleted;
}
