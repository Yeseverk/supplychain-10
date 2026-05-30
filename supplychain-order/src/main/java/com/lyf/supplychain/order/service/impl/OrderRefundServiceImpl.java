package com.lyf.supplychain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.constant.OrderConstants;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.entity.OrderRefund;
import com.lyf.supplychain.order.mapper.OrderMainMapper;
import com.lyf.supplychain.order.mapper.OrderRefundMapper;
import com.lyf.supplychain.order.request.RefundAuditRequest;
import com.lyf.supplychain.order.request.RefundCreateRequest;
import com.lyf.supplychain.order.request.RefundPageQuery;
import com.lyf.supplychain.order.service.OrderNumberService;
import com.lyf.supplychain.order.service.OrderRefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 退款业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class OrderRefundServiceImpl implements OrderRefundService {

    private final OrderRefundMapper refundMapper;
    private final OrderMainMapper orderMapper;
    private final OrderNumberService numberService;

    public OrderRefundServiceImpl(OrderRefundMapper refundMapper, OrderMainMapper orderMapper, OrderNumberService numberService) {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.numberService = numberService;
    }

    /**
     * 分页查询退款单。
     *
     * @param query 分页参数
     * @return 退款分页结果
     */
    @Override
    public PageResult<OrderRefund> page(RefundPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<OrderRefund>()
                .orderByDesc(OrderRefund::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(OrderRefund::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(OrderRefund::getRefundNo, keyword)
                    .or().like(OrderRefund::getOrderNo, keyword)
                    .or().like(OrderRefund::getPlatformRefundNo, keyword)
                    .or().like(OrderRefund::getRefundReason, keyword)
                    .or().like(OrderRefund::getReasonDetail, keyword));
        }
        Page<OrderRefund> page = refundMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        return PageResult.from(page);
    }

    /**
     * 创建退款单并将订单转入售后中。
     *
     * @param request 退款请求
     * @return 退款单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RefundCreateRequest request) {
        OrderMain order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            BusinessException.throwException(15001, "订单不存在");
        }
        if (request.getRefundAmount().compareTo(order.getPaymentAmount()) > 0) {
            BusinessException.throwException(15005, "退款金额超过订单金额");
        }
        OrderRefund refund = new OrderRefund();
        refund.setTenantId(TenantContext.getTenantId());
        refund.setRefundNo(numberService.nextNo("REF"));
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundType(request.getRefundType());
        refund.setRefundReason(request.getRefundReason());
        refund.setReasonDetail(request.getReasonDetail());
        refund.setRefundAmount(request.getRefundAmount());
        refund.setCurrency(order.getCurrency());
        refund.setStatus(OrderConstants.REFUND_PENDING);
        refund.setApplyTime(LocalDateTime.now());
        refund.setEvidenceUrls(request.getEvidenceUrls());
        refundMapper.insert(refund);
        order.setStatus(OrderConstants.AFTER_SALE);
        orderMapper.updateById(order);
        return refund.getId();
    }

    /**
     * 审核退款。
     *
     * @param id      退款单ID
     * @param request 审核请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, RefundAuditRequest request) {
        OrderRefund refund = detail(id);
        refund.setAuditTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(request.getApproved())) {
            refund.setStatus(OrderConstants.REFUND_APPROVED);
            refund.setActualRefundAmount(request.getActualRefundAmount() == null ? refund.getRefundAmount() : request.getActualRefundAmount());
        } else {
            refund.setStatus(OrderConstants.REFUND_REJECTED);
            refund.setReasonDetail(request.getRejectReason());
        }
        refundMapper.updateById(refund);
    }

    /**
     * 确认收到退货。
     *
     * @param id 退款单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void received(Long id) {
        OrderRefund refund = detail(id);
        if (OrderConstants.REFUND_APPROVED != refund.getStatus()) {
            BusinessException.throwException(15002, "退款状态不允许此操作");
        }
        refund.setStatus(OrderConstants.REFUND_RETURN_RECEIVED);
        refundMapper.updateById(refund);
    }

    /**
     * 完成退款。
     *
     * @param id 退款单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        OrderRefund refund = detail(id);
        if (OrderConstants.REFUND_APPROVED != refund.getStatus() && OrderConstants.REFUND_RETURN_RECEIVED != refund.getStatus()) {
            BusinessException.throwException(15002, "退款状态不允许此操作");
        }
        refund.setStatus(OrderConstants.REFUND_COMPLETED);
        refund.setCompleteTime(LocalDateTime.now());
        refundMapper.updateById(refund);
    }

    private OrderRefund detail(Long id) {
        OrderRefund refund = refundMapper.selectById(id);
        if (refund == null) {
            BusinessException.throwException(15001, "退款单不存在");
        }
        return refund;
    }
}
