package com.lyf.supplychain.common.feign.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物流账单应付创建请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class FinanceLogisticsPayableCreateRequest {

    private Long tenantId;

    private String billBatchNo;

    private String carrierCode;

    private BigDecimal payableAmount;

    private String currency;

    private LocalDate invoiceDate;

    private Integer paymentDays;
}
