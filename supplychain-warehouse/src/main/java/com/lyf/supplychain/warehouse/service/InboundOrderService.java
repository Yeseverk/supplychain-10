package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.InboundOrder;
import com.lyf.supplychain.warehouse.entity.InboundOrderItem;
import com.lyf.supplychain.warehouse.request.InboundConfirmRequest;
import com.lyf.supplychain.warehouse.request.InboundOrderRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;

import java.util.List;

/**
 * 入库单服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface InboundOrderService extends IService<InboundOrder> {

    /**
     * 分页查询入库单。
     *
     * @param query 分页参数
     * @return 入库单分页结果
     */
    PageResult<InboundOrder> pageInbound(WmsPageQuery query);

    /**
     * 查询入库单明细。
     *
     * @param id 入库单ID
     * @return 入库明细列表
     */
    List<InboundOrderItem> listItems(Long id);

    /**
     * 创建入库单。
     *
     * @param request 入库单请求
     * @return 入库单ID
     */
    Long create(InboundOrderRequest request);

    /**
     * 确认入库并更新库存、成本、库位和流水。
     *
     * @param id      入库单ID
     * @param request 确认请求
     */
    void confirm(Long id, InboundConfirmRequest request);
}
