package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.purchase.service.PurchaseReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 采购报表接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/report", "/pms/report", "/api/pms/reports", "/pms/reports"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_ORDER_LIST)
public class PurchaseReportController {

    private final PurchaseReportService reportService;

    public PurchaseReportController(PurchaseReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 查询采购总览报表。
     *
     * @return 总览数据
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(reportService.overview());
    }

    /**
     * 查询供应商采购金额排行。
     *
     * @return 排行数据
     */
    @GetMapping("/supplier-rank")
    public R<Map<String, Object>> supplierRank() {
        return R.ok(reportService.supplierRank());
    }

    /**
     * 查询月度采购趋势。
     *
     * @return 趋势数据
     */
    @GetMapping("/monthly-trend")
    public R<Map<String, Object>> monthlyTrend() {
        return R.ok(reportService.monthlyTrend());
    }
}
