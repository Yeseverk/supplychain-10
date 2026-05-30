package com.lyf.supplychain.finance.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.finance.entity.BiKpiThreshold;
import com.lyf.supplychain.finance.request.AiQueryRequest;
import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * BI 数据分析接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.BI_DASHBOARD_VIEW)
public class BiController {

    private final FinanceSettlementBiService financeSettlementBiService;

    public BiController(FinanceSettlementBiService financeSettlementBiService) {
        this.financeSettlementBiService = financeSettlementBiService;
    }

    /**
     * 查询 BI 聚合看板，兼容早期前端聚合路径。
     *
     * @return BI 聚合看板
     */
    @GetMapping({"/api/bi/dashboard", "/bi/dashboard"})
    public R<Map<String, Object>> dashboard() {
        return R.ok(Map.of(
                "overview", financeSettlementBiService.dashboardOverview(),
                "realtime", financeSettlementBiService.dashboardRealtime(),
                "kpi", financeSettlementBiService.kpiDashboard()
        ));
    }

    /**
     * 查询经营总览。
     *
     * @return 经营总览
     */
    @GetMapping({"/api/bi/dashboard/overview", "/bi/dashboard/overview"})
    public R<Map<String, Object>> dashboardOverview() {
        return R.ok(financeSettlementBiService.dashboardOverview());
    }

    /**
     * 查询今日实时数据。
     *
     * @return 今日实时数据
     */
    @GetMapping({"/api/bi/dashboard/realtime", "/bi/dashboard/realtime"})
    public R<Map<String, Object>> dashboardRealtime() {
        return R.ok(financeSettlementBiService.dashboardRealtime());
    }

    /**
     * 查询销售趋势。
     *
     * @return 销售趋势
     */
    @GetMapping({"/api/bi/sales/trend", "/bi/sales/trend"})
    public R<Map<String, Object>> salesTrend() {
        return R.ok(financeSettlementBiService.salesTrend());
    }

    /**
     * 查询平台销售对比。
     *
     * @return 平台销售对比
     */
    @GetMapping({"/api/bi/sales/platform-compare", "/bi/sales/platform-compare"})
    public R<Map<String, Object>> platformCompare() {
        return R.ok(financeSettlementBiService.platformCompare());
    }

    /**
     * 查询库存健康报告。
     *
     * @return 库存健康
     */
    @GetMapping({"/api/bi/inventory/health", "/bi/inventory/health"})
    public R<Map<String, Object>> inventoryHealth() {
        return R.ok(financeSettlementBiService.inventoryHealth());
    }

    /**
     * 查询库存周转率。
     *
     * @return 库存周转率
     */
    @GetMapping({"/api/bi/inventory/turnover", "/bi/inventory/turnover"})
    public R<Map<String, Object>> inventoryTurnover() {
        return R.ok(financeSettlementBiService.inventoryTurnover());
    }

    /**
     * 查询补货建议。
     *
     * @return 补货建议
     */
    @GetMapping({"/api/bi/reorder/suggestions", "/bi/reorder/suggestions"})
    public R<List<Map<String, Object>>> reorderSuggestions() {
        return R.ok(financeSettlementBiService.reorderSuggestions());
    }

    /**
     * 将补货建议转采购申请。
     *
     * @return 转换结果
     */
    @PostMapping({"/api/bi/reorder/to-purchase", "/bi/reorder/to-purchase"})
    @TenantWriteGuard(scene = "补货建议转采购申请")
    @OperationLog(module = "BI智能补货", action = "补货建议转采购申请", type = OperationLog.Type.INSERT)
    public R<Map<String, Object>> reorderToPurchase(@RequestBody(required = false) Map<String, Object> suggestion) {
        return R.ok(financeSettlementBiService.reorderToPurchase(suggestion));
    }

    /**
     * 预测指定 SKU 日销量。
     *
     * @param skuId SKU ID
     * @return 预测结果
     */
    @GetMapping({"/api/bi/reorder/forecast/{skuId}", "/bi/reorder/forecast/{skuId}"})
    public R<Map<String, Object>> forecast(@PathVariable("skuId") Long skuId) {
        return R.ok(financeSettlementBiService.forecastSku(skuId));
    }

    /**
     * 查询 KPI 看板。
     *
     * @return KPI 看板
     */
    @GetMapping({"/api/bi/kpi/dashboard", "/bi/kpi/dashboard"})
    public R<Map<String, Object>> kpiDashboard() {
        return R.ok(financeSettlementBiService.kpiDashboard());
    }

    /**
     * 查询 KPI 趋势。
     *
     * @return KPI 趋势
     */
    @GetMapping({"/api/bi/kpi/trend", "/bi/kpi/trend"})
    public R<Map<String, Object>> kpiTrend() {
        return R.ok(financeSettlementBiService.kpiTrend());
    }

    /**
     * 查询 KPI 阈值配置。
     *
     * @return 阈值配置
     */
    @GetMapping({"/api/bi/kpi/thresholds", "/bi/kpi/thresholds"})
    public R<List<BiKpiThreshold>> kpiThresholds() {
        return R.ok(financeSettlementBiService.kpiThresholds());
    }

    /**
     * 保存 KPI 阈值配置。
     *
     * @param thresholds 阈值列表
     * @return 无数据响应
     */
    @PutMapping({"/api/bi/kpi/thresholds", "/bi/kpi/thresholds"})
    @TenantWriteGuard(scene = "保存KPI阈值")
    @OperationLog(module = "BI指标预警", action = "保存KPI阈值", type = OperationLog.Type.UPDATE)
    public R<Void> saveKpiThresholds(@RequestBody List<BiKpiThreshold> thresholds) {
        financeSettlementBiService.saveKpiThresholds(thresholds);
        return R.ok();
    }

    /**
     * 执行 AI 自然语言查询。
     *
     * @param request 查询请求
     * @return 查询结果
     */
    @PostMapping({"/api/bi/ai/query", "/bi/ai/query"})
    @TenantWriteGuard(scene = "AI自然语言查询")
    public R<Map<String, Object>> aiQuery(@Valid @RequestBody AiQueryRequest request) {
        return R.ok(financeSettlementBiService.aiQuery(request));
    }

    /**
     * 查询 AI 快捷分析模板。
     *
     * @return 模板列表
     */
    @GetMapping({"/api/bi/ai/templates", "/bi/ai/templates"})
    public R<List<Map<String, Object>>> aiTemplates() {
        return R.ok(financeSettlementBiService.aiTemplates());
    }

    /**
     * 导出 BI 报表。
     *
     * @return 导出结果
     */
    @PostMapping({"/api/bi/export", "/bi/export"})
    @OperationLog(module = "BI报表", action = "导出BI报表", type = OperationLog.Type.EXPORT, saveParam = false)
    public R<Map<String, Object>> export() {
        return R.ok(financeSettlementBiService.exportReport());
    }
}
