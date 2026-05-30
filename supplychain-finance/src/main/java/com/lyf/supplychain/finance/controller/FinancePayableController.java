package com.lyf.supplychain.finance.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.finance.FinancePaymentRequest;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.entity.FinancePaymentRecord;
import com.lyf.supplychain.finance.request.FinancePayablePageQuery;
import com.lyf.supplychain.finance.service.FinancePayableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应付账款接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/fms/payables", "/fms/payables", "/api/pms/payables", "/pms/payables"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.FMS_BILL_IMPORT)
public class FinancePayableController {

    private final FinancePayableService payableService;

    public FinancePayableController(FinancePayableService payableService) {
        this.payableService = payableService;
    }

    /**
     * 分页查询应付账款。
     *
     * @param query 分页参数
     * @return 应付账款分页结果
     */
    @GetMapping
    public R<PageResult<FinancePayable>> pagePayables(FinancePayablePageQuery query) {
        return R.ok(payableService.pagePayables(query));
    }

    /**
     * 登记付款。
     *
     * @param payableId 应付账款ID
     * @param request   付款请求
     * @return 采购单ID
     */
    @PostMapping("/{payableId}/pay")
    @TenantWriteGuard(scene = "登记付款")
    public R<Long> pay(@PathVariable("payableId") Long payableId, @RequestBody FinancePaymentRequest request) {
        return R.ok(payableService.pay(payableId, request));
    }

    /**
     * 查询付款凭证/付款记录。
     *
     * @param payableId 应付账款ID
     * @return 付款记录列表
     */
    @GetMapping("/{payableId}/payments")
    public R<List<FinancePaymentRecord>> payments(@PathVariable("payableId") Long payableId) {
        return R.ok(payableService.payments(payableId));
    }
}
