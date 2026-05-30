package com.lyf.supplychain.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.request.PurchaseOrderPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseOrderRequest;
import com.lyf.supplychain.purchase.response.PurchaseOrderDetailResponse;

import java.util.List;

/**
 * 采购订单服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 分页查询采购订单。
     *
     * @param query 查询条件
     * @return 采购订单分页结果
     */
    PageResult<PurchaseOrder> pageOrders(PurchaseOrderPageQuery query);

    /**
     * Query purchase order detail with items and receipt records.
     *
     * @param id purchase order ID
     * @return purchase order detail
     */
    PurchaseOrderDetailResponse detail(Long id);

    /**
     * 创建采购订单和订单明细。
     *
     * @param request 创建请求
     * @return 采购订单ID
     */
    Long create(PurchaseOrderRequest request);

    /**
     * 修改草稿状态的采购订单。
     *
     * @param id      采购订单ID
     * @param request 修改请求
     */
    void updateDraft(Long id, PurchaseOrderRequest request);

    /**
     * 发送采购订单给供应商确认。
     *
     * @param id 采购订单ID
     */
    void send(Long id);

    /**
     * 确认采购订单。
     *
     * @param id 采购订单ID
     */
    void confirm(Long id);

    /**
     * 标记采购订单发货中。
     *
     * @param id 采购订单ID
     */
    void markShipping(Long id);

    /**
     * 标记采购订单已对账。
     *
     * @param id 采购订单ID
     */
    void reconcile(Long id);

    /**
     * 取消未完结的采购订单。
     *
     * @param id 采购订单ID
     */
    void cancel(Long id);

    /**
     * 查询供应商历史采购订单价格。
     *
     * @param supplierId 供应商ID
     * @param skuId      SKU ID
     * @return 历史采购订单列表
     */
    List<PurchaseOrder> priceHistory(Long supplierId, Long skuId);

    /**
     * 标记采购订单已结清。
     *
     * @param poId 采购订单ID
     */
    void markSettled(Long poId);
}
