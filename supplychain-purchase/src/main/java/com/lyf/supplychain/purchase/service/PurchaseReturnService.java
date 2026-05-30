package com.lyf.supplychain.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.purchase.entity.PurchaseReturn;
import com.lyf.supplychain.purchase.request.PurchaseReturnRequest;

/**
 * 采购退货服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseReturnService extends IService<PurchaseReturn> {

    /**
     * 创建采购退货单。
     *
     * @param request 退货请求
     * @return 退货单ID
     */
    Long create(PurchaseReturnRequest request);

    /**
     * 分页查询采购退货单。
     *
     * @param query 分页参数
     * @return 退货单分页结果
     */
    PageResult<PurchaseReturn> pageReturns(PageQuery query);

    /**
     * 确认退货出库，并通过 Feign 联动仓储扣减库存。
     *
     * @param returnId 退货单ID
     */
    void ship(Long returnId);
}
