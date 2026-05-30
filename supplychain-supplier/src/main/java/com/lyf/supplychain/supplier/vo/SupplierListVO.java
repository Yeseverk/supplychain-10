package com.lyf.supplychain.supplier.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商列表响应。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
public class SupplierListVO {

    private Long id;

    private String supplierCode;

    private String supplierName;

    private String supplierTypeName;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String grade;

    private BigDecimal score;

    private Integer status;

    private String statusName;

    private Integer moq;

    private Integer leadTimeDays;

    private LocalDateTime createTime;

    private Boolean certExpireWarning;
}
