package com.lyf.supplychain.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.purchase.constant.PurchaseStatus;
import com.lyf.supplychain.purchase.entity.PurchaseRequisition;
import com.lyf.supplychain.purchase.entity.PurchaseRequisitionItem;
import com.lyf.supplychain.purchase.mapper.PurchaseRequisitionItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseRequisitionMapper;
import com.lyf.supplychain.purchase.request.PurchaseAuditRequest;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import com.lyf.supplychain.purchase.model.PurchaseApprovalDecision;
import com.lyf.supplychain.purchase.service.PurchaseApprovalPolicy;
import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import com.lyf.supplychain.purchase.service.PurchaseRequisitionDuplicateChecker;
import com.lyf.supplychain.purchase.service.PurchaseRequisitionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 采购申请服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseRequisitionServiceImpl extends ServiceImpl<PurchaseRequisitionMapper, PurchaseRequisition>
        implements PurchaseRequisitionService {

    private final PurchaseRequisitionItemMapper itemMapper;
    private final PurchaseNumberService numberService;
    private final PurchaseRequisitionDuplicateChecker duplicateChecker;
    private final PurchaseApprovalPolicy approvalPolicy;

    public PurchaseRequisitionServiceImpl(PurchaseRequisitionItemMapper itemMapper,
                                          PurchaseNumberService numberService,
                                          PurchaseRequisitionDuplicateChecker duplicateChecker,
                                          PurchaseApprovalPolicy approvalPolicy) {
        this.itemMapper = itemMapper;
        this.numberService = numberService;
        this.duplicateChecker = duplicateChecker;
        this.approvalPolicy = approvalPolicy;
    }

    /**
     * 分页查询采购申请单。
     *
     * @param query 查询条件
     * @return 采购申请分页结果
     */
    @Override
    public PageResult<PurchaseRequisition> pageRequisitions(PurchaseRequisitionPageQuery query) {
        query.normalize();
        Page<PurchaseRequisition> page = page(Page.of(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<PurchaseRequisition>()
                        .eq(Objects.nonNull(query.getStatus()), PurchaseRequisition::getStatus, query.getStatus())
                        .like(Objects.nonNull(query.getReqNo()), PurchaseRequisition::getReqNo, query.getReqNo())
                        .like(Objects.nonNull(query.getTitle()), PurchaseRequisition::getTitle, query.getTitle())
                        .orderByDesc(PurchaseRequisition::getCreateTime));
        return PageResult.from(page);
    }

    /**
     * 创建采购申请单和明细。
     *
     * @param request 创建请求
     * @return 采购申请单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseRequisitionRequest request) {
        duplicateChecker.check(request);
        PurchaseRequisition requisition = new PurchaseRequisition();
        BeanUtils.copyProperties(request, requisition);
        requisition.setReqNo(numberService.nextNo("REQ"));
        requisition.setReqSource(defaultValue(request.getReqSource(), 3));
        requisition.setPriority(defaultValue(request.getPriority(), 2));
        requisition.setStatus(PurchaseStatus.DRAFT);
        requisition.setApplyTime(LocalDateTime.now());
        requisition.setTenantId(TenantContext.getTenantId());
        requisition.setTotalAmount(defaultAmount(request.getTotalAmount()));
        save(requisition);
        saveItems(requisition.getId(), request);
        return requisition.getId();
    }

    /**
     * 修改草稿状态的采购申请单。
     *
     * @param id      采购申请单ID
     * @param request 修改请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long id, PurchaseRequisitionRequest request) {
        PurchaseRequisition requisition = mustGet(id);
        ensureStatus(requisition, PurchaseStatus.DRAFT, "只有草稿状态的采购申请可以修改");
        BeanUtils.copyProperties(request, requisition);
        requisition.setId(id);
        requisition.setTotalAmount(defaultAmount(request.getTotalAmount()));
        updateById(requisition);
        itemMapper.delete(new LambdaQueryWrapper<PurchaseRequisitionItem>().eq(PurchaseRequisitionItem::getReqId, id));
        saveItems(id, request);
    }

    /**
     * 删除草稿状态的采购申请单。
     *
     * @param id 采购申请单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long id) {
        PurchaseRequisition requisition = mustGet(id);
        ensureStatus(requisition, PurchaseStatus.DRAFT, "只有草稿状态的采购申请可以删除");
        removeById(id);
        itemMapper.delete(new LambdaQueryWrapper<PurchaseRequisitionItem>().eq(PurchaseRequisitionItem::getReqId, id));
    }

    /**
     * 提交采购申请进入待审批状态。
     *
     * @param id 采购申请单ID
     */
    @Override
    public void submit(Long id) {
        PurchaseRequisition requisition = mustGet(id);
        ensureStatus(requisition, PurchaseStatus.DRAFT, "只有草稿状态的采购申请可以提交");
        PurchaseApprovalDecision decision = approvalPolicy.decide(requisition.getTotalAmount());
        Integer targetStatus = decision.isAutoApprove() ? PurchaseStatus.APPROVED : PurchaseStatus.PENDING;
        boolean updated = update(new LambdaUpdateWrapper<PurchaseRequisition>()
                .eq(PurchaseRequisition::getId, id)
                .eq(PurchaseRequisition::getStatus, PurchaseStatus.DRAFT)
                .set(PurchaseRequisition::getStatus, targetStatus)
                .set(PurchaseRequisition::getApprovalLevel, decision.getApprovalLevel())
                .set(PurchaseRequisition::getApprovalRole, decision.getApprovalRole())
                .set(PurchaseRequisition::getApplyTime, LocalDateTime.now())
                .set(decision.isAutoApprove(), PurchaseRequisition::getAuditUserId, 0L)
                .set(decision.isAutoApprove(), PurchaseRequisition::getAuditTime, LocalDateTime.now())
                .set(decision.isAutoApprove(), PurchaseRequisition::getAuditRemark, decision.getDescription()));
        if (!updated) {
            BusinessException.throwException("只有草稿状态的采购申请可以提交");
        }
    }

    /**
     * 审批通过采购申请。
     *
     * @param id      采购申请单ID
     * @param request 审批请求
     */
    @Override
    public void approve(Long id, PurchaseAuditRequest request) {
        audit(id, request, PurchaseStatus.APPROVED);
    }

    /**
     * 审批拒绝采购申请。
     *
     * @param id      采购申请单ID
     * @param request 审批请求
     */
    @Override
    public void reject(Long id, PurchaseAuditRequest request) {
        audit(id, request, PurchaseStatus.REJECTED);
    }

    private void audit(Long id, PurchaseAuditRequest request, Integer targetStatus) {
        boolean updated = update(new LambdaUpdateWrapper<PurchaseRequisition>()
                .eq(PurchaseRequisition::getId, id)
                .eq(PurchaseRequisition::getStatus, PurchaseStatus.PENDING)
                .set(PurchaseRequisition::getStatus, targetStatus)
                .set(PurchaseRequisition::getAuditUserId, request.getAuditUserId())
                .set(PurchaseRequisition::getAuditTime, LocalDateTime.now())
                .set(PurchaseRequisition::getAuditRemark, request.getAuditRemark()));
        if (!updated) {
            BusinessException.throwException("只有待审批状态的采购申请可以审批");
        }
    }

    private void saveItems(Long reqId, PurchaseRequisitionRequest request) {
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            PurchaseRequisitionItem item = new PurchaseRequisitionItem();
            BeanUtils.copyProperties(itemRequest, item);
            item.setReqId(reqId);
            item.setTenantId(TenantContext.getTenantId());
            item.setCurrentStock(defaultValue(itemRequest.getCurrentStock(), 0));
            item.setSafetyStock(defaultValue(itemRequest.getSafetyStock(), 0));
            item.setInTransitQty(defaultValue(itemRequest.getInTransitQty(), 0));
            itemMapper.insert(item);
        }
    }

    private PurchaseRequisition mustGet(Long id) {
        PurchaseRequisition requisition = getById(id);
        if (requisition == null) {
            BusinessException.throwException("采购申请不存在");
        }
        return requisition;
    }

    private void ensureStatus(PurchaseRequisition requisition, Integer status, String message) {
        if (!Objects.equals(requisition.getStatus(), status)) {
            BusinessException.throwException(message);
        }
    }

    private Integer defaultValue(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
