package com.lyf.supplychain.finance.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableOffsetRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePaymentRequest;
import com.lyf.supplychain.finance.service.FinancePayableService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 财务内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/internal/fms")
public class FinanceInternalController {

    private final FinancePayableService payableService;

    public FinanceInternalController(FinancePayableService payableService) {
        this.payableService = payableService;
    }

    /**
     * 根据采购单创建应付账款。
     *
     * @param request 创建请求
     * @return 应付账款ID
     */
    @PostMapping("/payables")
    public R<Long> createPayable(@RequestBody FinancePayableCreateRequest request) {
        return R.ok(payableService.createFromPurchaseOrder(request));
    }

    /**
     * 根据物流账单批次创建应付账款。
     *
     * @param request 创建请求
     * @return 应付账款ID
     */
    @PostMapping("/payables/logistics")
    public R<Long> createLogisticsPayable(@RequestBody FinanceLogisticsPayableCreateRequest request) {
        return R.ok(payableService.createFromLogisticsBill(request));
    }

    /**
     * 登记付款并返回采购单ID。
     *
     * @param payableId 应付账款ID
     * @param request   付款请求
     * @return 采购单ID
     */
    @PostMapping("/payables/{payableId}/payments")
    public R<Long> pay(@PathVariable("payableId") Long payableId, @RequestBody FinancePaymentRequest request) {
        return R.ok(payableService.pay(payableId, request));
    }

    /**
     * 根据采购退货冲减应付账款。
     *
     * @param request 应付账款冲减请求
     * @return 应付账款ID
     */
    @PostMapping("/payables/offset")
    public R<Long> offsetPayable(@RequestBody FinancePayableOffsetRequest request) {
        return R.ok(payableService.offsetForPurchaseReturn(request));
    }
}
