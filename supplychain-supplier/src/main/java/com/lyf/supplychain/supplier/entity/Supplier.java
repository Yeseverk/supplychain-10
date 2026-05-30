package com.lyf.supplychain.supplier.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lyf.supplychain.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商主表实体。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    private String supplierCode;

    private String supplierName;

    private Integer supplierType;

    private String categoryIds;

    private String province;

    private String city;

    private String address;

    private String website;

    private Integer companySize;

    private Integer foundedYear;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String contactWechat;

    private String contactWhatsapp;

    private String bankName;

    private String bankAccount;

    private String bankAccountName;

    private String taxNo;

    private Integer invoiceType;

    private Integer moq;

    private Integer leadTimeDays;

    private Integer monthlyCapacity;

    private String currency;

    private Integer paymentDays;

    private String grade;

    private BigDecimal score;

    private String lastScoreMonth;

    private Integer status;

    private Long auditUserId;

    private LocalDateTime auditTime;

    private String auditRemark;

    private Long portalUserId;

    private Integer portalEnabled;

    private String remark;

    private String tags;
}
