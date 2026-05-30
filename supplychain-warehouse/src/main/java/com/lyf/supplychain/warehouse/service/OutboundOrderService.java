package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.OutboundOrder;
import com.lyf.supplychain.warehouse.entity.OutboundOrderItem;
import com.lyf.supplychain.warehouse.request.OutboundOrderRequest;
import com.lyf.supplychain.warehouse.request.PickProgressRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;

import java.util.List;

/**
 * 出库单服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface OutboundOrderService extends IService<OutboundOrder> {

    /**
     * 分页查询出库单。
     *
     * @param query 分页参数
     * @return 出库单分页结果
     */
    PageResult<OutboundOrder> pageOutbound(WmsPageQuery query);

    /**
     * 创建出库单并按 FIFO 分配库位。
     *
     * @param request 创建请求
     * @return 出库单ID
     */
    Long create(OutboundOrderRequest request);

    /**
     * 查询拣货单明细。
     *
     * @param id 出库单ID
     * @return 拣货明细
     */
    List<OutboundOrderItem> pickList(Long id);

    /**
     * 更新拣货进度。
     *
     * @param id      出库单ID
     * @param request 拣货进度请求
     */
    void updatePick(Long id, PickProgressRequest request);

    /**
     * 确认出库并扣减库存。
     *
     * @param id 出库单ID
     */
    void confirm(Long id);
}
