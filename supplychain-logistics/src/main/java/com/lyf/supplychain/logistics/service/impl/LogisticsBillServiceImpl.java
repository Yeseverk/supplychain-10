package com.lyf.supplychain.logistics.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.finance.FinanceFeignClient;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.logistics.constant.LogisticsConstants;
import com.lyf.supplychain.logistics.entity.LogisticsBillRecord;
import com.lyf.supplychain.logistics.entity.LogisticsFeeRecord;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.mapper.LogisticsBillRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsFeeRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsWaybillMapper;
import com.lyf.supplychain.logistics.model.LogisticsBillConfirmResult;
import com.lyf.supplychain.logistics.model.LogisticsBillImportResult;
import com.lyf.supplychain.logistics.model.LogisticsBillImportRow;
import com.lyf.supplychain.logistics.service.LogisticsBillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流商账单导入与对账服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Service
public class LogisticsBillServiceImpl implements LogisticsBillService {

    private static final BigDecimal AUTO_CONFIRM_DIFF_RATE = BigDecimal.valueOf(0.05);

    private final LogisticsWaybillMapper waybillMapper;
    private final LogisticsFeeRecordMapper feeRecordMapper;
    private final LogisticsBillRecordMapper billRecordMapper;
    private final FinanceFeignClient financeFeignClient;

    public LogisticsBillServiceImpl(LogisticsWaybillMapper waybillMapper,
                                    LogisticsFeeRecordMapper feeRecordMapper,
                                    LogisticsBillRecordMapper billRecordMapper,
                                    FinanceFeignClient financeFeignClient) {
        this.waybillMapper = waybillMapper;
        this.feeRecordMapper = feeRecordMapper;
        this.billRecordMapper = billRecordMapper;
        this.financeFeignClient = financeFeignClient;
    }

