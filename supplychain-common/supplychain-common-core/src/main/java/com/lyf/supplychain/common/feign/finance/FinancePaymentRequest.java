package com.lyf.supplychain.common.feign.finance;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 付款登记请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class FinancePaymentRequest {

    private BigDecimal paymentAmount;

    private LocalDate paymentDate;

    private Integer paymentMethod;

    private String voucherNo;

    private Long operatorId;

    private String operatorName;

    private String remark;
}
