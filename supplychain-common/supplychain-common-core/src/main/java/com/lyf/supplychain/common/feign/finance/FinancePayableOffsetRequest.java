package com.lyf.supplychain.common.feign.finance;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 应付账款冲减请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class FinancePayableOffsetRequest {

    private Long tenantId;

    private Long poId;

    private String returnNo;

    private BigDecimal offsetAmount;

    private String reason;
}
