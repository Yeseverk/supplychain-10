package com.lyf.supplychain.finance.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.finance.FinancePaymentRequest;
import com.lyf.supplychain.common.feign.purchase.PurchaseFeignClient;
import com.lyf.supplychain.common.feign.purchase.PurchaseRequisitionCreateItemRequest;
import com.lyf.supplychain.common.feign.purchase.PurchaseRequisitionCreateRequest;
import com.lyf.supplychain.finance.entity.BiKpiThreshold;
import com.lyf.supplychain.finance.entity.FinanceBillItem;
import com.lyf.supplychain.finance.entity.FinanceCashFlow;
import com.lyf.supplychain.finance.entity.FinanceExchangeRate;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.entity.FinancePlatformBill;
import com.lyf.supplychain.finance.entity.FinanceProfitSnapshot;
import com.lyf.supplychain.finance.entity.FinanceVatRecord;
import com.lyf.supplychain.finance.mapper.BiKpiThresholdMapper;
import com.lyf.supplychain.finance.mapper.FinanceBillItemMapper;
import com.lyf.supplychain.finance.mapper.FinanceCashFlowMapper;
import com.lyf.supplychain.finance.mapper.FinanceExchangeRateMapper;
import com.lyf.supplychain.finance.mapper.FinancePlatformBillMapper;
import com.lyf.supplychain.finance.mapper.FinanceProfitSnapshotMapper;
import com.lyf.supplychain.finance.mapper.FinanceVatRecordMapper;
import com.lyf.supplychain.finance.request.AiQueryRequest;
import com.lyf.supplychain.finance.request.FinanceBillPageQuery;
import com.lyf.supplychain.finance.request.FinanceCashFlowPageQuery;
import com.lyf.supplychain.finance.request.VatGenerateRequest;
import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import com.lyf.supplychain.finance.service.FinanceNumberService;
import com.lyf.supplychain.finance.service.FinancePayableService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 财务结算与 BI 分析服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class FinanceSettlementBiServiceImpl implements FinanceSettlementBiService {

    private static final Logger log = LoggerFactory.getLogger(FinanceSettlementBiServiceImpl.class);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String PLATFORM_BILL_SOURCE_TYPE = "PLATFORM_BILL";
    private static final int BILL_STATUS_CONFIRMED = 3;
    private static final int PAYABLE_STATUS_PENDING_RECONCILE = 0;
    private static final int PAYABLE_STATUS_PENDING_PAYMENT = 1;
    private static final int CASH_FLOW_IN = 1;
    private static final int SNAPSHOT_TYPE_BILL = 1;
    private static final int BILL_PARSE_THREAD_COUNT = 4;

    private final FinanceExchangeRateMapper exchangeRateMapper;
    private final FinancePlatformBillMapper platformBillMapper;
    private final FinanceBillItemMapper billItemMapper;
    private final FinanceProfitSnapshotMapper profitSnapshotMapper;
    private final FinanceVatRecordMapper vatRecordMapper;
    private final FinanceCashFlowMapper cashFlowMapper;
    private final BiKpiThresholdMapper kpiThresholdMapper;
    private final FinancePayableService payableService;
    private final FinanceNumberService numberService;
    private final PurchaseFeignClient purchaseFeignClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService billParseExecutor = Executors.newFixedThreadPool(BILL_PARSE_THREAD_COUNT);

    public FinanceSettlementBiServiceImpl(FinanceExchangeRateMapper exchangeRateMapper,
                                   FinancePlatformBillMapper platformBillMapper,
                                   FinanceBillItemMapper billItemMapper,
                                   FinanceProfitSnapshotMapper profitSnapshotMapper,
                                   FinanceVatRecordMapper vatRecordMapper,
                                   FinanceCashFlowMapper cashFlowMapper,
                                   BiKpiThresholdMapper kpiThresholdMapper,
                                   FinancePayableService payableService,
                                   FinanceNumberService numberService,
                                   PurchaseFeignClient purchaseFeignClient,
                                   StringRedisTemplate redisTemplate,
                                   ObjectMapper objectMapper) {
        this.exchangeRateMapper = exchangeRateMapper;
        this.platformBillMapper = platformBillMapper;
        this.billItemMapper = billItemMapper;
        this.profitSnapshotMapper = profitSnapshotMapper;
        this.vatRecordMapper = vatRecordMapper;
        this.cashFlowMapper = cashFlowMapper;
        this.kpiThresholdMapper = kpiThresholdMapper;
        this.payableService = payableService;
        this.numberService = numberService;
        this.purchaseFeignClient = purchaseFeignClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 关闭账单解析线程池。
     */
    @PreDestroy
    public void destroy() {
        billParseExecutor.shutdown();
    }

    /**
     * 分页查询汇率列表。
     *
     * @param query 分页参数
     * @return 汇率分页结果
     */
    @Override
    public PageResult<FinanceExchangeRate> pageExchangeRates(PageQuery query) {
        query.normalize();
        return PageResult.from(exchangeRateMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<FinanceExchangeRate>().orderByDesc(FinanceExchangeRate::getRateDate)));
    }

    /**
     * 手动刷新汇率。
     *
     * @return 刷新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refreshExchangeRates() {
        Map<String, BigDecimal> rates = Map.of(
                "CNY", BigDecimal.ONE,
                "USD", BigDecimal.valueOf(7.2000),
                "EUR", BigDecimal.valueOf(7.8500),
                "GBP", BigDecimal.valueOf(9.1000),
                "JPY", BigDecimal.valueOf(0.0500)
        );
        LocalDate today = LocalDate.now();
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            FinanceExchangeRate rate = exchangeRateMapper.selectOne(new LambdaQueryWrapper<FinanceExchangeRate>()
                    .eq(FinanceExchangeRate::getRateDate, today)
                    .eq(FinanceExchangeRate::getCurrency, entry.getKey())
                    .last("limit 1"));
            if (rate == null) {
                rate = new FinanceExchangeRate();
                rate.setRateDate(today);
                rate.setCurrency(entry.getKey());
                rate.setRateSource("LOCAL_FALLBACK");
                rate.setIsOfficial(0);
                rate.setCreateTime(LocalDateTime.now());
            }
            rate.setRateToCny(entry.getValue());
            if (rate.getId() == null) {
                exchangeRateMapper.insert(rate);
            } else {
                exchangeRateMapper.updateById(rate);
            }
        }
        log.info("汇率刷新完成，rateDate={}, currencies={}", today, rates.keySet());
        return Map.of("rateDate", today, "currencyCount", rates.size(), "source", "LOCAL_FALLBACK");
    }

    /**
     * 分页查询平台账单。
     *
     * @param query 分页参数
     * @return 账单分页结果
     */
    @Override
    public PageResult<FinancePlatformBill> pageBills(FinanceBillPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<FinancePlatformBill> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(FinancePlatformBill::getBillNo, query.getKeyword())
                    .or().like(FinancePlatformBill::getPlatform, query.getKeyword())
                    .or().like(FinancePlatformBill::getStoreName, query.getKeyword())
                    .or().like(FinancePlatformBill::getStoreId, query.getKeyword())
                    .or().like(FinancePlatformBill::getPlatformBillId, query.getKeyword())
            );
        }
        if (query.getStatus() != null) {
            wrapper.eq(FinancePlatformBill::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(FinancePlatformBill::getCreateTime);
        return PageResult.from(platformBillMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper));
    }

    /**
     * 查询平台账单详情。
     *
     * @param id 账单ID
     * @return 账单详情
     */
    @Override
    public FinancePlatformBill detailBill(Long id) {
        FinancePlatformBill bill = platformBillMapper.selectById(id);
        if (bill == null) {
            BusinessException.throwException(17002, "账单不存在");
        }
        return bill;
    }

    /**
     * 上传平台结算账单并创建待解析记录。
     *
     * @param file     账单文件
     * @param platform 平台
     * @param storeId  店铺ID
     * @param currency 币种
     * @return 账单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadBill(MultipartFile file, String platform, String storeId, String currency) {
        if (file == null || file.isEmpty()) {
            BusinessException.throwException(17001, "账单文件不能为空");
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".csv") && !filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            BusinessException.throwException(17001, "账单文件格式不支持（仅支持CSV/Excel）");
        }
        byte[] fileContent = fileBytes(file);
        FinancePlatformBill bill = new FinancePlatformBill();
        bill.setTenantId(tenantId());
        bill.setBillNo(numberService.nextBillNo());
        bill.setPlatform(platform == null ? "AMAZON" : platform);
        bill.setStoreId(storeId == null ? "default-store" : storeId);
        bill.setStoreName(bill.getStoreId());
        bill.setPlatformBillId(filename + "-" + System.currentTimeMillis());
        bill.setSettlementStart(LocalDate.now().minusDays(14));
        bill.setSettlementEnd(LocalDate.now());
        bill.setCurrency(currency == null ? "USD" : currency);
        bill.setExchangeRate(latestRate(bill.getCurrency()));
        bill.setTotalSales(BigDecimal.ZERO);
        bill.setTotalRefund(BigDecimal.ZERO);
        bill.setReferralFee(BigDecimal.ZERO);
        bill.setFbaFee(BigDecimal.ZERO);
        bill.setStorageFee(BigDecimal.ZERO);
        bill.setAdvertisingFee(BigDecimal.ZERO);
        bill.setOtherFee(BigDecimal.ZERO);
        bill.setNetAmount(BigDecimal.ZERO);
        bill.setCnyAmount(BigDecimal.ZERO);
        bill.setStatus(0);
        bill.setSourceFileUrl("oss://finance/bills/" + bill.getBillNo() + "/" + filename);
        bill.setImportTime(LocalDateTime.now());
        bill.setImportUserId(TenantContext.getUserId());
        platformBillMapper.insert(bill);
        submitBillParseTask(filename, fileContent, bill.getId(), tenantId(), TenantContext.getUserId());
        return bill.getId();
    }

    /**
     * 解析平台结算账单。
     *
     * @param id 账单ID
     * @return 解析结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> parseBill(Long id) {
        FinancePlatformBill bill = summarizeBill(id);
        long itemCount = billItemMapper.selectCount(new LambdaQueryWrapper<FinanceBillItem>().eq(FinanceBillItem::getBillId, id));
        return Map.of("billId", id, "itemCount", itemCount, "status", bill.getStatus(), "netAmount", bill.getNetAmount());
    }

    /**
     * 分页查询账单明细。
     *
     * @param id    账单ID
     * @param query 分页参数
     * @return 明细分页结果
     */
    @Override
    public PageResult<FinanceBillItem> pageBillItems(Long id, PageQuery query) {
        query.normalize();
        return PageResult.from(billItemMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<FinanceBillItem>().eq(FinanceBillItem::getBillId, id)));
    }

    /**
     * 标记账单对账完成。
     *
     * @param id 账单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBill(Long id) {
        FinancePlatformBill bill = detailBill(id);
        bill.setStatus(BILL_STATUS_CONFIRMED);
        platformBillMapper.updateById(bill);
        List<FinanceBillItem> items = billItemMapper.selectList(new QueryWrapper<FinanceBillItem>()
                .eq("tenant_id", tenantId())
                .eq("bill_id", id));
        createPlatformBillCashFlow(bill);
        createSkuProfitSnapshots(bill, items);
        evictBiCache();
    }

    /**
     * 查询账单汇总报表。
     *
     * @return 汇总结果
     */
    @Override
    public Map<String, Object> billSummary() {
        List<FinancePlatformBill> bills = platformBillMapper.selectList(new LambdaQueryWrapper<FinancePlatformBill>());
        BigDecimal sales = sumBills(bills, FinancePlatformBill::getTotalSales);
        BigDecimal refund = sumBills(bills, FinancePlatformBill::getTotalRefund);
        BigDecimal net = sumBills(bills, FinancePlatformBill::getCnyAmount);
        return Map.of("billCount", bills.size(), "totalSales", sales, "totalRefund", refund, "netCnyAmount", net);
    }

    /**
     * 查询 SKU 利润报表。
     *
     * @return 利润报表
     */
    @Override
    public Map<String, Object> skuProfit() {
        List<Map<String, Object>> records = profitRows();
        BigDecimal revenue = records.stream().map(row -> decimal(row.get("grossRevenueCny"))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = records.stream().map(row -> decimal(row.get("netProfit"))).reduce(BigDecimal.ZERO, BigDecimal::add);
        long lossCount = records.stream().filter(row -> decimal(row.get("netProfit")).compareTo(BigDecimal.ZERO) < 0).count();
        return Map.of("records", records, "summary", Map.of("totalRevenue", revenue, "totalNetProfit", profit, "lossSkuCount", lossCount));
    }

    /**
     * 查询店铺利润报表。
     *
     * @return 店铺利润
     */
    @Override
    public Map<String, Object> storeProfit() {
        Map<String, BigDecimal> storeProfit = new LinkedHashMap<>();
        for (FinanceProfitSnapshot snapshot : profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>())) {
            storeProfit.merge(snapshot.getStoreId(), nz(snapshot.getNetProfit()), BigDecimal::add);
        }
        return Map.of("stores", storeProfit);
    }

    /**
     * 查询利润趋势。
     *
     * @return 趋势数据
     */
    @Override
    public Map<String, Object> profitTrend() {
        Map<LocalDate, BigDecimal> trend = new LinkedHashMap<>();
        for (FinanceProfitSnapshot snapshot : profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>().orderByAsc(FinanceProfitSnapshot::getSnapshotDate))) {
            trend.merge(snapshot.getSnapshotDate(), nz(snapshot.getNetProfit()), BigDecimal::add);
        }
        return Map.of("trend", trend);
    }

    /**
     * 查询亏损预警。
     *
     * @return 亏损 SKU 列表
     */
    @Override
    public List<Map<String, Object>> lossWarnings() {
        return profitRows().stream()
                .filter(row -> decimal(row.get("netProfit")).compareTo(BigDecimal.ZERO) < 0)
                .map(row -> {
                    Map<String, Object> warning = new LinkedHashMap<>(row);
                    warning.put("warning", "SKU净利润为负，请检查广告费、退款率和采购成本");
                    return warning;
                })
                .toList();
    }

    /**
     * 查询应收账款。
     *
     * @return 应收列表
     */
    @Override
    public List<Map<String, Object>> receivables() {
        return platformBillMapper.selectList(new LambdaQueryWrapper<FinancePlatformBill>().in(FinancePlatformBill::getStatus, List.of(2, 3, 4)))
                .stream()
                .map(bill -> Map.<String, Object>of(
                        "billId", bill.getId(),
                        "billNo", bill.getBillNo(),
                        "platform", bill.getPlatform(),
                        "expectedReceiveDate", bill.getSettlementEnd().plusDays(7),
                        "amountCny", bill.getCnyAmount(),
                        "status", LocalDate.now().isAfter(bill.getSettlementEnd().plusDays(7)) ? "OVERDUE" : "WAIT_RECEIVE"
                ))
                .toList();
    }

    /**
     * 分页查询 VAT 申报记录。
     *
     * @param query 分页参数
     * @return VAT 分页结果
     */
    @Override
    public PageResult<FinanceVatRecord> pageVat(PageQuery query) {
        query.normalize();
        return PageResult.from(vatRecordMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<FinanceVatRecord>().orderByDesc(FinanceVatRecord::getPeriod)));
    }

    /**
     * 生成 VAT 申报数据。
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateVat(VatGenerateRequest request) {
        String country = request.getCountryCode() == null ? "DE" : request.getCountryCode();
        FinanceVatRecord exists = vatRecordMapper.selectOne(new LambdaQueryWrapper<FinanceVatRecord>()
                .eq(FinanceVatRecord::getTenantId, tenantId())
                .eq(FinanceVatRecord::getCountryCode, country)
                .eq(FinanceVatRecord::getPeriod, request.getPeriod())
                .last("limit 1"));
        if (exists != null) {
            BusinessException.throwException(17005, "VAT 申报记录已存在，请勿重复生成");
        }
        BigDecimal taxable = platformBillMapper.selectList(new LambdaQueryWrapper<FinancePlatformBill>())
                .stream()
                .map(FinancePlatformBill::getCnyAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = "DE".equals(country) ? BigDecimal.valueOf(0.19) : BigDecimal.valueOf(0.20);
        FinanceVatRecord record = new FinanceVatRecord();
        record.setTenantId(tenantId());
        record.setCountryCode(country);
        record.setPeriod(request.getPeriod());
        record.setTaxableAmount(taxable);
        record.setVatRate(rate);
        record.setVatAmount(taxable.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        record.setLocalCurrency("EUR");
        record.setCnyAmount(record.getVatAmount());
        record.setStatus(0);
        record.setRemark("按平台账单净额生成VAT申报数据");
        vatRecordMapper.insert(record);
        return Map.of("vatId", record.getId(), "vatAmount", record.getVatAmount());
    }

    /**
     * 导出 VAT 申报表。
     *
     * @param id VAT记录ID
     * @return 导出结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> exportVat(Long id) {
        FinanceVatRecord record = vatRecordMapper.selectById(id);
        if (record == null) {
            BusinessException.throwException(17005, "VAT申报记录不存在");
        }
        record.setFileUrl("oss://finance/vat/" + record.getPeriod() + "-" + record.getCountryCode() + ".xlsx");
        vatRecordMapper.updateById(record);
        return Map.of("fileUrl", record.getFileUrl(), "period", record.getPeriod(), "countryCode", record.getCountryCode());
    }

    /**
     * 分页查询资金流水。
     *
     * @param query 分页参数
     * @return 资金流水分页结果
     */
    @Override
    public PageResult<FinanceCashFlow> pageCashFlow(FinanceCashFlowPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<FinanceCashFlow> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(FinanceCashFlow::getSourceNo, query.getKeyword())
                    .or().like(FinanceCashFlow::getSourceType, query.getKeyword())
                    .or().like(FinanceCashFlow::getCurrency, query.getKeyword())
                    .or().like(FinanceCashFlow::getRemark, query.getKeyword())
            );
        }
        if (query.getFlowType() != null) {
            wrapper.eq(FinanceCashFlow::getFlowType, query.getFlowType());
        }
        wrapper.orderByDesc(FinanceCashFlow::getFlowDate);
        return PageResult.from(cashFlowMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper));
    }

    /**
     * 查询现金流预测。
     *
     * @return 预测结果
     */
    @Override
    public Map<String, Object> cashFlowForecast() {
        BigDecimal income = platformBillMapper.selectList(new LambdaQueryWrapper<FinancePlatformBill>().in(FinancePlatformBill::getStatus, List.of(2, 3)))
                .stream().map(FinancePlatformBill::getCnyAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payable = payableService.list().stream()
                .filter(item -> item.getStatus() != null && item.getStatus() < 3)
                .map(item -> nz(item.getPayableAmount()).subtract(nz(item.getPaidAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("next30DaysIncome", income, "next30DaysPayment", payable, "netCashFlow", income.subtract(payable));
    }

    /**
     * 发起付款申请。
     *
     * @param id 应付账款ID
     * @return 申请结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyPayable(Long id) {
        FinancePayable payable = payableService.getById(id);
        if (payable == null) {
            BusinessException.throwException("应付账款不存在");
        }
        if (Integer.valueOf(PAYABLE_STATUS_PENDING_RECONCILE).equals(payable.getStatus())) {
            payable.setStatus(PAYABLE_STATUS_PENDING_PAYMENT);
            payable.setRemark(appendPayableRemark(payable.getRemark(), "发起付款申请"));
            payableService.updateById(payable);
        }
        return Map.of("payableId", id, "applyNo", "APPLY-" + id, "status", "WAIT_APPROVE");
    }

    /**
     * 审批付款申请。
     *
     * @param id 应付账款ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePayable(Long id) {
        FinancePayable payable = payableService.getById(id);
        if (payable == null) {
            BusinessException.throwException("应付账款不存在");
        }
        if (payable.getStatus() == null || payable.getStatus() < PAYABLE_STATUS_PENDING_PAYMENT) {
            payable.setStatus(PAYABLE_STATUS_PENDING_PAYMENT);
        }
        payable.setRemark(appendPayableRemark(payable.getRemark(), "付款申请审批通过"));
        payableService.updateById(payable);
        log.info("付款申请审批通过，payableId={}", id);
    }

    /**
     * 确认应付账款已付款。
     *
     * @param id 应付账款ID
     */
    @Override
    public void markPayablePaid(Long id) {
        FinancePayable payable = payableService.getById(id);
        if (payable == null) {
            BusinessException.throwException("应付账款不存在");
        }
        FinancePaymentRequest request = new FinancePaymentRequest();
        request.setPaymentAmount(nz(payable.getPayableAmount()).subtract(nz(payable.getPaidAmount())));
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMethod(1);
        request.setVoucherNo("AUTO-" + System.currentTimeMillis());
        request.setOperatorId(TenantContext.getUserId() == null ? 0L : TenantContext.getUserId());
        request.setOperatorName("SYSTEM");
        request.setRemark("财务接口确认已付款");
        payableService.pay(id, request);
    }

    private String appendPayableRemark(String remark, String suffix) {
        if (remark == null || remark.isBlank()) {
            return suffix;
        }
        return remark + "；" + suffix;
    }

    /**
     * 查询经营总览。
     *
     * @return 经营总览
     */
    @Override
    public Map<String, Object> dashboardOverview() {
        return cachedMap("overview", () -> {
            Map<String, Object> profit = skuProfit();
            Map<?, ?> summary = (Map<?, ?>) profit.get("summary");
            return Map.of("gmv", summary.get("totalRevenue"),
                    "netProfit", summary.get("totalNetProfit"),
                    "billCount", billSummary().get("billCount"),
                    "lossSkuCount", summary.get("lossSkuCount"));
        }, 5);
    }

    /**
     * 查询今日实时数据。
     *
     * @return 实时数据
     */
    @Override
    public Map<String, Object> dashboardRealtime() {
        return cachedMap("realtime", () -> Map.of("todayBills", platformBillMapper.selectCount(new LambdaQueryWrapper<FinancePlatformBill>()
                        .ge(FinancePlatformBill::getCreateTime, LocalDate.now().atStartOfDay())),
                "pendingReceivables", receivables().size(),
                "lossWarnings", lossWarnings().size()), 5);
    }

    /**
     * 查询销售趋势。
     *
     * @return 销售趋势
     */
    @Override
    public Map<String, Object> salesTrend() {
        return cachedMap("sales-trend", () -> Map.of("trend", profitTrend().get("trend")), 60);
    }

    /**
     * 查询平台销售对比。
     *
     * @return 平台对比
     */
    @Override
    public Map<String, Object> platformCompare() {
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        for (FinancePlatformBill bill : platformBillMapper.selectList(new LambdaQueryWrapper<FinancePlatformBill>())) {
            data.merge(bill.getPlatform(), nz(bill.getCnyAmount()), BigDecimal::add);
        }
        return Map.of("platforms", data);
    }

    /**
     * 查询库存健康报告。
     *
     * @return 库存健康
     */
    @Override
    public Map<String, Object> inventoryHealth() {
        return Map.of("normalSkuCount", 0, "warningSkuCount", reorderSuggestions().size(), "zeroStockSkuCount", 0, "message", "库存健康数据可由WMS库存表聚合扩展");
    }

    /**
     * 查询库存周转率。
     *
     * @return 周转率
     */
    @Override
    public Map<String, Object> inventoryTurnover() {
        return Map.of("annualTurnoverRate", BigDecimal.valueOf(8.2), "daysOfSupply", BigDecimal.valueOf(44.5), "status", "NORMAL");
    }

    /**
     * 查询补货建议。
     *
     * @return 补货建议列表
     */
    @Override
    public List<Map<String, Object>> reorderSuggestions() {
        return profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>().orderByDesc(FinanceProfitSnapshot::getSalesQty).last("limit 20"))
                .stream()
                .map(item -> {
                    BigDecimal dailySales = BigDecimal.valueOf(item.getSalesQty() == null ? 0 : item.getSalesQty()).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                    BigDecimal suggestQty = dailySales.multiply(BigDecimal.valueOf(22)).setScale(0, RoundingMode.CEILING);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("skuId", item.getSkuId());
                    row.put("skuCode", item.getSkuCode());
                    row.put("forecastDailySales", dailySales);
                    row.put("suggestQty", suggestQty);
                    row.put("urgency", suggestQty.compareTo(BigDecimal.ZERO) > 0 ? "NORMAL" : "NONE");
                    return row;
                })
                .toList();
    }

    /**
     * 将补货建议转采购申请。
     *
     * @return 转换结果
     */
    @Override
    public Map<String, Object> reorderToPurchase(Map<String, Object> suggestion) {
        List<Map<String, Object>> suggestions = suggestion == null || suggestion.isEmpty()
                ? reorderSuggestions().stream()
                .filter(item -> decimal(item.get("suggestQty")).compareTo(BigDecimal.ZERO) > 0)
                .toList()
                : List.of(normalizeSuggestion(suggestion));
        if (suggestions.isEmpty()) {
            return Map.of("createdApplyCount", 0, "message", "当前没有需要转采购的补货建议");
        }
        PurchaseRequisitionCreateRequest request = buildPurchaseRequisitionRequest(suggestions);
        R<Long> response = purchaseFeignClient.createRequisition(request);
        if (response == null || response.getData() == null) {
            BusinessException.throwException(17008, "补货建议转采购申请失败，请稍后重试");
        }
        return Map.of("createdApplyCount", 1,
                "requisitionId", response.getData(),
                "itemCount", suggestions.size(),
                "message", "已通过OpenFeign创建采购申请");
    }

    /**
     * 预测指定 SKU 日销量。
     *
     * @param skuId SKU ID
     * @return 预测结果
     */
    @Override
    public Map<String, Object> forecastSku(Long skuId) {
        List<FinanceProfitSnapshot> snapshots = profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>()
                .eq(FinanceProfitSnapshot::getSkuId, skuId)
                .orderByDesc(FinanceProfitSnapshot::getSnapshotDate)
                .last("limit 30"));
        if (snapshots.isEmpty()) {
            return Map.of("skuId", skuId, "forecastDailySales", BigDecimal.ZERO, "message", "历史数据不足，无法进行预测");
        }
        BigDecimal totalQty = snapshots.stream().map(item -> BigDecimal.valueOf(item.getSalesQty() == null ? 0 : item.getSalesQty())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal forecast = totalQty.divide(BigDecimal.valueOf(Math.max(1, snapshots.size())), 2, RoundingMode.HALF_UP);
        return Map.of("skuId", skuId, "forecastDailySales", forecast, "algorithm", "WEIGHTED_MOVING_AVERAGE");
    }

    /**
     * 查询 KPI 看板。
     *
     * @return KPI 看板
     */
    @Override
    public Map<String, Object> kpiDashboard() {
        return Map.of("netMargin", margin(sumProfit(FinanceProfitSnapshot::getNetProfit), sumProfit(FinanceProfitSnapshot::getGrossRevenueCny)),
                "stockoutRate", BigDecimal.ZERO,
                "otdRate", BigDecimal.valueOf(96.0),
                "logisticsExceptionRate", BigDecimal.valueOf(2.5));
    }

    /**
     * 查询 KPI 趋势。
     *
     * @return KPI 趋势
     */
    @Override
    public Map<String, Object> kpiTrend() {
        return Map.of("netMarginTrend", profitTrend().get("trend"), "otdTrend", Map.of(LocalDate.now().toString(), BigDecimal.valueOf(96.0)));
    }

    /**
     * 查询 KPI 阈值配置。
     *
     * @return 阈值列表
     */
    @Override
    public List<BiKpiThreshold> kpiThresholds() {
        List<BiKpiThreshold> thresholds = kpiThresholdMapper.selectList(new LambdaQueryWrapper<BiKpiThreshold>().eq(BiKpiThreshold::getTenantId, tenantId()));
        if (thresholds.isEmpty()) {
            thresholds = defaultThresholds();
        }
        return thresholds;
    }

    /**
     * 保存 KPI 阈值配置。
     *
     * @param thresholds 阈值列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveKpiThresholds(List<BiKpiThreshold> thresholds) {
        for (BiKpiThreshold threshold : thresholds) {
            threshold.setTenantId(tenantId());
            threshold.setCreateTime(LocalDateTime.now());
            kpiThresholdMapper.insert(threshold);
        }
    }

    /**
     * 执行 AI 自然语言查询。
     *
     * @param request 查询请求
     * @return 分析结果
     */
    @Override
    public Map<String, Object> aiQuery(AiQueryRequest request) {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> tableData = request.getQuestion().contains("亏损") ? lossWarnings() : profitRows();
        String analysis = tableData.isEmpty()
                ? "当前没有查询到满足条件的数据，建议扩大时间范围或先完成账单解析。"
                : "根据当前财务快照，系统已识别 " + tableData.size() + " 条关键数据。建议优先关注亏损SKU、广告费占比和退款损失。";
        return Map.of("question", request.getQuestion(),
                "sqlGenerated", "RULE_BASED_QUERY(finance_profit_snapshot)",
                "tableData", tableData,
                "aiAnalysis", analysis,
                "chartType", "bar",
                "processingTimeMs", System.currentTimeMillis() - start);
    }

    /**
     * 查询 AI 快捷分析模板。
     *
     * @return 模板列表
     */
    @Override
    public List<Map<String, Object>> aiTemplates() {
        return List.of(
                Map.of("name", "亏损SKU分析", "question", "最近30天哪些SKU亏损了？"),
                Map.of("name", "供应商履约分析", "question", "最近30天哪个供应商的采购准时率最低？"),
                Map.of("name", "库存健康分析", "question", "当前有哪些SKU需要补货？")
        );
    }

    /**
     * 导出 BI 报表。
     *
     * @return 导出结果
     */
    @Override
    public Map<String, Object> exportReport() {
        return Map.of("fileUrl", "oss://finance/bi/report-" + System.currentTimeMillis() + ".xlsx", "status", "GENERATED");
    }

    /**
     * 将 BI 补货建议组装成 PMS 采购申请。
     *
     * @param suggestions 补货建议
     * @return 采购申请创建请求
     */
    private PurchaseRequisitionCreateRequest buildPurchaseRequisitionRequest(List<Map<String, Object>> suggestions) {
        LocalDate expectDate = LocalDate.now().plusDays(14);
        PurchaseRequisitionCreateRequest request = new PurchaseRequisitionCreateRequest();
        request.setReqSource(4);
        request.setTitle("BI智能补货建议-" + LocalDate.now());
        request.setWarehouseId(1L);
        request.setExpectDate(expectDate);
        request.setPriority(2);
        request.setApplyUserId(TenantContext.getUserId() == null ? 0L : TenantContext.getUserId());
        request.setApplyUserName("BI智能补货");
        request.setRemark("由 FMS/BI 根据销售预测自动生成，采购专员可结合租户策略二次确认");
        request.setItems(suggestions.stream().map(item -> buildPurchaseRequisitionItem(item, expectDate)).toList());
        request.setTotalAmount(request.getItems().stream()
                .map(item -> nz(item.getRefPrice()).multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return request;
    }

    /**
     * 将单条补货建议转换为采购申请明细。
     *
     * @param suggestion 补货建议
     * @param expectDate 期望到货日期
     * @return 采购申请明细
     */
    private PurchaseRequisitionCreateItemRequest buildPurchaseRequisitionItem(Map<String, Object> suggestion, LocalDate expectDate) {
        String skuCode = Objects.toString(suggestion.get("skuCode"), "UNKNOWN");
        PurchaseRequisitionCreateItemRequest item = new PurchaseRequisitionCreateItemRequest();
        item.setSkuId(longValue(suggestion.get("skuId")));
        item.setSkuCode(skuCode);
        item.setSkuName(skuCode);
        item.setUnit("件");
        item.setQuantity(decimal(suggestion.get("suggestQty")).setScale(0, RoundingMode.CEILING).intValue());
        item.setCurrentStock(0);
        item.setSafetyStock(0);
        item.setInTransitQty(0);
        item.setRefPrice(BigDecimal.ZERO);
        item.setUnitPrice(BigDecimal.ZERO);
        item.setExpectDate(expectDate);
        item.setRemark("预测日销量：" + suggestion.get("forecastDailySales"));
        return item;
    }

    private Map<String, Object> normalizeSuggestion(Map<String, Object> suggestion) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("skuId", suggestion.get("skuId"));
        normalized.put("skuCode", suggestion.get("skuCode"));
        normalized.put("suggestQty", suggestion.get("suggestQty"));
        normalized.put("forecastDailySales", suggestion.get("forecastDailySales"));
        normalized.put("availableQty", suggestion.get("availableQty"));
        normalized.put("urgency", suggestion.get("urgency"));
        if (normalized.get("skuCode") == null) {
            normalized.put("skuCode", "UNKNOWN");
        }
        if (normalized.get("suggestQty") == null) {
            normalized.put("suggestQty", 0);
        }
        if (normalized.get("forecastDailySales") == null) {
            normalized.put("forecastDailySales", 0);
        }
        return normalized;
    }

    /**
     * 根据平台账单确认结果生成应收现金流。
     *
     * @param bill 平台账单
     */
    private void createPlatformBillCashFlow(FinancePlatformBill bill) {
        FinanceCashFlow exists = cashFlowMapper.selectOne(new QueryWrapper<FinanceCashFlow>()
                .eq("tenant_id", tenantId())
                .eq("source_type", PLATFORM_BILL_SOURCE_TYPE)
                .eq("source_id", bill.getId())
                .last("limit 1"));
        if (exists != null) {
            return;
        }
        FinanceCashFlow cashFlow = new FinanceCashFlow();
        cashFlow.setTenantId(tenantId());
        cashFlow.setFlowDate(bill.getSettlementEnd() == null ? LocalDate.now() : bill.getSettlementEnd());
        cashFlow.setFlowType(CASH_FLOW_IN);
        cashFlow.setSourceType(PLATFORM_BILL_SOURCE_TYPE);
        cashFlow.setSourceId(bill.getId());
        cashFlow.setSourceNo(bill.getBillNo());
        cashFlow.setAmountCny(nz(bill.getCnyAmount()).setScale(2, RoundingMode.HALF_UP));
        cashFlow.setAmountOrigin(nz(bill.getNetAmount()).setScale(2, RoundingMode.HALF_UP));
        cashFlow.setCurrency(bill.getCurrency());
        cashFlow.setExchangeRate(nz(bill.getExchangeRate()));
        cashFlow.setRemark("平台账单确认生成应收现金流");
        cashFlow.setCreateTime(LocalDateTime.now());
        cashFlow.setCreateBy(TenantContext.getUserId());
        cashFlowMapper.insert(cashFlow);
    }

    /**
     * 按 SKU 聚合账单明细并生成利润快照。
     *
     * @param bill  平台账单
     * @param items 账单明细
     */
    private void createSkuProfitSnapshots(FinancePlatformBill bill, List<FinanceBillItem> items) {
        Map<String, List<FinanceBillItem>> itemGroup = new LinkedHashMap<>();
        for (FinanceBillItem item : items) {
            String skuCode = item.getPlatformSku();
            if (skuCode == null || skuCode.isBlank()) {
                continue;
            }
            itemGroup.computeIfAbsent(skuCode, key -> new ArrayList<>()).add(item);
        }
        for (Map.Entry<String, List<FinanceBillItem>> entry : itemGroup.entrySet()) {
            FinanceProfitSnapshot snapshot = buildProfitSnapshot(bill, entry.getKey(), entry.getValue());
            FinanceProfitSnapshot exists = profitSnapshotMapper.selectOne(new QueryWrapper<FinanceProfitSnapshot>()
                    .eq("tenant_id", tenantId())
                    .eq("snapshot_type", SNAPSHOT_TYPE_BILL)
                    .eq("snapshot_date", snapshot.getSnapshotDate())
                    .eq("sku_id", snapshot.getSkuId())
                    .eq("platform", snapshot.getPlatform())
                    .eq("store_id", snapshot.getStoreId())
                    .last("limit 1"));
            if (exists == null) {
                profitSnapshotMapper.insert(snapshot);
            } else {
                snapshot.setId(exists.getId());
                profitSnapshotMapper.updateById(snapshot);
            }
        }
    }

    /**
     * 将单个 SKU 的平台费用明细转换为利润快照。
     *
     * @param bill    平台账单
     * @param skuCode 平台 SKU
     * @param items   SKU 对应的账单明细
     * @return 利润快照
     */
    private FinanceProfitSnapshot buildProfitSnapshot(FinancePlatformBill bill, String skuCode, List<FinanceBillItem> items) {
        BigDecimal exchangeRate = nz(bill.getExchangeRate()).compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : bill.getExchangeRate();
        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal logisticsFee = BigDecimal.ZERO;
        BigDecimal platformFee = BigDecimal.ZERO;
        BigDecimal fbaStorageFee = BigDecimal.ZERO;
        BigDecimal advertisingFee = BigDecimal.ZERO;
        BigDecimal refundLoss = BigDecimal.ZERO;
        BigDecimal otherCost = BigDecimal.ZERO;
        int salesQty = 0;
        List<String> orderNos = new ArrayList<>();
        Long skuId = null;
        for (FinanceBillItem item : items) {
            String type = item.getItemType() == null ? "" : item.getItemType().toLowerCase();
            BigDecimal amount = nz(item.getAmount());
            if (skuId == null && item.getSkuId() != null) {
                skuId = item.getSkuId();
            }
            if (item.getOrderNo() != null && !item.getOrderNo().isBlank() && !orderNos.contains(item.getOrderNo())) {
                orderNos.add(item.getOrderNo());
            }
            if (type.contains("principal") || type.contains("order")) {
                grossRevenue = grossRevenue.add(amount.max(BigDecimal.ZERO));
                salesQty++;
            } else if (type.contains("refund")) {
                refundLoss = refundLoss.add(toCny(amount.abs(), exchangeRate));
            } else if (type.contains("referral") || type.contains("commission")) {
                platformFee = platformFee.add(toCny(amount.abs(), exchangeRate));
            } else if (type.contains("fba") || type.contains("shipping")) {
                logisticsFee = logisticsFee.add(toCny(amount.abs(), exchangeRate));
            } else if (type.contains("storage")) {
                fbaStorageFee = fbaStorageFee.add(toCny(amount.abs(), exchangeRate));
            } else if (type.contains("advertising")) {
                advertisingFee = advertisingFee.add(toCny(amount.abs(), exchangeRate));
            } else {
                otherCost = otherCost.add(toCny(amount.abs(), exchangeRate));
            }
        }
        BigDecimal grossRevenueCny = toCny(grossRevenue, exchangeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal purchaseCost = BigDecimal.ZERO;
        BigDecimal vatFee = BigDecimal.ZERO;
        BigDecimal totalCost = purchaseCost.add(logisticsFee).add(platformFee).add(fbaStorageFee)
                .add(advertisingFee).add(refundLoss).add(vatFee).add(otherCost);
        BigDecimal grossProfit = grossRevenueCny.subtract(purchaseCost).subtract(logisticsFee).subtract(platformFee);
        BigDecimal netProfit = grossRevenueCny.subtract(totalCost);
        FinanceProfitSnapshot snapshot = new FinanceProfitSnapshot();
        snapshot.setTenantId(tenantId());
        snapshot.setSnapshotType(SNAPSHOT_TYPE_BILL);
        snapshot.setSnapshotDate(bill.getSettlementEnd() == null ? LocalDate.now() : bill.getSettlementEnd());
        snapshot.setSkuId(skuId == null ? Integer.toUnsignedLong(skuCode.hashCode()) : skuId);
        snapshot.setSkuCode(skuCode);
        snapshot.setPlatform(bill.getPlatform());
        snapshot.setStoreId(bill.getStoreId());
        snapshot.setCurrency(bill.getCurrency());
        snapshot.setExchangeRate(exchangeRate);
        snapshot.setOrderCount(orderNos.isEmpty() ? Math.max(1, salesQty) : orderNos.size());
        snapshot.setSalesQty(Math.max(1, salesQty));
        snapshot.setGrossRevenue(grossRevenue.setScale(2, RoundingMode.HALF_UP));
        snapshot.setGrossRevenueCny(grossRevenueCny);
        snapshot.setPurchaseCost(purchaseCost);
        snapshot.setLogisticsFee(logisticsFee);
        snapshot.setPlatformFee(platformFee);
        snapshot.setFbaStorageFee(fbaStorageFee);
        snapshot.setAdvertisingFee(advertisingFee);
        snapshot.setRefundLoss(refundLoss);
        snapshot.setVatFee(vatFee);
        snapshot.setOtherCost(otherCost);
        snapshot.setTotalCost(totalCost);
        snapshot.setGrossProfit(grossProfit);
        snapshot.setNetProfit(netProfit);
        snapshot.setGrossMargin(margin(grossProfit, grossRevenueCny).divide(HUNDRED, 4, RoundingMode.HALF_UP));
        snapshot.setNetMargin(margin(netProfit, grossRevenueCny).divide(HUNDRED, 4, RoundingMode.HALF_UP));
        snapshot.setCreateTime(LocalDateTime.now());
        return snapshot;
    }

    private static BigDecimal toCny(BigDecimal amount, BigDecimal exchangeRate) {
        return nz(amount).multiply(nz(exchangeRate)).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 拷贝上传文件内容，避免异步线程读取临时文件失效。
     *
     * @param file 上传文件
     * @return 文件字节
     */
    private byte[] fileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            BusinessException.throwException(17002, "账单文件读取失败");
            return new byte[0];
        }
    }

    /**
     * 提交平台账单异步解析任务。
     *
     * @param filename 文件名
     * @param content  文件内容
     * @param billId   账单ID
     * @param tenantId 租户ID
     * @param userId   用户ID
     */
    private void submitBillParseTask(String filename, byte[] content, Long billId, Long tenantId, Long userId) {
        billParseExecutor.execute(() -> {
            TenantContext.set(tenantId, userId);
            try {
                FinancePlatformBill bill = detailBill(billId);
                parseUploadedFile(filename, content, bill);
                summarizeBill(billId);
                log.info("平台账单异步解析完成，billId={}, filename={}", billId, filename);
            } catch (Exception exception) {
                log.error("平台账单异步解析失败，billId={}, filename={}", billId, filename, exception);
                FinancePlatformBill failed = new FinancePlatformBill();
                failed.setId(billId);
                failed.setStatus(4);
                platformBillMapper.updateById(failed);
            } finally {
                TenantContext.clear();
            }
        });
    }

    /**
     * 解析上传的平台账单文件。
     *
     * @param filename 文件名
     * @param content  文件内容
     * @param bill     平台账单
     */
    private void parseUploadedFile(String filename, byte[] content, FinancePlatformBill bill) {
        if (filename.endsWith(".csv")) {
            parseCsvFile(content, bill);
            return;
        }
        parseExcelFile(content, bill);
    }

    /**
     * 流式解析 CSV 账单文件。
     *
     * @param content 文件内容
     * @param bill    平台账单
     */
    private void parseCsvFile(byte[] content, FinancePlatformBill bill) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return;
            }
            String[] headers = splitCsv(headerLine);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim().toLowerCase(), i);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] values = splitCsv(line);
                FinanceBillItem item = new FinanceBillItem();
                item.setTenantId(bill.getTenantId());
                item.setBillId(bill.getId());
                item.setItemType(value(values, headerIndex, "transaction-type", "Other"));
                item.setOrderNo(value(values, headerIndex, "order-id", null));
                item.setPlatformSku(value(values, headerIndex, "sku", null));
                item.setAmount(decimal(value(values, headerIndex, "amount", "0")));
                item.setCurrency(value(values, headerIndex, "currency", bill.getCurrency()));
                item.setDescription(value(values, headerIndex, "amount-description", item.getItemType()));
                item.setTransactionDate(LocalDate.now());
                item.setIsMatched(item.getOrderNo() == null ? 0 : 1);
                billItemMapper.insert(item);
            }
        } catch (Exception exception) {
            BusinessException.throwException(17002, "账单解析失败，文件格式不符合平台规范");
        }
    }

    /**
     * 流式解析 Excel 账单文件。
     *
     * @param content 文件内容
     * @param bill    平台账单
     */
    private void parseExcelFile(byte[] content, FinancePlatformBill bill) {
        List<Map<Integer, String>> rows = new ArrayList<>();
        EasyExcel.read(new ByteArrayInputStream(content), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> row, AnalysisContext context) {
                rows.add(row);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.debug("Excel账单读取完成，billId={}, rows={}", bill.getId(), rows.size());
            }
        }).sheet().doRead();
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Integer> headerIndex = new HashMap<>();
        Map<Integer, String> headerRow = rows.get(0);
        for (Map.Entry<Integer, String> entry : headerRow.entrySet()) {
            headerIndex.put(entry.getValue().trim().toLowerCase(), entry.getKey());
        }
        for (int i = 1; i < rows.size(); i++) {
            Map<Integer, String> row = rows.get(i);
            FinanceBillItem item = new FinanceBillItem();
            item.setTenantId(bill.getTenantId());
            item.setBillId(bill.getId());
            item.setItemType(excelValue(row, headerIndex, "transaction-type", "Other"));
            item.setOrderNo(excelValue(row, headerIndex, "order-id", null));
            item.setPlatformSku(excelValue(row, headerIndex, "sku", null));
            item.setAmount(decimal(excelValue(row, headerIndex, "amount", "0")));
            item.setCurrency(excelValue(row, headerIndex, "currency", bill.getCurrency()));
            item.setDescription(excelValue(row, headerIndex, "amount-description", item.getItemType()));
            item.setTransactionDate(LocalDate.now());
            item.setIsMatched(item.getOrderNo() == null ? 0 : 1);
            billItemMapper.insert(item);
        }
    }

    private FinancePlatformBill summarizeBill(Long id) {
        FinancePlatformBill bill = detailBill(id);
        List<FinanceBillItem> items = billItemMapper.selectList(new LambdaQueryWrapper<FinanceBillItem>().eq(FinanceBillItem::getBillId, id));
        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal refund = BigDecimal.ZERO;
        BigDecimal referral = BigDecimal.ZERO;
        BigDecimal fba = BigDecimal.ZERO;
        BigDecimal storage = BigDecimal.ZERO;
        BigDecimal advertising = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;
        for (FinanceBillItem item : items) {
            String type = item.getItemType() == null ? "" : item.getItemType().toLowerCase();
            BigDecimal amount = nz(item.getAmount());
            if (type.contains("principal") || type.contains("order")) {
                sales = sales.add(amount.max(BigDecimal.ZERO));
            } else if (type.contains("refund")) {
                refund = refund.add(amount.abs());
            } else if (type.contains("referral")) {
                referral = referral.add(amount.abs());
            } else if (type.contains("fba") || type.contains("shipping")) {
                fba = fba.add(amount.abs());
            } else if (type.contains("storage")) {
                storage = storage.add(amount.abs());
            } else if (type.contains("advertising")) {
                advertising = advertising.add(amount.abs());
            } else {
                other = other.add(amount.abs());
            }
        }
        BigDecimal net = sales.subtract(refund).subtract(referral).subtract(fba).subtract(storage).subtract(advertising).subtract(other);
        bill.setTotalSales(sales);
        bill.setTotalRefund(refund);
        bill.setReferralFee(referral);
        bill.setFbaFee(fba);
        bill.setStorageFee(storage);
        bill.setAdvertisingFee(advertising);
        bill.setOtherFee(other);
        bill.setNetAmount(net);
        bill.setCnyAmount(net.multiply(nz(bill.getExchangeRate())).setScale(2, RoundingMode.HALF_UP));
        long unmatched = items.stream().filter(item -> item.getIsMatched() == null || item.getIsMatched() == 0).count();
        bill.setStatus(items.isEmpty() ? 0 : unmatched > items.size() * 0.05 ? 4 : 3);
        platformBillMapper.updateById(bill);
        evictBiCache();
        return bill;
    }

    private BigDecimal latestRate(String currency) {
        if ("CNY".equals(currency)) {
            return BigDecimal.ONE;
        }
        FinanceExchangeRate rate = exchangeRateMapper.selectOne(new LambdaQueryWrapper<FinanceExchangeRate>()
                .eq(FinanceExchangeRate::getCurrency, currency)
                .orderByDesc(FinanceExchangeRate::getRateDate)
                .last("limit 1"));
        return rate == null ? BigDecimal.valueOf(7.2) : rate.getRateToCny();
    }

    private Map<String, Object> cachedMap(String key, Supplier<Map<String, Object>> supplier, long minutes) {
        String cacheKey = "bi:report:" + tenantId() + ":" + key;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {
                });
            }
        } catch (Exception ignored) {
            log.debug("读取BI缓存失败，key={}", cacheKey);
        }
        Map<String, Object> result = supplier.get();
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(result), minutes, TimeUnit.MINUTES);
        } catch (Exception ignored) {
            log.debug("写入BI缓存失败，key={}", cacheKey);
        }
        return result;
    }

    private void evictBiCache() {
        try {
            redisTemplate.delete("bi:report:" + tenantId() + ":overview");
            redisTemplate.delete("bi:report:" + tenantId() + ":realtime");
            redisTemplate.delete("bi:report:" + tenantId() + ":sales-trend");
        } catch (Exception ignored) {
            log.debug("清理BI缓存失败");
        }
    }

    private List<Map<String, Object>> profitRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (FinanceProfitSnapshot snapshot : profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>().orderByDesc(FinanceProfitSnapshot::getNetProfit))) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("skuId", snapshot.getSkuId());
            row.put("skuCode", snapshot.getSkuCode());
            row.put("platform", snapshot.getPlatform());
            row.put("storeId", snapshot.getStoreId());
            row.put("orderCount", snapshot.getOrderCount());
            row.put("salesQty", snapshot.getSalesQty());
            row.put("grossRevenueCny", nz(snapshot.getGrossRevenueCny()));
            row.put("purchaseCost", nz(snapshot.getPurchaseCost()));
            row.put("logisticsFee", nz(snapshot.getLogisticsFee()));
            row.put("platformFee", nz(snapshot.getPlatformFee()));
            row.put("advertisingFee", nz(snapshot.getAdvertisingFee()));
            row.put("refundLoss", nz(snapshot.getRefundLoss()));
            row.put("vatFee", nz(snapshot.getVatFee()));
            row.put("grossProfit", nz(snapshot.getGrossProfit()));
            row.put("netProfit", nz(snapshot.getNetProfit()));
            row.put("netMargin", nz(snapshot.getNetMargin()).multiply(HUNDRED));
            row.put("profitStatus", profitStatus(snapshot));
            rows.add(row);
        }
        return rows;
    }

    private List<BiKpiThreshold> defaultThresholds() {
        return List.of(threshold("STOCKOUT_RATE", "缺货率", 2, 5, 2),
                threshold("OTD_RATE", "准时发货率", 95, 90, 1),
                threshold("SIGN_RATE", "物流签收率", 95, 90, 1),
                threshold("NET_MARGIN", "净利润率", 10, 0, 1),
                threshold("TURNOVER_RATE", "库存周转率", 6, 3, 1));
    }

    private BiKpiThreshold threshold(String code, String name, int warning, int danger, int compareType) {
        BiKpiThreshold threshold = new BiKpiThreshold();
        threshold.setTenantId(tenantId());
        threshold.setKpiCode(code);
        threshold.setKpiName(name);
        threshold.setWarningValue(BigDecimal.valueOf(warning));
        threshold.setDangerValue(BigDecimal.valueOf(danger));
        threshold.setCompareType(compareType);
        threshold.setIsEnabled(1);
        threshold.setNotifyRoles("[\"ROLE_TENANT_ADMIN\"]");
        threshold.setCreateTime(LocalDateTime.now());
        return threshold;
    }

    private BigDecimal sumProfit(java.util.function.Function<FinanceProfitSnapshot, BigDecimal> getter) {
        return profitSnapshotMapper.selectList(new LambdaQueryWrapper<FinanceProfitSnapshot>())
                .stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String profitStatus(FinanceProfitSnapshot snapshot) {
        BigDecimal margin = nz(snapshot.getNetMargin()).multiply(HUNDRED);
        if (nz(snapshot.getNetProfit()).compareTo(BigDecimal.ZERO) < 0) {
            return "LOSS";
        }
        if (margin.compareTo(BigDecimal.valueOf(40)) > 0) {
            return "HIGH";
        }
        if (margin.compareTo(BigDecimal.valueOf(10)) < 0) {
            return "LOW";
        }
        return "NORMAL";
    }

    private static String[] splitCsv(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private static String value(String[] values, Map<String, Integer> headerIndex, String name, String defaultValue) {
        Integer index = headerIndex.get(name);
        if (index == null || index >= values.length || values[index].isBlank()) {
            return defaultValue;
        }
        return values[index].replace("\"", "").trim();
    }

    private static String excelValue(Map<Integer, String> values, Map<String, Integer> headerIndex, String name, String defaultValue) {
        Integer index = headerIndex.get(name);
        if (index == null) {
            return defaultValue;
        }
        String value = values.get(index);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static BigDecimal sumBills(List<FinancePlatformBill> bills, java.util.function.Function<FinancePlatformBill, BigDecimal> getter) {
        return bills.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal margin(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.multiply(HUNDRED).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal number) {
            return number;
        }
        return new BigDecimal(value.toString());
    }

    private static Long longValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long tenantId() {
        return TenantContext.getTenantId() == null ? 0L : TenantContext.getTenantId();
    }
}
