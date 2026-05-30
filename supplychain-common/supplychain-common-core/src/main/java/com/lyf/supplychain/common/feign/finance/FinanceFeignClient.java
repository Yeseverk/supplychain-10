package com.lyf.supplychain.common.feign.finance;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 财务服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@FeignClient(name = "supplychain-finance", path = "/internal/fms")
public interface FinanceFeignClient {

    /**
     * 根据采购单创建应付账款。
     *
     * @param request 应付账款创建请求
     * @return 应付账款ID
     */
    @PostMapping("/payables")
    R<Long> createPayable(@RequestBody FinancePayableCreateRequest request);

    /**
     * 根据物流账单批次创建应付账款。
     *
     * @param request 物流账单应付创建请求
     * @return 应付账款ID
     */
    @PostMapping("/payables/logistics")
    R<Long> createLogisticsPayable(@RequestBody FinanceLogisticsPayableCreateRequest request);

    /**
     * 登记付款并返回采购单ID，用于采购单状态同步。
     *
     * @param payableId 应付账款ID
     * @param request   付款请求
     * @return 采购单ID
     */
    @PostMapping("/payables/{payableId}/payments")
    R<Long> pay(@PathVariable("payableId") Long payableId, @RequestBody FinancePaymentRequest request);

    /**
     * 根据采购退货冲减应付账款。
     *
     * @param request 应付账款冲减请求
     * @return 应付账款ID
     */
    @PostMapping("/payables/offset")
    R<Long> offsetPayable(@RequestBody FinancePayableOffsetRequest request);
}
