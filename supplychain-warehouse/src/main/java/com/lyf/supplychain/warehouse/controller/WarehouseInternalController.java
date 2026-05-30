package com.lyf.supplychain.warehouse.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInventoryWarningResponse;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundOrderCreateRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseStockItem;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.Warehouse;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.request.InventoryAdjustRequest;
import com.lyf.supplychain.warehouse.request.OutboundOrderRequest;
import com.lyf.supplychain.warehouse.request.WmsItemRequest;
import com.lyf.supplychain.warehouse.service.OutboundOrderService;
import com.lyf.supplychain.warehouse.service.WarehouseService;
import com.lyf.supplychain.warehouse.service.WmsInventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 仓储内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/internal/wms")
public class WarehouseInternalController {

    private final WmsInventoryService wmsInventoryService;

    private final OutboundOrderService outboundOrderService;

    private final WarehouseService warehouseService;

    public WarehouseInternalController(WmsInventoryService wmsInventoryService,
                                       OutboundOrderService outboundOrderService,
                                       WarehouseService warehouseService) {
        this.wmsInventoryService = wmsInventoryService;
        this.outboundOrderService = outboundOrderService;
        this.warehouseService = warehouseService;
    }

    /**
     * 采购入库增加库存。
     *
     * @param request 入库请求
     * @return 无数据响应
     */
    @PostMapping("/inventory/inbound")
    public R<Void> inbound(@RequestBody WarehouseInboundRequest request) {
        for (WarehouseStockItem item : request.getItems()) {
            wmsInventoryService.applyChange(toInventoryAdjustRequest(request.getWarehouseId(),
                    request.getBizNo(), request.getBizType(), item, Math.abs(item.getQuantity()), WmsConstants.LOG_PURCHASE_IN));
        }
        return R.ok();
    }

    /**
     * 采购退货减少库存。
     *
     * @param request 出库请求
     * @return 无数据响应
     */
    @PostMapping("/inventory/outbound")
    public R<Void> outbound(@RequestBody WarehouseOutboundRequest request) {
        for (WarehouseStockItem item : request.getItems()) {
            wmsInventoryService.applyChange(toInventoryAdjustRequest(request.getWarehouseId(),
                    request.getBizNo(), request.getBizType(), item, -Math.abs(item.getQuantity()), WmsConstants.LOG_DAMAGE_OUT));
        }
        return R.ok();
    }

    /**
     * 创建 WMS 出库单，供 OMS 等上游业务生成可追踪履约单据。
     *
     * @param request 出库单创建请求
     * @return 出库单ID
     */
    @PostMapping("/outbound/orders")
    public R<Long> createOutboundOrder(@RequestBody WarehouseOutboundOrderCreateRequest request) {
        return R.ok(outboundOrderService.create(toOutboundOrderRequest(request)));
    }

    /**
     * 查询库存预警列表。
     *
     * @return 库存预警列表
     */
    @GetMapping("/inventory/warnings")
    public R<List<WarehouseInventoryWarningResponse>> warnings() {
        return R.ok(wmsInventoryService.warnings().stream()
                .map(this::toWarningResponse)
                .toList());
    }

    private WarehouseInventoryWarningResponse toWarningResponse(Inventory inventory) {
        WarehouseInventoryWarningResponse response = new WarehouseInventoryWarningResponse();
        response.setTenantId(inventory.getTenantId());
        response.setWarehouseId(inventory.getWarehouseId());
        response.setLocationId(inventory.getLocationId());
        response.setSkuId(inventory.getSkuId());
        response.setSkuCode(inventory.getSkuCode());
        response.setSkuName(inventory.getSkuName());
        response.setQuantity(inventory.getQuantity());
        response.setAvailableQty(inventory.getAvailableQty());
        response.setInTransitQty(inventory.getInTransitQty());
        response.setSafetyStock(inventory.getSafetyStock());
        response.setMaxStock(inventory.getMaxStock());
        response.setReorderPoint(inventory.getReorderPoint());
        return response;
    }

    private OutboundOrderRequest toOutboundOrderRequest(WarehouseOutboundOrderCreateRequest source) {
        OutboundOrderRequest target = new OutboundOrderRequest();
        target.setOutboundType(source.getOutboundType() == null ? 1 : source.getOutboundType());
        target.setWarehouseId(source.getWarehouseId());
        target.setWarehouseName(resolveWarehouseName(source));
        target.setRefType(source.getRefType());
        target.setRefId(source.getRefId());
        target.setRefNo(source.getRefNo());
        target.setPlanDate(source.getPlanDate());
        target.setRemark(source.getRemark());
        target.setItems((source.getItems() == null ? Collections.<WarehouseStockItem>emptyList() : source.getItems())
                .stream()
                .map(this::toWmsItemRequest)
                .toList());
        return target;
    }

    private WmsItemRequest toWmsItemRequest(WarehouseStockItem source) {
        WmsItemRequest target = new WmsItemRequest();
        target.setSkuId(source.getSkuId());
        target.setSkuCode(source.getSkuCode());
        target.setSkuName(source.getSkuName());
        target.setQuantity(source.getQuantity());
        target.setLocationId(source.getLocationId());
        return target;
    }

    private InventoryAdjustRequest toInventoryAdjustRequest(Long warehouseId,
                                                            String refNo,
                                                            String refType,
                                                            WarehouseStockItem source,
                                                            Integer changeQty,
                                                            Integer logType) {
        InventoryAdjustRequest target = new InventoryAdjustRequest();
        target.setWarehouseId(warehouseId);
        target.setLocationId(source.getLocationId());
        target.setSkuId(source.getSkuId());
        target.setSkuCode(source.getSkuCode());
        target.setSkuName(source.getSkuName());
        target.setChangeQty(changeQty);
        target.setLogType(logType);
        target.setRefType(refType);
        target.setRefNo(refNo);
        target.setOperatorName("内部联动");
        target.setRemark(refType + "：" + refNo);
        return target;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveWarehouseName(WarehouseOutboundOrderCreateRequest source) {
        if (hasText(source.getWarehouseName())) {
            return source.getWarehouseName();
        }
        Warehouse warehouse = source.getWarehouseId() == null ? null : warehouseService.getById(source.getWarehouseId());
        if (warehouse != null && hasText(warehouse.getWarehouseName())) {
            return warehouse.getWarehouseName();
        }
        return "仓库-" + source.getWarehouseId();
    }
}
