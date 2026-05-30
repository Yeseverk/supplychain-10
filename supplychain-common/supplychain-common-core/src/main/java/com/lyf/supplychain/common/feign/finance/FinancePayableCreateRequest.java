package com.lyf.supplychain.common.feign.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付账款创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class FinancePayableCreateRequest {

    private Long tenantId;

    private Long poId;

    private String poNo;

    private Long supplierId;

    private String supplierName;

    private String invoiceNo;

    private LocalDate invoiceDate;

    private BigDecimal payableAmount;

    private String currency;

    private Integer paymentDays;
}
