package com.lyf.supplychain.finance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.feign.purchase.PurchaseFeignClient;
import com.lyf.supplychain.common.feign.purchase.PurchaseRequisitionCreateRequest;
import com.lyf.supplychain.finance.entity.FinanceBillItem;
import com.lyf.supplychain.finance.entity.FinanceCashFlow;
import com.lyf.supplychain.finance.entity.FinancePlatformBill;
import com.lyf.supplychain.finance.entity.FinanceProfitSnapshot;
import com.lyf.supplychain.finance.mapper.BiKpiThresholdMapper;
import com.lyf.supplychain.finance.mapper.FinanceBillItemMapper;
import com.lyf.supplychain.finance.mapper.FinanceCashFlowMapper;
import com.lyf.supplychain.finance.mapper.FinanceExchangeRateMapper;
import com.lyf.supplychain.finance.mapper.FinancePlatformBillMapper;
import com.lyf.supplychain.finance.mapper.FinanceProfitSnapshotMapper;
import com.lyf.supplychain.finance.mapper.FinanceVatRecordMapper;
import com.lyf.supplychain.finance.service.impl.FinanceSettlementBiServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 财务结算业务测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class FinanceSettlementBiServiceImplTest {

    private final FinancePlatformBill bill = bill();
    private final List<FinanceBillItem> billItems = billItems();
    private final List<FinancePlatformBill> updatedBills = new ArrayList<>();
    private final List<FinanceCashFlow> cashFlows = new ArrayList<>();
    private final List<FinanceProfitSnapshot> snapshots = new ArrayList<>();
    private final List<PurchaseRequisitionCreateRequest> purchaseRequests = new ArrayList<>();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void confirmBillShouldCreateCashFlowAndSkuProfitSnapshotIdempotently() {
        TenantContext.set(101L, 1001L);
        FinanceSettlementBiService service = service();

        service.confirmBill(7001L);
        service.confirmBill(7001L);

        assertThat(updatedBills.get(0).getStatus()).isEqualTo(3);
        assertThat(cashFlows).hasSize(1);
        assertThat(cashFlows.get(0).getSourceType()).isEqualTo("PLATFORM_BILL");
        assertThat(cashFlows.get(0).getAmountCny()).isEqualByComparingTo("720.00");
        assertThat(snapshots).hasSize(1);
        FinanceProfitSnapshot snapshot = snapshots.get(0);
        assertThat(snapshot.getSkuCode()).isEqualTo("SKU-A");
        assertThat(snapshot.getGrossRevenueCny()).isEqualByComparingTo("720.00");
        assertThat(snapshot.getPlatformFee()).isEqualByComparingTo("108.0000");
        assertThat(snapshot.getAdvertisingFee()).isEqualByComparingTo("36.0000");
        assertThat(snapshot.getRefundLoss()).isEqualByComparingTo("144.0000");
        assertThat(snapshot.getNetProfit()).isLessThan(snapshot.getGrossRevenueCny());
    }

    @Test
    void reorderToPurchaseShouldCreateRequisitionByFeign() {
        TenantContext.set(101L, 1001L);
        FinanceSettlementBiService service = service();

        Map<String, Object> result = service.reorderToPurchase();

        assertThat(result.get("createdApplyCount")).isEqualTo(1);
        assertThat(result.get("requisitionId")).isEqualTo(9001L);
        assertThat(purchaseRequests).hasSize(1);
        PurchaseRequisitionCreateRequest request = purchaseRequests.get(0);
        assertThat(request.getTitle()).startsWith("BI智能补货建议-");
        assertThat(request.getItems()).hasSize(1);
        assertThat(request.getItems().get(0).getSkuCode()).isEqualTo("SKU-A");
        assertThat(request.getItems().get(0).getQuantity()).isGreaterThan(0);
    }

    private FinanceSettlementBiService service() {
        return new FinanceSettlementBiServiceImpl(
                mapper(FinanceExchangeRateMapper.class, this::defaultValue),
                mapper(FinancePlatformBillMapper.class, this::platformBillMapper),
                mapper(FinanceBillItemMapper.class, this::billItemMapper),
                mapper(FinanceProfitSnapshotMapper.class, this::profitSnapshotMapper),
                mapper(FinanceVatRecordMapper.class, this::defaultValue),
                mapper(FinanceCashFlowMapper.class, this::cashFlowMapper),
                mapper(BiKpiThresholdMapper.class, this::defaultValue),
                payableService(),
                new FinanceNumberService() {
                    @Override
                    public String nextPayableNo() {
                        return "PAY-001";
                    }

                    @Override
                    public String nextBillNo() {
                        return "BILL-001";
                    }
                },
                purchaseFeignClient(),
                null,
                new ObjectMapper()
        );
    }

    private Object platformBillMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        return switch (method.getName()) {
            case "selectById" -> bill;
            case "updateById" -> {
                updatedBills.add((FinancePlatformBill) args[0]);
                yield 1;
            }
            default -> defaultValue(proxy, method, args);
        };
    }

    private Object billItemMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if ("selectList".equals(method.getName())) {
            return billItems;
        }
        return defaultValue(proxy, method, args);
    }

    private Object profitSnapshotMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if ("selectList".equals(method.getName())) {
            return List.of(reorderSnapshot());
        }
        if ("selectOne".equals(method.getName())) {
            return snapshots.isEmpty() ? null : snapshots.get(0);
        }
        if ("insert".equals(method.getName())) {
            snapshots.add((FinanceProfitSnapshot) args[0]);
            return 1;
        }
        return defaultValue(proxy, method, args);
    }

    private Object cashFlowMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if ("selectOne".equals(method.getName())) {
            return cashFlows.isEmpty() ? null : cashFlows.get(0);
        }
        if ("insert".equals(method.getName())) {
            cashFlows.add((FinanceCashFlow) args[0]);
            return 1;
        }
        return defaultValue(proxy, method, args);
    }

    private FinancePayableService payableService() {
        return mapper(FinancePayableService.class, (proxy, method, args) -> {
            if ("list".equals(method.getName())) {
                return List.of();
            }
            return defaultValue(proxy, method, args);
        });
    }

    private PurchaseFeignClient purchaseFeignClient() {
        return mapper(PurchaseFeignClient.class, (proxy, method, args) -> {
            if ("createRequisition".equals(method.getName())) {
                purchaseRequests.add((PurchaseRequisitionCreateRequest) args[0]);
                return R.ok(9001L);
            }
            return defaultValue(proxy, method, args);
        });
    }

    private Object defaultValue(Object proxy, java.lang.reflect.Method method, Object[] args) {
        Class<?> returnType = method.getReturnType();
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (Page.class.isAssignableFrom(returnType)) {
            return Page.of(1, 10);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T mapper(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private FinancePlatformBill bill() {
        FinancePlatformBill bill = new FinancePlatformBill();
        bill.setId(7001L);
        bill.setTenantId(101L);
        bill.setBillNo("BILL-001");
        bill.setPlatform("AMAZON");
        bill.setStoreId("store-001");
        bill.setSettlementEnd(LocalDate.of(2026, 5, 25));
        bill.setCurrency("USD");
        bill.setExchangeRate(BigDecimal.valueOf(7.2));
        bill.setCnyAmount(BigDecimal.valueOf(720));
        bill.setStatus(2);
        return bill;
    }

    private FinanceProfitSnapshot reorderSnapshot() {
        FinanceProfitSnapshot snapshot = new FinanceProfitSnapshot();
        snapshot.setTenantId(101L);
        snapshot.setSkuId(501L);
        snapshot.setSkuCode("SKU-A");
        snapshot.setSalesQty(60);
        snapshot.setNetProfit(BigDecimal.valueOf(1200));
        return snapshot;
    }

    private List<FinanceBillItem> billItems() {
        return List.of(
                item("Principal", "SKU-A", BigDecimal.valueOf(100)),
                item("ReferralFee", "SKU-A", BigDecimal.valueOf(-15)),
                item("FBAFee", "SKU-A", BigDecimal.valueOf(-10)),
                item("Advertising", "SKU-A", BigDecimal.valueOf(-5)),
                item("Refund", "SKU-A", BigDecimal.valueOf(-20))
        );
    }

    private FinanceBillItem item(String type, String sku, BigDecimal amount) {
        FinanceBillItem item = new FinanceBillItem();
        item.setTenantId(101L);
        item.setBillId(7001L);
        item.setItemType(type);
        item.setPlatformSku(sku);
        item.setAmount(amount);
        item.setCurrency("USD");
        item.setIsMatched(1);
        item.setTransactionDate(LocalDate.of(2026, 5, 25));
        return item;
    }
}
