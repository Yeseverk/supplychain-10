package com.lyf.supplychain.finance.service;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.finance.entity.BiKpiThreshold;
import com.lyf.supplychain.finance.entity.FinanceBillItem;
import com.lyf.supplychain.finance.entity.FinanceCashFlow;
import com.lyf.supplychain.finance.entity.FinanceExchangeRate;
import com.lyf.supplychain.finance.entity.FinancePlatformBill;
import com.lyf.supplychain.finance.entity.FinanceVatRecord;
import com.lyf.supplychain.finance.request.AiQueryRequest;
import com.lyf.supplychain.finance.request.FinanceBillPageQuery;
import com.lyf.supplychain.finance.request.FinanceCashFlowPageQuery;
import com.lyf.supplychain.finance.request.VatGenerateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 财务结算与 BI 分析服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface FinanceSettlementBiService {

    /**
     * 分页查询汇率列表。
     *
     * @param query 分页参数
     * @return 汇率分页结果
     */
    PageResult<FinanceExchangeRate> pageExchangeRates(PageQuery query);

    /**
     * 手动刷新汇率。
     *
     * @return 刷新结果
     */
    Map<String, Object> refreshExchangeRates();

    /**
     * 分页查询平台账单。
     *
     * @param query 分页参数
     * @return 账单分页结果
     */
    PageResult<FinancePlatformBill> pageBills(FinanceBillPageQuery query);

    /**
     * 查询平台账单详情。
     *
     * @param id 账单ID
     * @return 账单详情
     */
    FinancePlatformBill detailBill(Long id);

    /**
     * 上传平台结算账单并创建待解析记录。
     *
     * @param file     账单文件
     * @param platform 平台
     * @param storeId  店铺ID
     * @param currency 币种
     * @return 账单ID
     */
    Long uploadBill(MultipartFile file, String platform, String storeId, String currency);

    /**
     * 解析平台结算账单。
     *
     * @param id 账单ID
     * @return 解析结果
     */
    Map<String, Object> parseBill(Long id);

    /**
     * 分页查询账单明细。
     *
     * @param id    账单ID
     * @param query 分页参数
     * @return 明细分页结果
     */
    PageResult<FinanceBillItem> pageBillItems(Long id, PageQuery query);

    /**
     * 标记账单对账完成。
     *
     * @param id 账单ID
     */
    void confirmBill(Long id);

    /**
     * 查询账单汇总报表。
     *
     * @return 汇总结果
     */
    Map<String, Object> billSummary();

    /**
     * 查询 SKU 利润报表。
     *
     * @return 利润报表
     */
    Map<String, Object> skuProfit();

    /**
     * 查询店铺利润报表。
     *
     * @return 店铺利润
     */
    Map<String, Object> storeProfit();

    /**
     * 查询利润趋势。
     *
     * @return 趋势数据
     */
    Map<String, Object> profitTrend();

    /**
     * 查询亏损预警。
     *
     * @return 亏损 SKU 列表
     */
    List<Map<String, Object>> lossWarnings();

    /**
     * 查询应收账款。
     *
     * @return 应收列表
     */
    List<Map<String, Object>> receivables();

    /**
     * 分页查询 VAT 申报记录。
     *
     * @param query 分页参数
     * @return VAT 分页结果
     */
    PageResult<FinanceVatRecord> pageVat(PageQuery query);

    /**
     * 生成 VAT 申报数据。
     *
     * @param request 生成请求
     * @return 生成结果
     */
    Map<String, Object> generateVat(VatGenerateRequest request);

    /**
     * 导出 VAT 申报表。
     *
     * @param id VAT记录ID
     * @return 导出结果
     */
    Map<String, Object> exportVat(Long id);

    /**
     * 分页查询资金流水。
     *
     * @param query 分页参数
     * @return 资金流水分页结果
     */
    PageResult<FinanceCashFlow> pageCashFlow(FinanceCashFlowPageQuery query);

    /**
     * 查询现金流预测。
     *
     * @return 预测结果
     */
    Map<String, Object> cashFlowForecast();

    /**
     * 发起付款申请。
     *
     * @param id 应付账款ID
     * @return 申请结果
     */
    Map<String, Object> applyPayable(Long id);

    /**
     * 审批付款申请。
     *
     * @param id 应付账款ID
     */
    void approvePayable(Long id);

    /**
     * 确认应付账款已付款。
     *
     * @param id 应付账款ID
     */
    void markPayablePaid(Long id);

    /**
     * 查询经营总览。
     *
     * @return 经营总览
     */
    Map<String, Object> dashboardOverview();

    /**
     * 查询今日实时数据。
     *
     * @return 实时数据
     */
    Map<String, Object> dashboardRealtime();

    /**
     * 查询销售趋势。
     *
     * @return 销售趋势
     */
    Map<String, Object> salesTrend();

    /**
     * 查询平台销售对比。
     *
     * @return 平台对比
     */
    Map<String, Object> platformCompare();

    /**
     * 查询库存健康报告。
     *
     * @return 库存健康
     */
    Map<String, Object> inventoryHealth();

    /**
     * 查询库存周转率。
     *
     * @return 周转率
     */
    Map<String, Object> inventoryTurnover();

    /**
     * 查询补货建议。
     *
     * @return 补货建议列表
     */
    List<Map<String, Object>> reorderSuggestions();

    /**
     * 将补货建议转采购申请。
     *
     * @return 转换结果
     */
    Map<String, Object> reorderToPurchase(Map<String, Object> suggestion);

    /**
     * 预测指定 SKU 日销量。
     *
     * @param skuId SKU ID
     * @return 预测结果
     */
    Map<String, Object> forecastSku(Long skuId);

    /**
     * 查询 KPI 看板。
     *
     * @return KPI 看板
     */
    Map<String, Object> kpiDashboard();

    /**
     * 查询 KPI 趋势。
     *
     * @return KPI 趋势
     */
    Map<String, Object> kpiTrend();

    /**
     * 查询 KPI 阈值配置。
     *
     * @return 阈值列表
     */
    List<BiKpiThreshold> kpiThresholds();

    /**
     * 保存 KPI 阈值配置。
     *
     * @param thresholds 阈值列表
     */
    void saveKpiThresholds(List<BiKpiThreshold> thresholds);

    /**
     * 执行 AI 自然语言查询。
     *
     * @param request 查询请求
     * @return 分析结果
     */
    Map<String, Object> aiQuery(AiQueryRequest request);

    /**
     * 查询 AI 快捷分析模板。
     *
     * @return 模板列表
     */
    List<Map<String, Object>> aiTemplates();

    /**
     * 导出 BI 报表。
     *
     * @return 导出结果
     */
    Map<String, Object> exportReport();
}
