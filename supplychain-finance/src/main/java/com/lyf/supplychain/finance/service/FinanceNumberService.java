package com.lyf.supplychain.finance.service;

/**
 * 财务单号服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface FinanceNumberService {

    /**
     * 生成应付账款单号。
     *
     * @return 应付账款单号
     */
    String nextPayableNo();

    /**
     * 生成平台账单编号。
     *
     * @return 平台账单编号
     */
    String nextBillNo();
}
