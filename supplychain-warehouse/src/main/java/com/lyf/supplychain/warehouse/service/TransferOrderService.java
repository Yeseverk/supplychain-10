package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.TransferOrder;
import com.lyf.supplychain.warehouse.request.TransferOrderRequest;

/**
 * 仓库调拨服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface TransferOrderService extends IService<TransferOrder> {

    /**
     * 分页查询调拨单。
     *
     * @param query 分页参数
     * @return 调拨单分页结果
     */
    PageResult<TransferOrder> pageTransfers(PageQuery query);

    /**
     * 创建调拨单。
     *
     * @param request 调拨请求
     * @return 调拨单ID
     */
    Long create(TransferOrderRequest request);

    /**
     * 审核通过调拨单。
     *
     * @param id 调拨单ID
     */
    void approve(Long id);

    /**
     * 确认调拨发货，扣减来源仓并增加目标仓在途库存。
     *
     * @param id 调拨单ID
     */
    void ship(Long id);

    /**
     * 确认调拨到货，减少在途库存并增加目标仓实物库存。
     *
     * @param id 调拨单ID
     */
    void receive(Long id);
}
