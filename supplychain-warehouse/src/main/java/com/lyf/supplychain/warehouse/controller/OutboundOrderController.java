package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.OutboundOrder;
import com.lyf.supplychain.warehouse.entity.OutboundOrderItem;
import com.lyf.supplychain.warehouse.request.OutboundOrderRequest;
import com.lyf.supplychain.warehouse.request.PickProgressRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.OutboundOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 出库管理接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/outbound", "/wms/outbound"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_OUTBOUND_MANAGE)
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    public OutboundOrderController(OutboundOrderService outboundOrderService) {
        this.outboundOrderService = outboundOrderService;
    }

    /**
     * 出库单列表。
     *
     * @param query 分页参数
     * @return 出库单分页结果
     */
    @GetMapping
    public R<PageResult<OutboundOrder>> pageOutbound(WmsPageQuery query) {
        return R.ok(outboundOrderService.pageOutbound(query));
    }

    /**
     * 创建出库单。
     *
     * @param request 创建请求
     * @return 出库单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建出库单")
    public R<Long> create(@Valid @RequestBody OutboundOrderRequest request) {
        return R.ok(outboundOrderService.create(request));
    }

    /**
     * 获取拣货单。
     *
     * @param id 出库单ID
     * @return 拣货单明细
     */
    @GetMapping("/{id}/picklist")
    public R<List<OutboundOrderItem>> pickList(@PathVariable("id") Long id) {
        return R.ok(outboundOrderService.pickList(id));
    }

    /**
     * 更新拣货进度。
     *
     * @param id      出库单ID
     * @param request 拣货请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/pick")
    @TenantWriteGuard(scene = "更新拣货进度")
    public R<Void> updatePick(@PathVariable("id") Long id, @Valid @RequestBody PickProgressRequest request) {
        outboundOrderService.updatePick(id, request);
        return R.ok();
    }

    /**
     * 确认出库。
     *
     * @param id 出库单ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/confirm")
    @TenantWriteGuard(scene = "确认出库")
    public R<Void> confirm(@PathVariable("id") Long id) {
        outboundOrderService.confirm(id);
        return R.ok();
    }
}
