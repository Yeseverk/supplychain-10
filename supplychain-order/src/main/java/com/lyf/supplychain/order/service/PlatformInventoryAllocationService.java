package com.lyf.supplychain.order.service;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.order.entity.OrderItem;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationAdjustRequest;
import com.lyf.supplychain.order.request.PlatformInventoryAllocationRequest;

import java.util.List;

/**
 * 多平台库存分配服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformInventoryAllocationService {

    /**
     * 分页查询平台库存分配。
     *
     * @param query 分页参数
     * @return 平台库存分配分页数据
     */
    PageResult<PlatformInventoryAllocation> page(PageQuery query);

    /**
     * 创建平台库存分配并初始化可售库存。
     *
     * @param request 保存请求
     * @return 分配记录ID
     */
    Long create(PlatformInventoryAllocationRequest request);

    /**
     * 调整平台库存分配，并按已冻结和已售数量重算可售库存。
     *
     * @param id      分配记录ID
     * @param request 调整请求
     */
    void adjust(Long id, PlatformInventoryAllocationAdjustRequest request);

    /**
     * 同步单个平台库存到外部平台。
     *
     * @param id 分配记录ID
     */
    void sync(Long id);

    /**
     * 订单创建时冻结平台可售配额，使用 CAS 条件避免并发超卖。
     *
     * @param request 订单创建请求
     */
    void freezeForOrder(OrderCreateRequest request);

    /**
     * 订单取消时释放已冻结的平台库存。
     *
     * @param order 订单主表
     * @param items 订单明细
     */
    void releaseForCancel(OrderMain order, List<OrderItem> items);

    /**
     * 订单出库后确认平台库存售出。
     *
     * @param order 订单主表
     * @param items 订单明细
     */
    void confirmShipment(OrderMain order, List<OrderItem> items);
}
