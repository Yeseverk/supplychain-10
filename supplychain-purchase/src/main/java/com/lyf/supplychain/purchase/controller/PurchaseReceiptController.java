package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.purchase.entity.PurchaseReceipt;
import com.lyf.supplychain.purchase.request.PurchaseReceiptPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseReceiptRequest;
import com.lyf.supplychain.purchase.service.PurchaseReceiptService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购收货接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/receipts", "/pms/receipts"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_RECEIPT_CONFIRM)
public class PurchaseReceiptController {

    private final PurchaseReceiptService receiptService;

    public PurchaseReceiptController(PurchaseReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * 创建收货单。
     *
     * @param request 创建请求
     * @return 收货单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建采购收货单")
    public R<Long> create(@Valid @RequestBody PurchaseReceiptRequest request) {
        return R.ok(receiptService.create(request));
    }

    /**
     * 分页查询收货单。
     *
     * @param query 分页参数
     * @return 收货单分页结果
     */
    @GetMapping({"", "/page"})
    public R<PageResult<PurchaseReceipt>> pageReceipts(PurchaseReceiptPageQuery query) {
        return R.ok(receiptService.pageReceipts(query));
    }

    /**
     * 确认入库。
     *
     * @param id 收货单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/confirm")
    @TenantWriteGuard(scene = "确认采购入库")
    public R<Void> confirm(@PathVariable("id") Long id) {
        receiptService.confirmInbound(id);
        return R.ok();
    }
}
