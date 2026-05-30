package com.lyf.supplychain.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.purchase.entity.PurchaseRequisition;
import com.lyf.supplychain.purchase.request.PurchaseAuditRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;

/**
 * 采购申请服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseRequisitionService extends IService<PurchaseRequisition> {

    /**
     * 分页查询采购申请单。
     *
     * @param query 查询条件
     * @return 采购申请分页结果
     */
    PageResult<PurchaseRequisition> pageRequisitions(PurchaseRequisitionPageQuery query);

    /**
     * 创建采购申请单和明细。
     *
     * @param request 创建请求
     * @return 采购申请单ID
     */
    Long create(PurchaseRequisitionRequest request);

    /**
     * 修改草稿状态的采购申请单。
     *
     * @param id      采购申请单ID
     * @param request 修改请求
     */
    void updateDraft(Long id, PurchaseRequisitionRequest request);

    /**
     * 删除草稿状态的采购申请单。
     *
     * @param id 采购申请单ID
     */
    void deleteDraft(Long id);

    /**
     * 提交采购申请进入待审批状态。
     *
     * @param id 采购申请单ID
     */
    void submit(Long id);

    /**
     * 审批通过采购申请。
     *
     * @param id      采购申请单ID
     * @param request 审批请求
     */
    void approve(Long id, PurchaseAuditRequest request);

    /**
     * 审批拒绝采购申请。
     *
     * @param id      采购申请单ID
     * @param request 审批请求
     */
    void reject(Long id, PurchaseAuditRequest request);
}
