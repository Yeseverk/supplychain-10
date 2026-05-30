package com.lyf.supplychain.finance.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.finance.entity.FinanceBillItem;
import com.lyf.supplychain.finance.entity.FinanceCashFlow;
import com.lyf.supplychain.finance.entity.FinanceExchangeRate;
import com.lyf.supplychain.finance.entity.FinancePlatformBill;
import com.lyf.supplychain.finance.entity.FinanceVatRecord;
import com.lyf.supplychain.finance.request.FinanceBillPageQuery;
import com.lyf.supplychain.finance.request.FinanceCashFlowPageQuery;
import com.lyf.supplychain.finance.request.VatGenerateRequest;
import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 财务结算接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.FMS_PROFIT_VIEW)
public class FinanceSettlementController {

    private final FinanceSettlementBiService financeSettlementBiService;

    public FinanceSettlementController(FinanceSettlementBiService financeSettlementBiService) {
        this.financeSettlementBiService = financeSettlementBiService;
    }

    /**
     * 查询汇率列表。
     *
     * @param query 分页参数
     * @return 汇率分页结果
     */
    @GetMapping({"/api/fms/exchange-rates", "/fms/exchange-rates"})
    public R<PageResult<FinanceExchangeRate>> exchangeRates(PageQuery query) {
        return R.ok(financeSettlementBiService.pageExchangeRates(query));
    }

    /**
     * 手动刷新汇率。
     *
     * @return 刷新结果
     */
    @PostMapping({"/api/fms/exchange-rates/refresh", "/fms/exchange-rates/refresh"})
    @TenantWriteGuard(scene = "手动刷新汇率")
    public R<Map<String, Object>> refreshRates() {
        return R.ok(financeSettlementBiService.refreshExchangeRates());
    }

    /**
     * 查询平台账单列表。
     *
     * @param query 分页参数
     * @return 账单分页结果
     */
    @GetMapping({"/api/fms/bills", "/fms/bills"})
    public R<PageResult<FinancePlatformBill>> bills(FinanceBillPageQuery query) {
        return R.ok(financeSettlementBiService.pageBills(query));
    }

    /**
     * 查询平台账单详情。
     *
     * @param id 账单ID
     * @return 账单详情
     */
    @GetMapping({"/api/fms/bills/{id}", "/fms/bills/{id}"})
    public R<FinancePlatformBill> billDetail(@PathVariable("id") Long id) {
        return R.ok(financeSettlementBiService.detailBill(id));
    }

    /**
     * 上传平台账单。
     *
     * @param file     账单文件
     * @param platform 平台
     * @param storeId  店铺ID
     * @param currency 币种
     * @return 账单ID
     */
    @PostMapping({"/api/fms/bills/upload", "/fms/bills/upload"})
    @TenantWriteGuard(scene = "上传平台账单")
    public R<Long> upload(@RequestParam("file") MultipartFile file,
                          @RequestParam(value = "platform", required = false) String platform,
                          @RequestParam(value = "storeId", required = false) String storeId,
                          @RequestParam(value = "currency", required = false) String currency) {
        return R.ok(financeSettlementBiService.uploadBill(file, platform, storeId, currency));
    }

    /**
     * 手动触发账单解析。
     *
     * @param id 账单ID
     * @return 解析结果
     */
    @PostMapping({"/api/fms/bills/{id}/parse", "/fms/bills/{id}/parse"})
    @TenantWriteGuard(scene = "解析平台账单")
    public R<Map<String, Object>> parse(@PathVariable("id") Long id) {
        return R.ok(financeSettlementBiService.parseBill(id));
    }

    /**
     * 查询账单明细。
     *
     * @param id    账单ID
     * @param query 分页参数
     * @return 明细分页结果
     */
    @GetMapping({"/api/fms/bills/{id}/items", "/fms/bills/{id}/items"})
    public R<PageResult<FinanceBillItem>> items(@PathVariable("id") Long id, PageQuery query) {
        return R.ok(financeSettlementBiService.pageBillItems(id, query));
    }

    /**
     * 标记对账完成。
     *
     * @param id 账单ID
     * @return 无数据响应
     */
    @PutMapping({"/api/fms/bills/{id}/confirm", "/fms/bills/{id}/confirm"})
    @TenantWriteGuard(scene = "确认平台账单")
    @OperationLog(module = "财务结算", action = "确认平台账单", type = OperationLog.Type.UPDATE)
    public R<Void> confirm(@PathVariable("id") Long id) {
        financeSettlementBiService.confirmBill(id);
        return R.ok();
    }

    /**
     * 查询账单汇总报表。
     *
     * @return 汇总结果
     */
    @GetMapping({"/api/fms/bills/summary", "/fms/bills/summary"})
    public R<Map<String, Object>> billSummary() {
        return R.ok(financeSettlementBiService.billSummary());
    }

