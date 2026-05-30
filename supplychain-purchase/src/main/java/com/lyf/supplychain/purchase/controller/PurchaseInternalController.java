package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.purchase.PurchaseRequisitionCreateItemRequest;
import com.lyf.supplychain.common.feign.purchase.PurchaseRequisitionCreateRequest;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import com.lyf.supplychain.purchase.service.PurchaseOrderService;
import com.lyf.supplychain.purchase.service.PurchaseRequisitionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采购内部接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping("/internal/pms")
public class PurchaseInternalController {

    private final PurchaseOrderService orderService;
    private final PurchaseRequisitionService requisitionService;

    public PurchaseInternalController(PurchaseOrderService orderService,
                                      PurchaseRequisitionService requisitionService) {
        this.orderService = orderService;
        this.requisitionService = requisitionService;
    }

    /**
     * 创建采购申请。
     *
     * @param request 采购申请创建请求
     * @return 采购申请ID
     */
    @PostMapping("/requisitions")
    public R<Long> createRequisition(@RequestBody PurchaseRequisitionCreateRequest request) {
        return R.ok(requisitionService.create(toRequisitionRequest(request)));
    }

    /**
     * 标记采购订单已结清。
     *
     * @param poId 采购订单ID
     * @return 无数据响应
     */
    @PostMapping("/orders/{poId}/settled")
    public R<Void> markSettled(@PathVariable("poId") Long poId) {
        orderService.markSettled(poId);
        return R.ok();
    }

    /**
     * 转换公共 Feign 创建请求为采购模块内部请求。
     *
     * @param request 公共创建请求
     * @return 采购模块内部请求
     */
    private PurchaseRequisitionRequest toRequisitionRequest(PurchaseRequisitionCreateRequest request) {
        PurchaseRequisitionRequest target = new PurchaseRequisitionRequest();
        target.setReqSource(request.getReqSource());
        target.setTitle(request.getTitle());
        target.setWarehouseId(request.getWarehouseId());
        target.setExpectDate(request.getExpectDate());
        target.setTotalAmount(request.getTotalAmount());
        target.setPriority(request.getPriority());
        target.setApplyUserId(request.getApplyUserId());
        target.setApplyUserName(request.getApplyUserName());
        target.setRemark(request.getRemark());
        target.setItems(toItems(request.getItems()));
        return target;
    }

    /**
     * 转换采购申请明细。
     *
     * @param items 公共明细请求
     * @return 采购模块内部明细请求
     */
    private List<PurchaseItemRequest> toItems(List<PurchaseRequisitionCreateItemRequest> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(item -> {
            PurchaseItemRequest target = new PurchaseItemRequest();
            target.setSkuId(item.getSkuId());
            target.setSkuCode(item.getSkuCode());
            target.setSkuName(item.getSkuName());
            target.setSpec(item.getSpec());
            target.setUnit(item.getUnit());
            target.setQuantity(item.getQuantity());
            target.setCurrentStock(item.getCurrentStock());
            target.setSafetyStock(item.getSafetyStock());
            target.setInTransitQty(item.getInTransitQty());
            target.setRefPrice(item.getRefPrice());
            target.setUnitPrice(item.getUnitPrice());
            target.setExpectDate(item.getExpectDate());
            target.setRemark(item.getRemark());
            return target;
        }).toList();
    }
}
