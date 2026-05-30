package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.warehouse.entity.InboundOrder;
import com.lyf.supplychain.warehouse.entity.InboundOrderItem;
import com.lyf.supplychain.warehouse.request.InboundConfirmRequest;
import com.lyf.supplychain.warehouse.request.InboundOrderRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.InboundOrderService;
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
 * 入库管理接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/wms/inbound", "/wms/inbound"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.WMS_INBOUND_MANAGE)
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    /**
     * 入库单列表。
     *
     * @param query 分页参数
     * @return 入库单分页结果
     */
    @GetMapping
    public R<PageResult<InboundOrder>> pageInbound(WmsPageQuery query) {
        return R.ok(inboundOrderService.pageInbound(query));
    }

    /**
     * 查询入库单明细。
     *
     * @param id 入库单ID
     * @return 入库明细列表
     */
    @GetMapping("/{id}/items")
    public R<List<InboundOrderItem>> items(@PathVariable("id") Long id) {
        return R.ok(inboundOrderService.listItems(id));
    }

    /**
     * 创建入库单。
     *
     * @param request 创建请求
     * @return 入库单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建入库单")
    public R<Long> create(@Valid @RequestBody InboundOrderRequest request) {
        return R.ok(inboundOrderService.create(request));
    }

    /**
     * 确认入库。
     *
     * @param id      入库单ID
     * @param request 确认请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/confirm")
    @TenantWriteGuard(scene = "确认入库")
    public R<Void> confirm(@PathVariable("id") Long id, @Valid @RequestBody InboundConfirmRequest request) {
        inboundOrderService.confirm(id, request);
        return R.ok();
    }
}
