package com.lyf.supplychain.purchase.service;

import java.util.Map;

/**
 * 采购报表服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseReportService {

    /**
     * 汇总采购总览指标。
     *
     * @return 总览指标
     */
    Map<String, Object> overview();

    /**
     * 汇总供应商采购金额排行。
     *
     * @return 排行指标
     */
    Map<String, Object> supplierRank();

    /**
     * 汇总月度采购趋势。
     *
     * @return 趋势指标
     */
    Map<String, Object> monthlyTrend();
}
