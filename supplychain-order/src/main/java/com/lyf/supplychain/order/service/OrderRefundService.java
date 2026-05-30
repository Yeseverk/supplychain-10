package com.lyf.supplychain.order.service;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.order.entity.OrderRefund;
import com.lyf.supplychain.order.request.RefundAuditRequest;
import com.lyf.supplychain.order.request.RefundCreateRequest;
import com.lyf.supplychain.order.request.RefundPageQuery;

/**
 * 退款业务服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface OrderRefundService {

    /**
     * 分页查询退款单。
     *
     * @param query 分页参数
     * @return 退款分页结果
     */
    PageResult<OrderRefund> page(RefundPageQuery query);

    /**
     * 创建退款单并将订单转入售后中。
     *
     * @param request 退款请求
     * @return 退款单ID
     */
    Long create(RefundCreateRequest request);

    /**
     * 审核退款。
     *
     * @param id      退款单ID
     * @param request 审核请求
     */
    void audit(Long id, RefundAuditRequest request);

    /**
     * 确认收到退货。
     *
     * @param id 退款单ID
     */
    void received(Long id);

    /**
     * 完成退款。
     *
     * @param id 退款单ID
     */
    void complete(Long id);
}
