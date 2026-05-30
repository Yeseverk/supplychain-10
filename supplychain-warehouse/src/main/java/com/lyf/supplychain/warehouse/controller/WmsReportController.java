package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.warehouse.service.WmsReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * WMS 报表看板接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/report", "/wms/report"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_INVENTORY_LIST)
public class WmsReportController {

    private final WmsReportService reportService;

    public WmsReportController(WmsReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 库存总览。
     *
     * @return 库存总览
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(reportService.overview());
    }

    /**
     * 库存健康度报告。
     *
     * @return 健康度报告
     */
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        return R.ok(reportService.health());
    }

    /**
     * 出入库趋势。
     *
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public R<Map<String, Object>> trend() {
        return R.ok(reportService.trend());
    }
}
