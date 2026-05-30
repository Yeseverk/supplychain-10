package com.lyf.supplychain.finance.service.impl;

import java.math.BigDecimal;

/**
 * 应付账款退货冲减策略。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public class FinancePayableOffsetPolicy {

    /**
     * 计算退货冲减后的应付金额，不能低于已付款金额。
     *
     * @param payableAmount 当前应付金额
     * @param offsetAmount  冲减金额
     * @param paidAmount    已付款金额
     * @return 冲减后的应付金额
     */
    public BigDecimal offset(BigDecimal payableAmount, BigDecimal offsetAmount, BigDecimal paidAmount) {
        BigDecimal safePayableAmount = payableAmount == null ? BigDecimal.ZERO : payableAmount;
        BigDecimal safeOffsetAmount = offsetAmount == null ? BigDecimal.ZERO : offsetAmount;
        BigDecimal safePaidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        BigDecimal targetAmount = safePayableAmount.subtract(safeOffsetAmount);
        return targetAmount.max(safePaidAmount);
    }
}
