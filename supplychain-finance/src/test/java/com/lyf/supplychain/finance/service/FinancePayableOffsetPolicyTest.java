package com.lyf.supplychain.finance.service;

import com.lyf.supplychain.finance.service.impl.FinancePayableOffsetPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应付账款退货冲减策略测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class FinancePayableOffsetPolicyTest {

    private final FinancePayableOffsetPolicy policy = new FinancePayableOffsetPolicy();

    @Test
    void shouldReducePayableAmountByReturnAmount() {
        BigDecimal amount = policy.offset(new BigDecimal("1000.00"), new BigDecimal("200.00"), BigDecimal.ZERO);

        assertThat(amount).isEqualByComparingTo("800.00");
    }

    @Test
    void shouldNotReducePayableBelowPaidAmount() {
        BigDecimal amount = policy.offset(new BigDecimal("1000.00"), new BigDecimal("900.00"), new BigDecimal("300.00"));

        assertThat(amount).isEqualByComparingTo("300.00");
    }
}