    /**
     * 导入物流商账单并与预估运费自动对账。
     *
     * @param file        账单文件
     * @param carrierCode 物流商编码
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogisticsBillImportResult importBill(MultipartFile file, String carrierCode) {
        if (file == null || file.isEmpty()) {
            BusinessException.throwException(16015, "物流商账单文件不能为空");
        }
        String billBatchNo = "TMSBILL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<LogisticsBillImportRow> rows = readRows(file);
        LogisticsBillImportResult result = new LogisticsBillImportResult();
        result.setBillBatchNo(billBatchNo);
        for (LogisticsBillImportRow row : rows) {
            reconcileRow(result, billBatchNo, carrierCode, row);
        }
        return result;
    }

    /**
     * 确认物流账单批次并推送财务生成应付账款。
     *
     * @param billBatchNo 账单批次号
     * @return 确认结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogisticsBillConfirmResult confirmBillBatch(String billBatchNo) {
        List<LogisticsBillRecord> records = billRecordMapper.selectList(new LambdaQueryWrapper<LogisticsBillRecord>()
                .eq(LogisticsBillRecord::getTenantId, TenantContext.getTenantId())
                .eq(LogisticsBillRecord::getBillBatchNo, billBatchNo));
        if (records.isEmpty()) {
            BusinessException.throwException(16017, "物流账单批次不存在");
        }
        LogisticsBillRecord first = records.get(0);
        Long existedPayableId = first.getPayableId();
        if (existedPayableId != null) {
            return buildConfirmResult(billBatchNo, first.getCarrierCode(), existedPayableId, sumActualFee(records), first.getCurrency(), records.size());
        }
        boolean hasAbnormal = records.stream()
                .anyMatch(record -> record.getReconcileStatus() == null
                        || record.getReconcileStatus() != LogisticsConstants.RECONCILE_AUTO_CONFIRMED);
        if (hasAbnormal) {
            BusinessException.throwException(16018, "账单存在未匹配或待复核记录，不能确认生成应付");
        }
        String currency = first.getCurrency();
        boolean mixedCurrency = records.stream().anyMatch(record -> !currency.equals(record.getCurrency()));
        if (mixedCurrency) {
            BusinessException.throwException(16019, "账单批次存在多个币种，请拆分后确认");
        }
        BigDecimal payableAmount = sumActualFee(records);
        FinanceLogisticsPayableCreateRequest request = new FinanceLogisticsPayableCreateRequest();
        request.setTenantId(TenantContext.getTenantId());
        request.setBillBatchNo(billBatchNo);
        request.setCarrierCode(first.getCarrierCode());
        request.setPayableAmount(payableAmount);
        request.setCurrency(currency);
        request.setInvoiceDate(java.time.LocalDate.now());
        R<Long> response = financeFeignClient.createLogisticsPayable(request);
        Long payableId = response == null ? null : response.getData();
        if (payableId == null) {
            BusinessException.throwException(16020, "财务应付账款创建失败");
        }
        for (LogisticsBillRecord record : records) {
            record.setPayableId(payableId);
            record.setConfirmTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            billRecordMapper.updateById(record);
        }
        return buildConfirmResult(billBatchNo, first.getCarrierCode(), payableId, payableAmount, currency, records.size());
    }

    private List<LogisticsBillImportRow> readRows(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return readExcelRows(file);
            }
            return readCsvRows(file);
        } catch (Exception ex) {
            throw new BusinessException(16016, "物流商账单解析失败：" + ex.getMessage());
        }
    }

    private List<LogisticsBillImportRow> readExcelRows(MultipartFile file) throws Exception {
        List<LogisticsBillImportRow> rows = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), LogisticsBillImportRow.class, new AnalysisEventListener<LogisticsBillImportRow>() {
            @Override
            public void invoke(LogisticsBillImportRow data, AnalysisContext context) {
                if (hasBizKey(data)) {
                    rows.add(data);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // EasyExcel 监听器要求实现该回调，业务在 invoke 中逐行处理。
            }
        }).sheet().doRead();
        return rows;
    }

    private List<LogisticsBillImportRow> readCsvRows(MultipartFile file) throws Exception {
        List<LogisticsBillImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                LogisticsBillImportRow row = parseCsvLine(line);
                if (hasBizKey(row)) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private LogisticsBillImportRow parseCsvLine(String line) {
        String[] values = line.split(",", -1);
        LogisticsBillImportRow row = new LogisticsBillImportRow();
        row.setTrackingNo(value(values, 0));
        row.setWaybillNo(value(values, 1));
        row.setBillingWeightG(decimal(values, 2));
        row.setBaseFee(decimal(values, 3));
        row.setFuelSurcharge(decimal(values, 4));
        row.setPeakSurcharge(decimal(values, 5));
        row.setRemoteFee(decimal(values, 6));
        row.setOtherFee(decimal(values, 7));
        row.setActualTotal(decimal(values, 8));
        row.setCurrency(value(values, 9));
        return row;
    }

    private void reconcileRow(LogisticsBillImportResult result,
                              String billBatchNo,
                              String carrierCode,
                              LogisticsBillImportRow row) {
        result.setImportedCount(result.getImportedCount() + 1);
        BigDecimal actualTotal = actualTotal(row);
        LogisticsBillRecord billRecord = buildBillRecord(billBatchNo, carrierCode, row, actualTotal);
        LogisticsWaybill waybill = findWaybill(row);
        if (waybill == null) {
            markUnmatched(result, billRecord, "账单中的运单号未在系统中找到");
            return;
        }
        LogisticsFeeRecord feeRecord = findFeeRecord(waybill.getId());
        if (feeRecord == null) {
            markUnmatched(result, billRecord, "系统中未找到运费预估记录");
            return;
        }
        BigDecimal diffAmount = actualTotal.subtract(nullToZero(feeRecord.getEstimatedTotal()));
        BigDecimal diffRate = diffRate(diffAmount, feeRecord.getEstimatedTotal());
        billRecord.setWaybillId(waybill.getId());
        billRecord.setWaybillNo(waybill.getWaybillNo());
        billRecord.setTrackingNo(waybill.getTrackingNo());
        billRecord.setDiffAmount(diffAmount);
        billRecord.setDiffRate(diffRate);
        billRecord.setReconcileStatus(diffRate.compareTo(AUTO_CONFIRM_DIFF_RATE) <= 0
                ? LogisticsConstants.RECONCILE_AUTO_CONFIRMED : LogisticsConstants.RECONCILE_PENDING_REVIEW);
        billRecord.setRemark(billRecord.getReconcileStatus() == LogisticsConstants.RECONCILE_AUTO_CONFIRMED
                ? "自动对账通过" : "费用差异超过5%，待人工复核");
        billRecordMapper.insert(billRecord);
        feeRecord.setActualTotal(actualTotal);
        feeRecord.setBillingWeightG(row.getBillingWeightG() == null ? feeRecord.getBillingWeightG() : row.getBillingWeightG());
        feeRecord.setUpdateTime(LocalDateTime.now());
        feeRecordMapper.updateById(feeRecord);
        waybill.setActualFee(actualTotal);
        waybillMapper.updateById(waybill);
        result.setMatchedCount(result.getMatchedCount() + 1);
        if (billRecord.getReconcileStatus() == LogisticsConstants.RECONCILE_AUTO_CONFIRMED) {
            result.setAutoConfirmedCount(result.getAutoConfirmedCount() + 1);
        } else {
            result.setPendingReviewCount(result.getPendingReviewCount() + 1);
        }
        result.setTotalActualFee(result.getTotalActualFee().add(actualTotal));
        result.setTotalDiffAmount(result.getTotalDiffAmount().add(diffAmount));
    }

    private LogisticsBillRecord buildBillRecord(String billBatchNo,
                                                String carrierCode,
                                                LogisticsBillImportRow row,
                                                BigDecimal actualTotal) {
        LogisticsBillRecord record = new LogisticsBillRecord();
        record.setTenantId(TenantContext.getTenantId());
        record.setBillBatchNo(billBatchNo);
        record.setCarrierCode(carrierCode);
        record.setWaybillNo(row.getWaybillNo());
        record.setTrackingNo(row.getTrackingNo());
        record.setBillingWeightG(row.getBillingWeightG());
        record.setBaseFee(nullToZero(row.getBaseFee()));
        record.setFuelSurcharge(nullToZero(row.getFuelSurcharge()));
        record.setPeakSurcharge(nullToZero(row.getPeakSurcharge()));
        record.setRemoteFee(nullToZero(row.getRemoteFee()));
        record.setOtherFee(nullToZero(row.getOtherFee()));
        record.setActualTotal(actualTotal);
        record.setCurrency(row.getCurrency() == null || row.getCurrency().isBlank() ? "CNY" : row.getCurrency());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return record;
    }

    private LogisticsBillConfirmResult buildConfirmResult(String billBatchNo,
                                                          String carrierCode,
                                                          Long payableId,
                                                          BigDecimal payableAmount,
                                                          String currency,
                                                          Integer confirmedCount) {
        LogisticsBillConfirmResult result = new LogisticsBillConfirmResult();
        result.setBillBatchNo(billBatchNo);
        result.setCarrierCode(carrierCode);
        result.setPayableId(payableId);
        result.setPayableAmount(payableAmount);
        result.setCurrency(currency);
        result.setConfirmedCount(confirmedCount);
        return result;
    }

    private BigDecimal sumActualFee(List<LogisticsBillRecord> records) {
        return records.stream()
                .map(LogisticsBillRecord::getActualTotal)
                .map(this::nullToZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void markUnmatched(LogisticsBillImportResult result, LogisticsBillRecord billRecord, String remark) {
        billRecord.setDiffAmount(BigDecimal.ZERO);
        billRecord.setDiffRate(BigDecimal.ZERO);
        billRecord.setReconcileStatus(LogisticsConstants.RECONCILE_UNMATCHED);
        billRecord.setRemark(remark);
        billRecordMapper.insert(billRecord);
        result.setUnmatchedCount(result.getUnmatchedCount() + 1);
        result.setTotalActualFee(result.getTotalActualFee().add(billRecord.getActualTotal()));
    }

    private LogisticsWaybill findWaybill(LogisticsBillImportRow row) {
        boolean hasTrackingNo = row.getTrackingNo() != null && !row.getTrackingNo().isBlank();
        boolean hasWaybillNo = row.getWaybillNo() != null && !row.getWaybillNo().isBlank();
        return waybillMapper.selectOne(new LambdaQueryWrapper<LogisticsWaybill>()
                .eq(LogisticsWaybill::getTenantId, TenantContext.getTenantId())
                .and(wrapper -> {
                    if (hasTrackingNo && hasWaybillNo) {
                        wrapper.eq(LogisticsWaybill::getTrackingNo, row.getTrackingNo())
                                .or()
                                .eq(LogisticsWaybill::getWaybillNo, row.getWaybillNo());
                    } else if (hasTrackingNo) {
                        wrapper.eq(LogisticsWaybill::getTrackingNo, row.getTrackingNo());
                    } else {
                        wrapper.eq(LogisticsWaybill::getWaybillNo, row.getWaybillNo());
                    }
                })
                .last("limit 1"));
    }

    private LogisticsFeeRecord findFeeRecord(Long waybillId) {
        return feeRecordMapper.selectOne(new LambdaQueryWrapper<LogisticsFeeRecord>()
                .eq(LogisticsFeeRecord::getWaybillId, waybillId)
                .last("limit 1"));
    }

    private BigDecimal actualTotal(LogisticsBillImportRow row) {
        if (row.getActualTotal() != null) {
            return row.getActualTotal();
        }
        return nullToZero(row.getBaseFee())
                .add(nullToZero(row.getFuelSurcharge()))
                .add(nullToZero(row.getPeakSurcharge()))
                .add(nullToZero(row.getRemoteFee()))
                .add(nullToZero(row.getOtherFee()));
    }

    private BigDecimal diffRate(BigDecimal diffAmount, BigDecimal estimatedTotal) {
        BigDecimal estimated = nullToZero(estimatedTotal);
        if (estimated.compareTo(BigDecimal.ZERO) == 0) {
            return diffAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.ONE;
        }
        return diffAmount.abs().divide(estimated, 4, RoundingMode.HALF_UP);
    }

    private boolean hasBizKey(LogisticsBillImportRow row) {
        return row != null && ((row.getTrackingNo() != null && !row.getTrackingNo().isBlank())
                || (row.getWaybillNo() != null && !row.getWaybillNo().isBlank()));
    }

    private String value(String[] values, int index) {
        if (index >= values.length || values[index] == null || values[index].isBlank()) {
            return null;
        }
        return values[index].trim();
    }

    private BigDecimal decimal(String[] values, int index) {
        String value = value(values, index);
        return value == null ? null : new BigDecimal(value);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
