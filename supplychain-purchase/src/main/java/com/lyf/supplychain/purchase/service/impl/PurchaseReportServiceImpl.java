package com.lyf.supplychain.purchase.service.impl;

import com.lyf.supplychain.purchase.mapper.PurchaseReportMapper;
import com.lyf.supplychain.purchase.service.PurchaseReportService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 采购报表服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseReportServiceImpl implements PurchaseReportService {

    private final PurchaseReportMapper reportMapper;

    public PurchaseReportServiceImpl(PurchaseReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    /**
     * 汇总采购总览指标。
     *
     * @return 总览指标
     */
    @Override
    public Map<String, Object> overview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.putAll(reportMapper.overview());
        return overview;
    }

    /**
     * 汇总供应商采购金额排行。
     *
     * @return 排行指标
     */
    @Override
    public Map<String, Object> supplierRank() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", reportMapper.supplierRank());
        return result;
    }

    /**
     * 汇总月度采购趋势。
     *
     * @return 趋势指标
     */
    @Override
    public Map<String, Object> monthlyTrend() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rows", reportMapper.monthlyTrend());
        return result;
    }
}