    /**
     * 查询 SKU 利润报表。
     *
     * @return 利润报表
     */
    @GetMapping({"/api/fms/profit/sku", "/fms/profit/sku"})
    public R<Map<String, Object>> skuProfit() {
        return R.ok(financeSettlementBiService.skuProfit());
    }

    /**
     * 查询店铺利润报表。
     *
     * @return 店铺利润
     */
    @GetMapping({"/api/fms/profit/store", "/fms/profit/store"})
    public R<Map<String, Object>> storeProfit() {
        return R.ok(financeSettlementBiService.storeProfit());
    }

    /**
     * 查询利润趋势。
     *
     * @return 利润趋势
     */
    @GetMapping({"/api/fms/profit/trend", "/fms/profit/trend"})
    public R<Map<String, Object>> profitTrend() {
        return R.ok(financeSettlementBiService.profitTrend());
    }

    /**
     * 查询亏损预警。
     *
     * @return 亏损预警
     */
    @GetMapping({"/api/fms/profit/loss-warning", "/fms/profit/loss-warning"})
    public R<List<Map<String, Object>>> lossWarning() {
        return R.ok(financeSettlementBiService.lossWarnings());
    }

    /**
     * 查询应收账款。
     *
     * @return 应收列表
     */
    @GetMapping({"/api/fms/receivables", "/fms/receivables"})
    public R<List<Map<String, Object>>> receivables() {
        return R.ok(financeSettlementBiService.receivables());
    }

    /**
     * 发起付款申请。
     *
     * @param id 应付账款ID
     * @return 申请结果
     */
    @PostMapping({"/api/fms/payables/{id}/apply", "/fms/payables/{id}/apply"})
    @TenantWriteGuard(scene = "发起付款申请")
    @OperationLog(module = "财务应付", action = "发起付款申请", type = OperationLog.Type.INSERT)
    public R<Map<String, Object>> apply(@PathVariable("id") Long id) {
        return R.ok(financeSettlementBiService.applyPayable(id));
    }

    /**
     * 审批付款申请。
     *
     * @param id 应付账款ID
     * @return 无数据响应
     */
    @PutMapping({"/api/fms/payables/{id}/approve", "/fms/payables/{id}/approve"})
    @TenantWriteGuard(scene = "审批付款申请")
    @OperationLog(module = "财务应付", action = "审批付款申请", type = OperationLog.Type.UPDATE)
    public R<Void> approve(@PathVariable("id") Long id) {
        financeSettlementBiService.approvePayable(id);
        return R.ok();
    }

    /**
     * 确认已付款。
     *
     * @param id 应付账款ID
     * @return 无数据响应
     */
    @PutMapping({"/api/fms/payables/{id}/paid", "/fms/payables/{id}/paid"})
    @TenantWriteGuard(scene = "确认付款")
    @OperationLog(module = "财务应付", action = "确认已付款", type = OperationLog.Type.UPDATE)
    public R<Void> paid(@PathVariable("id") Long id) {
        financeSettlementBiService.markPayablePaid(id);
        return R.ok();
    }

    /**
     * 查询 VAT 申报列表。
     *
     * @param query 分页参数
     * @return VAT 分页结果
     */
    @GetMapping({"/api/fms/vat", "/fms/vat"})
    public R<PageResult<FinanceVatRecord>> vat(PageQuery query) {
        return R.ok(financeSettlementBiService.pageVat(query));
    }

    /**
     * 生成 VAT 申报数据。
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping({"/api/fms/vat/generate", "/fms/vat/generate"})
    @TenantWriteGuard(scene = "生成VAT申报数据")
    @OperationLog(module = "VAT申报", action = "生成VAT申报数据", type = OperationLog.Type.INSERT)
    public R<Map<String, Object>> generateVat(@Valid @RequestBody VatGenerateRequest request) {
        return R.ok(financeSettlementBiService.generateVat(request));
    }

    /**
     * 导出 VAT 申报表。
     *
     * @param id VAT记录ID
     * @return 导出结果
     */
    @GetMapping({"/api/fms/vat/{id}/export", "/fms/vat/{id}/export"})
    @OperationLog(module = "VAT申报", action = "导出VAT申报表", type = OperationLog.Type.EXPORT, saveParam = false)
    public R<Map<String, Object>> exportVat(@PathVariable("id") Long id) {
        return R.ok(financeSettlementBiService.exportVat(id));
    }

    /**
     * 查询资金流水。
     *
     * @param query 分页参数
     * @return 资金流水分页结果
     */
    @GetMapping({"/api/fms/cash-flow", "/fms/cash-flow"})
    public R<PageResult<FinanceCashFlow>> cashFlow(FinanceCashFlowPageQuery query) {
        return R.ok(financeSettlementBiService.pageCashFlow(query));
    }

    /**
     * 查询现金流预测。
     *
     * @return 预测结果
     */
    @GetMapping({"/api/fms/cash-flow/forecast", "/fms/cash-flow/forecast"})
    public R<Map<String, Object>> cashFlowForecast() {
        return R.ok(financeSettlementBiService.cashFlowForecast());
    }
}
