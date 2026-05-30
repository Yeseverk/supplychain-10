package com.lyf.supplychain.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableOffsetRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePaymentRequest;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.entity.FinancePaymentRecord;
import com.lyf.supplychain.finance.request.FinancePayablePageQuery;

import java.util.List;

/**
 * 应付账款服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface FinancePayableService extends IService<FinancePayable> {

    /**
     * 根据采购订单创建应付账款，重复调用时直接返回已有记录。
     *
     * @param request 创建请求
     * @return 应付账款ID
     */
    Long createFromPurchaseOrder(FinancePayableCreateRequest request);

    /**
     * 根据物流账单批次创建应付账款，重复调用时直接返回已有记录。
     *
     * @param request 物流账单应付创建请求
     * @return 应付账款ID
     */
    Long createFromLogisticsBill(FinanceLogisticsPayableCreateRequest request);

    /**
     * 分页查询应付账款。
     *
     * @param query 分页参数
     * @return 应付账款分页结果
     */
    PageResult<FinancePayable> pagePayables(FinancePayablePageQuery query);

    /**
     * 登记付款并更新应付账款状态。
     *
     * @param payableId 应付账款ID
     * @param request   付款请求
     * @return 关联采购单ID
     */
    Long pay(Long payableId, FinancePaymentRequest request);

    /**
     * 查询应付账款付款记录。
     *
     * @param payableId 应付账款ID
     * @return 付款记录列表
     */
    List<FinancePaymentRecord> payments(Long payableId);

    /**
     * 根据采购退货冲减应付账款。
     *
     * @param request 应付账款冲减请求
     * @return 应付账款ID
     */
    Long offsetForPurchaseReturn(FinancePayableOffsetRequest request);
}
