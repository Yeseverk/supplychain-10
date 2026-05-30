package com.lyf.supplychain.finance.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.common.feign.purchase.PurchaseFeignClient;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.mapper.FinancePaymentRecordMapper;
import com.lyf.supplychain.finance.service.impl.FinancePayableServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 财务应付账款服务测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class FinancePayableServiceImplTest {

    @Test
    void createFromLogisticsBillShouldBeIdempotentBySourceBizNo() {
        TestableFinancePayableService service = new TestableFinancePayableService();
        FinanceLogisticsPayableCreateRequest request = new FinanceLogisticsPayableCreateRequest();
        request.setTenantId(101L);
        request.setBillBatchNo("TMSBILL202605250001");
        request.setCarrierCode("DHL");
        request.setPayableAmount(BigDecimal.valueOf(95));
        request.setCurrency("CNY");
        request.setInvoiceDate(LocalDate.of(2026, 5, 25));

        Long firstId = service.createFromLogisticsBill(request);
        Long secondId = service.createFromLogisticsBill(request);

        assertThat(firstId).isEqualTo(7001L);
        assertThat(secondId).isEqualTo(7001L);
        assertThat(service.saveCount).isEqualTo(1);
        assertThat(service.saved.getSourceType()).isEqualTo("TMS_LOGISTICS_BILL");
        assertThat(service.saved.getSourceBizNo()).isEqualTo("TMSBILL202605250001");
        assertThat(service.saved.getSupplierName()).isEqualTo("DHL");
    }

    private static class TestableFinancePayableService extends FinancePayableServiceImpl {

        private FinancePayable saved;
        private int saveCount;

        TestableFinancePayableService() {
            super(mapper(FinancePaymentRecordMapper.class), new FinanceNumberService() {
                @Override
                public String nextPayableNo() {
                    return "PAY-001";
                }

                @Override
                public String nextBillNo() {
                    return "BILL-001";
                }
            }, mapper(PurchaseFeignClient.class));
        }

        @Override
        public FinancePayable getOne(Wrapper<FinancePayable> queryWrapper) {
            return saved;
        }

        @Override
        public boolean save(FinancePayable entity) {
            entity.setId(7001L);
            saved = entity;
            saveCount++;
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapper(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null);
    }
}
