package com.lyf.supplychain.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.finance.FinanceFeignClient;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.logistics.entity.LogisticsBillRecord;
import com.lyf.supplychain.logistics.entity.LogisticsFeeRecord;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.mapper.LogisticsBillRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsFeeRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsWaybillMapper;
import com.lyf.supplychain.logistics.service.impl.LogisticsBillServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 物流账单导入与自动对账测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class LogisticsBillServiceImplTest {

    private final LogisticsWaybill matchedWaybill = waybill();
    private final LogisticsFeeRecord matchedFee = feeRecord();
    private final List<LogisticsBillRecord> insertedBillRecords = new ArrayList<>();
    private final List<LogisticsFeeRecord> updatedFees = new ArrayList<>();
    private final List<LogisticsWaybill> updatedWaybills = new ArrayList<>();
    private final List<LogisticsBillRecord> updatedBillRecords = new ArrayList<>();
    private int waybillSelectCount;
    private FinanceLogisticsPayableCreateRequest payableRequest;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void importBillShouldReconcileMatchedRowsAndMarkUnmatchedRows() {
        TenantContext.set(101L, 1001L);
        LogisticsBillService service = new LogisticsBillServiceImpl(
                mapper(LogisticsWaybillMapper.class, this::waybillMapper),
                mapper(LogisticsFeeRecordMapper.class, this::feeRecordMapper),
                mapper(LogisticsBillRecordMapper.class, this::billRecordMapper),
                financeFeignClient()
        );
        String csv = """
                trackingNo,waybillNo,billingWeightG,baseFee,fuelSurcharge,peakSurcharge,remoteFee,otherFee,actualTotal,currency
                TRK001,WB001,550,80,10,5,0,0,95,CNY
                TRK404,WB404,500,50,0,0,0,0,50,CNY
                """;
        MockMultipartFile file = new MockMultipartFile("file", "dhl-bill.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        var result = service.importBill(file, "DHL");

        assertThat(result.getImportedCount()).isEqualTo(2);
        assertThat(result.getMatchedCount()).isEqualTo(1);
        assertThat(result.getAutoConfirmedCount()).isEqualTo(1);
        assertThat(result.getUnmatchedCount()).isEqualTo(1);
        assertThat(result.getBillBatchNo()).startsWith("TMSBILL");
        assertThat(insertedBillRecords).hasSize(2);
        assertThat(insertedBillRecords.get(0).getReconcileStatus()).isEqualTo(0);
        assertThat(insertedBillRecords.get(1).getReconcileStatus()).isEqualTo(2);
        assertThat(updatedFees.get(0).getActualTotal()).isEqualByComparingTo("95");
        assertThat(updatedWaybills.get(0).getActualFee()).isEqualByComparingTo("95");
    }

    @Test
    void confirmBillBatchShouldCreateFinancePayableAfterAllRowsAutoConfirmed() {
        TenantContext.set(101L, 1001L);
        LogisticsBillService service = new LogisticsBillServiceImpl(
                mapper(LogisticsWaybillMapper.class, this::waybillMapper),
                mapper(LogisticsFeeRecordMapper.class, this::feeRecordMapper),
                mapper(LogisticsBillRecordMapper.class, this::billRecordMapper),
                financeFeignClient()
        );
        LogisticsBillRecord record = new LogisticsBillRecord();
        record.setId(6001L);
        record.setTenantId(101L);
        record.setBillBatchNo("TMSBILL202605250001");
        record.setCarrierCode("DHL");
        record.setActualTotal(BigDecimal.valueOf(95));
        record.setCurrency("CNY");
        record.setReconcileStatus(0);
        insertedBillRecords.add(record);

        var result = service.confirmBillBatch("TMSBILL202605250001");

        assertThat(result.getPayableId()).isEqualTo(7001L);
        assertThat(result.getPayableAmount()).isEqualByComparingTo("95");
        assertThat(payableRequest.getBillBatchNo()).isEqualTo("TMSBILL202605250001");
        assertThat(payableRequest.getCarrierCode()).isEqualTo("DHL");
        assertThat(updatedBillRecords.get(0).getPayableId()).isEqualTo(7001L);
    }

    private Object waybillMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        return switch (method.getName()) {
            case "selectOne" -> ++waybillSelectCount == 1 ? matchedWaybill : null;
            case "updateById" -> {
                updatedWaybills.add((LogisticsWaybill) args[0]);
                yield 1;
            }
            default -> defaultValue(method);
        };
    }

    private Object feeRecordMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        return switch (method.getName()) {
            case "selectOne" -> matchedFee;
            case "updateById" -> {
                updatedFees.add((LogisticsFeeRecord) args[0]);
                yield 1;
            }
            default -> defaultValue(method);
        };
    }

    private Object billRecordMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if ("insert".equals(method.getName())) {
            insertedBillRecords.add((LogisticsBillRecord) args[0]);
            return 1;
        }
        if ("selectList".equals(method.getName())) {
            return insertedBillRecords;
        }
        if ("updateById".equals(method.getName())) {
            updatedBillRecords.add((LogisticsBillRecord) args[0]);
            return 1;
        }
        return defaultValue(method);
    }

    private FinanceFeignClient financeFeignClient() {
        return mapper(FinanceFeignClient.class, (proxy, method, args) -> {
            if ("createLogisticsPayable".equals(method.getName())) {
                payableRequest = (FinanceLogisticsPayableCreateRequest) args[0];
                return R.ok(7001L);
            }
            return null;
        });
    }

    private Object defaultValue(java.lang.reflect.Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T mapper(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private LogisticsWaybill waybill() {
        LogisticsWaybill waybill = new LogisticsWaybill();
        waybill.setId(9001L);
        waybill.setTenantId(101L);
        waybill.setWaybillNo("WB001");
        waybill.setTrackingNo("TRK001");
        return waybill;
    }

    private LogisticsFeeRecord feeRecord() {
        LogisticsFeeRecord fee = new LogisticsFeeRecord();
        fee.setId(8001L);
        fee.setTenantId(101L);
        fee.setWaybillId(9001L);
        fee.setWaybillNo("WB001");
        fee.setEstimatedTotal(BigDecimal.valueOf(100));
        fee.setCurrency("CNY");
        return fee;
    }
}
