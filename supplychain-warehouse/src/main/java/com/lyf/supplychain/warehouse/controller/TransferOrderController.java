package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.TransferOrder;
import com.lyf.supplychain.warehouse.request.TransferOrderRequest;
import com.lyf.supplychain.warehouse.service.TransferOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调拨管理接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/transfers", "/wms/transfers"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_INVENTORY_ADJUST)
public class TransferOrderController {

    private final TransferOrderService transferOrderService;

    public TransferOrderController(TransferOrderService transferOrderService) {
        this.transferOrderService = transferOrderService;
    }

    /**
     * 调拨单列表。
     *
     * @param query 分页参数
     * @return 调拨单分页结果
     */
    @GetMapping
    public R<PageResult<TransferOrder>> pageTransfers(PageQuery query) {
        return R.ok(transferOrderService.pageTransfers(query));
    }

    /**
     * 创建调拨单。
     *
     * @param request 创建请求
     * @return 调拨单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建调拨单")
    public R<Long> create(@Valid @RequestBody TransferOrderRequest request) {
        return R.ok(transferOrderService.create(request));
    }

    /**
     * 审核调拨单。
     *
     * @param id 调拨单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/approve")
    @TenantWriteGuard(scene = "审核调拨单")
    public R<Void> approve(@PathVariable("id") Long id) {
        transferOrderService.approve(id);
        return R.ok();
    }

    /**
     * 确认发货。
     *
     * @param id 调拨单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/ship")
    @TenantWriteGuard(scene = "确认调拨发货")
    public R<Void> ship(@PathVariable("id") Long id) {
        transferOrderService.ship(id);
        return R.ok();
    }

    /**
     * 确认到货。
     *
     * @param id 调拨单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/receive")
    @TenantWriteGuard(scene = "确认调拨到货")
    public R<Void> receive(@PathVariable("id") Long id) {
        transferOrderService.receive(id);
        return R.ok();
    }
}
