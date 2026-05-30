package com.lyf.supplychain.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.purchase.entity.PurchaseReceipt;
import com.lyf.supplychain.purchase.request.PurchaseReceiptPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseReceiptRequest;

/**
 * 采购收货服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseReceiptService extends IService<PurchaseReceipt> {

    /**
     * 创建采购收货单和收货明细。
     *
     * @param request 收货请求
     * @return 收货单ID
     */
    Long create(PurchaseReceiptRequest request);

    /**
     * 分页查询采购收货单。
     *
     * @param query 分页参数
     * @return 收货单分页结果
     */
    PageResult<PurchaseReceipt> pageReceipts(PurchaseReceiptPageQuery query);

    /**
     * 确认入库，并通过 Seata 联动仓储库存和财务应付账款。
     *
     * @param receiptId 收货单ID
     */
    void confirmInbound(Long receiptId);
}
