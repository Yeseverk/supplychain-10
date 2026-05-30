package com.lyf.supplychain.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.finance.FinanceLogisticsPayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableCreateRequest;
import com.lyf.supplychain.common.feign.finance.FinancePayableOffsetRequest;
import com.lyf.supplychain.common.feign.finance.FinancePaymentRequest;
import com.lyf.supplychain.common.feign.purchase.PurchaseFeignClient;
import com.lyf.supplychain.finance.entity.FinancePayable;
import com.lyf.supplychain.finance.entity.FinancePaymentRecord;
import com.lyf.supplychain.finance.mapper.FinancePayableMapper;
import com.lyf.supplychain.finance.mapper.FinancePaymentRecordMapper;
import com.lyf.supplychain.finance.request.FinancePayablePageQuery;
import com.lyf.supplychain.finance.service.FinanceNumberService;
import com.lyf.supplychain.finance.service.FinancePayableService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 应付账款服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class FinancePayableServiceImpl extends ServiceImpl<FinancePayableMapper, FinancePayable>
        implements FinancePayableService {

    private static final int STATUS_PENDING_RECONCILE = 0;

    private static final int STATUS_PENDING_PAYMENT = 1;

    private static final int STATUS_PARTIAL_PAYMENT = 2;

    private static final int STATUS_SETTLED = 3;

    private static final String SOURCE_TYPE_PURCHASE_ORDER = "PURCHASE_ORDER";

    private static final String SOURCE_TYPE_TMS_LOGISTICS_BILL = "TMS_LOGISTICS_BILL";

    private final FinancePaymentRecordMapper paymentRecordMapper;
    private final FinanceNumberService numberService;
    private final PurchaseFeignClient purchaseFeignClient;
    private final FinancePayableOffsetPolicy offsetPolicy = new FinancePayableOffsetPolicy();

    public FinancePayableServiceImpl(FinancePaymentRecordMapper paymentRecordMapper,
                                     FinanceNumberService numberService,
                                     PurchaseFeignClient purchaseFeignClient) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.numberService = numberService;
        this.purchaseFeignClient = purchaseFeignClient;
    }

    /**
     * 根据采购订单创建应付账款，重复调用时直接返回已有记录。
     *
     * @param request 创建请求
     * @return 应付账款ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromPurchaseOrder(FinancePayableCreateRequest request) {
        FinancePayable exists = getOne(new LambdaQueryWrapper<FinancePayable>()
                .eq(FinancePayable::getPoId, request.getPoId())
                .last("limit 1"));
        if (exists != null) {
            return exists.getId();
        }
        FinancePayable payable = new FinancePayable();
        BeanUtils.copyProperties(request, payable);
        payable.setSourceType(SOURCE_TYPE_PURCHASE_ORDER);
        payable.setSourceBizNo(request.getPoNo());
        payable.setPayableNo(numberService.nextPayableNo());
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setCurrency(request.getCurrency() == null ? "CNY" : request.getCurrency());
        payable.setPaymentDays(request.getPaymentDays() == null ? 0 : request.getPaymentDays());
        payable.setInvoiceDate(request.getInvoiceDate() == null ? LocalDate.now() : request.getInvoiceDate());
        payable.setDueDate(payable.getInvoiceDate().plusDays(payable.getPaymentDays()));
        payable.setStatus(STATUS_PENDING_RECONCILE);
        payable.setOverdueDays(0);
        save(payable);
        return payable.getId();
    }

    /**
     * 根据物流账单批次创建应付账款，重复调用时直接返回已有记录。
     *
     * @param request 物流账单应付创建请求
     * @return 应付账款ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromLogisticsBill(FinanceLogisticsPayableCreateRequest request) {
        FinancePayable exists = getOne(new LambdaQueryWrapper<FinancePayable>()
                .eq(FinancePayable::getSourceType, SOURCE_TYPE_TMS_LOGISTICS_BILL)
                .eq(FinancePayable::getSourceBizNo, request.getBillBatchNo())
                .last("limit 1"));
        if (exists != null) {
            return exists.getId();
        }
        FinancePayable payable = new FinancePayable();
        payable.setTenantId(request.getTenantId());
        payable.setSourceType(SOURCE_TYPE_TMS_LOGISTICS_BILL);
        payable.setSourceBizNo(request.getBillBatchNo());
        payable.setPayableNo(numberService.nextPayableNo());
        payable.setSupplierName(request.getCarrierCode());
        payable.setInvoiceNo(request.getBillBatchNo());
        payable.setInvoiceDate(request.getInvoiceDate() == null ? LocalDate.now() : request.getInvoiceDate());
        payable.setPayableAmount(request.getPayableAmount());
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setCurrency(request.getCurrency() == null ? "CNY" : request.getCurrency());
        payable.setPaymentDays(request.getPaymentDays() == null ? 0 : request.getPaymentDays());
        payable.setDueDate(payable.getInvoiceDate().plusDays(payable.getPaymentDays()));
        payable.setStatus(STATUS_PENDING_RECONCILE);
        payable.setOverdueDays(0);
        payable.setRemark("物流账单自动生成应付，账单批次：" + request.getBillBatchNo());
        save(payable);
        return payable.getId();
    }

    /**
     * 分页查询应付账款。
     *
     * @param query 分页参数
     * @return 应付账款分页结果
     */
    @Override
    public PageResult<FinancePayable> pagePayables(FinancePayablePageQuery query) {
        query.normalize();
        LambdaQueryWrapper<FinancePayable> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(FinancePayable::getPayableNo, query.getKeyword())
                    .or().like(FinancePayable::getSourceBizNo, query.getKeyword())
                    .or().like(FinancePayable::getSupplierName, query.getKeyword())
                    .or().like(FinancePayable::getInvoiceNo, query.getKeyword())
                    .or().like(FinancePayable::getRemark, query.getKeyword())
            );
        }
        if (query.getStatus() != null) {
            wrapper.eq(FinancePayable::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(FinancePayable::getCreateTime);
        Page<FinancePayable> page = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    /**
     * 登记付款并更新应付账款状态。
     *
     * @param payableId 应付账款ID
     * @param request   付款请求
     * @return 关联采购单ID
     */
    @Override
    @GlobalTransactional(name = "finance-pay-purchase-settle", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long pay(Long payableId, FinancePaymentRequest request) {
        FinancePayable payable = getById(payableId);
        if (payable == null) {
            BusinessException.throwException("应付账款不存在");
        }
        if (request == null || request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            BusinessException.throwException("付款金额必须大于0");
        }
        BigDecimal payableAmount = payable.getPayableAmount() == null ? BigDecimal.ZERO : payable.getPayableAmount();
        BigDecimal currentPaidAmount = payable.getPaidAmount() == null ? BigDecimal.ZERO : payable.getPaidAmount();
        BigDecimal remainingAmount = payableAmount.subtract(currentPaidAmount);
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0 || Integer.valueOf(STATUS_SETTLED).equals(payable.getStatus())) {
            BusinessException.throwException("应付账款已结清，无需重复付款");
        }
        if (request.getPaymentAmount().compareTo(remainingAmount) > 0) {
            BusinessException.throwException("付款金额不能超过剩余应付金额");
        }
        FinancePaymentRecord record = new FinancePaymentRecord();
        BeanUtils.copyProperties(request, record);
        record.setTenantId(payable.getTenantId());
        record.setPayableId(payableId);
        paymentRecordMapper.insert(record);
        BigDecimal paidAmount = currentPaidAmount.add(request.getPaymentAmount());
        payable.setPaidAmount(paidAmount);
        payable.setStatus(paidAmount.compareTo(payableAmount) >= 0 ? STATUS_SETTLED : STATUS_PARTIAL_PAYMENT);
        updateById(payable);
        if (payable.getStatus() == STATUS_SETTLED && payable.getPoId() != null) {
            assertSuccess(purchaseFeignClient.markSettled(payable.getPoId()), "PMS采购单结清回写失败");
        }
        return payable.getPoId();
    }

    private void assertSuccess(R<?> response, String message) {
        if (response == null || response.getCode() == null || response.getCode() != 200) {
            BusinessException.throwException(message + "：" + (response == null ? "无响应" : response.getMsg()));
        }
    }

    @Override
    public List<FinancePaymentRecord> payments(Long payableId) {
        return paymentRecordMapper.selectList(new LambdaQueryWrapper<FinancePaymentRecord>()
                .eq(FinancePaymentRecord::getPayableId, payableId)
                .orderByDesc(FinancePaymentRecord::getCreateTime));
    }

    /**
     * 根据采购退货冲减应付账款。
     *
     * @param request 应付账款冲减请求
     * @return 应付账款ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long offsetForPurchaseReturn(FinancePayableOffsetRequest request) {
        FinancePayable payable = getOne(new LambdaQueryWrapper<FinancePayable>()
                .eq(FinancePayable::getPoId, request.getPoId())
                .last("limit 1"));
        if (payable == null) {
            return null;
        }
        if (request.getOffsetAmount() == null || request.getOffsetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            BusinessException.throwException("冲减金额必须大于0");
        }
        BigDecimal newPayableAmount = offsetPolicy.offset(payable.getPayableAmount(), request.getOffsetAmount(), payable.getPaidAmount());
        payable.setPayableAmount(newPayableAmount);
        payable.setStatus(payable.getPaidAmount().compareTo(newPayableAmount) >= 0 ? STATUS_SETTLED : payable.getStatus());
        payable.setRemark((payable.getRemark() == null ? "" : payable.getRemark() + "；")
                + "采购退货冲减，退货单：" + request.getReturnNo()
                + "，冲减金额：" + request.getOffsetAmount()
                + "，原因：" + request.getReason());
        updateById(payable);
        if (payable.getStatus() == STATUS_SETTLED && payable.getPoId() != null) {
            assertSuccess(purchaseFeignClient.markSettled(payable.getPoId()), "PMS采购单结清回写失败");
        }
        return payable.getId();
    }
}
