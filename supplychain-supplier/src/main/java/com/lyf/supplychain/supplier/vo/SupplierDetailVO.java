package com.lyf.supplychain.supplier.vo;

import com.lyf.supplychain.supplier.entity.SupplierAuditLog;
import com.lyf.supplychain.supplier.entity.SupplierCert;
import com.lyf.supplychain.supplier.entity.SupplierContact;
import com.lyf.supplychain.supplier.entity.SupplierScoreLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商详情响应。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierDetailVO {

    private Long id;

    private String supplierCode;

    private String supplierName;

    private Integer supplierType;

    private String categoryIds;

    private String province;

    private String city;

    private String address;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String contactWechat;

    private String contactWhatsapp;

    private String bankName;

    private String bankAccount;

    private String bankAccountName;

    private String taxNo;

    private Integer moq;

    private Integer leadTimeDays;

    private Integer monthlyCapacity;

    private String currency;

    private Integer paymentDays;

    private String grade;

    private BigDecimal score;

    private Integer status;

    private String remark;

    private String tags;

    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<SupplierCert> certs;

    private List<SupplierContact> contacts;

    private List<SupplierScoreLog> scoreLogs;

    private List<SupplierAuditLog> auditLogs;
}
