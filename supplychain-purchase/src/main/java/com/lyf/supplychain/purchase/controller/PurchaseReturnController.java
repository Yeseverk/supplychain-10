package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.purchase.entity.PurchaseReturn;
import com.lyf.supplychain.purchase.request.PurchaseReturnRequest;
import com.lyf.supplychain.purchase.service.PurchaseReturnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购退货接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/returns", "/pms/returns"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_ORDER_MANAGE)
public class PurchaseReturnController {

    private final PurchaseReturnService returnService;

    public PurchaseReturnController(PurchaseReturnService returnService) {
        this.returnService = returnService;
    }

    /**
     * 创建退货单。
     *
     * @param request 创建请求
     * @return 退货单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建采购退货单")
    public R<Long> create(@Valid @RequestBody PurchaseReturnRequest request) {
        return R.ok(returnService.create(request));
    }

    /**
     * 分页查询退货单。
     *
     * @param query 分页参数
     * @return 退货单分页结果
     */
    @GetMapping({"", "/page"})
    public R<PageResult<PurchaseReturn>> pageReturns(PageQuery query) {
        return R.ok(returnService.pageReturns(query));
    }

    /**
     * 退货出库。
     *
     * @param id 退货单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/ship")
    @TenantWriteGuard(scene = "采购退货出库")
    public R<Void> ship(@PathVariable("id") Long id) {
        returnService.ship(id);
        return R.ok();
    }
}
