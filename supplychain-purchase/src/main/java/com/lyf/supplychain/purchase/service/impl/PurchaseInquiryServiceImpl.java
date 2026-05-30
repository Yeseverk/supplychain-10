package com.lyf.supplychain.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.supplier.SupplierBriefResponse;
import com.lyf.supplychain.common.feign.supplier.SupplierFeignClient;
import com.lyf.supplychain.purchase.entity.PurchaseInquiry;
import com.lyf.supplychain.purchase.entity.PurchaseInquiryItem;
import com.lyf.supplychain.purchase.mapper.PurchaseInquiryItemMapper;
import com.lyf.supplychain.purchase.mapper.PurchaseInquiryMapper;
import com.lyf.supplychain.purchase.request.PurchaseInquiryPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseInquiryRequest;
import com.lyf.supplychain.purchase.request.PurchaseInquiryQuoteItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseInquiryQuoteRequest;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.model.PurchaseInquiryScore;
import com.lyf.supplychain.purchase.service.PurchaseInquiryScoreCalculator;
import com.lyf.supplychain.purchase.service.PurchaseInquiryService;
import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import com.lyf.supplychain.purchase.vo.PurchaseInquiryCompareVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 采购询价服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseInquiryServiceImpl extends ServiceImpl<PurchaseInquiryMapper, PurchaseInquiry>
        implements PurchaseInquiryService {

    private final PurchaseInquiryItemMapper itemMapper;
    private final PurchaseNumberService numberService;
    private final SupplierFeignClient supplierFeignClient;
    private final PurchaseInquiryScoreCalculator scoreCalculator;

    public PurchaseInquiryServiceImpl(PurchaseInquiryItemMapper itemMapper,
                                      PurchaseNumberService numberService,
                                      SupplierFeignClient supplierFeignClient,
                                      PurchaseInquiryScoreCalculator scoreCalculator) {
        this.itemMapper = itemMapper;
        this.numberService = numberService;
        this.supplierFeignClient = supplierFeignClient;
        this.scoreCalculator = scoreCalculator;
    }

    /**
     * 创建询价单并写入询价明细。
     *
     * @param request 询价请求
     * @return 询价单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PurchaseInquiryRequest request) {
        PurchaseInquiry inquiry = new PurchaseInquiry();
        BeanUtils.copyProperties(request, inquiry);
        inquiry.setTenantId(TenantContext.getTenantId());
        inquiry.setInquiryNo(numberService.nextNo("INQ"));
        inquiry.setStatus(0);
        inquiry.setSendTime(LocalDateTime.now());
        save(inquiry);
        for (PurchaseItemRequest itemRequest : request.getItems()) {
            PurchaseInquiryItem item = new PurchaseInquiryItem();
            BeanUtils.copyProperties(itemRequest, item);
            item.setTenantId(TenantContext.getTenantId());
            item.setInquiryId(inquiry.getId());
            item.setInquiryQty(itemRequest.getQuantity());
            itemMapper.insert(item);
        }
        return inquiry.getId();
    }

    /**
     * 分页查询询价单列表。
     *
     * @param query 分页参数
     * @return 询价单分页结果
     */
    @Override
    public PageResult<PurchaseInquiry> pageInquiries(PurchaseInquiryPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<PurchaseInquiry> wrapper = new LambdaQueryWrapper<PurchaseInquiry>()
                .orderByDesc(PurchaseInquiry::getCreateTime);
        if (query.getStatus() != null) {
            wrapper.eq(PurchaseInquiry::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(PurchaseInquiry::getInquiryNo, keyword)
                    .or().like(PurchaseInquiry::getSupplierName, keyword)
                    .or().like(PurchaseInquiry::getRemark, keyword));
        }
        Page<PurchaseInquiry> page = page(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        return PageResult.from(page);
    }

    /**
     * 查询同一采购申请下的报价对比数据。
     *
     * @param reqId 采购申请ID
     * @return 询价报价列表
     */
    @Override
    public List<PurchaseInquiryCompareVO> compare(Long reqId) {
        List<PurchaseInquiry> inquiries = list(new LambdaQueryWrapper<PurchaseInquiry>()
                .eq(PurchaseInquiry::getReqId, reqId)
                .in(PurchaseInquiry::getStatus, 1, 2, 3)
                .isNotNull(PurchaseInquiry::getTotalQuoteAmt));
        if (inquiries.isEmpty()) {
            return List.of();
        }
        BigDecimal bestQuoteAmount = inquiries.stream()
                .map(PurchaseInquiry::getTotalQuoteAmt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        Map<Long, List<PurchaseInquiryItem>> itemMap = itemMapper.selectList(new LambdaQueryWrapper<PurchaseInquiryItem>()
                        .in(PurchaseInquiryItem::getInquiryId, inquiries.stream().map(PurchaseInquiry::getId).toList()))
                .stream()
                .collect(Collectors.groupingBy(PurchaseInquiryItem::getInquiryId));
        List<PurchaseInquiryCompareVO> result = inquiries.stream()
                .map(inquiry -> toCompareVO(inquiry, bestQuoteAmount, itemMap.getOrDefault(inquiry.getId(), List.of())))
                .sorted(Comparator.comparing(PurchaseInquiryCompareVO::getTotalScore).reversed())
                .toList();
        if (!result.isEmpty()) {
            result.get(0).setRecommended(true);
            result.stream().skip(1).forEach(item -> item.setRecommended(false));
        }
        return result;
    }

    /**
     * 供应商提交询价报价。
     *
     * @param inquiryId 询价单ID
     * @param request   报价请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quote(Long inquiryId, PurchaseInquiryQuoteRequest request) {
        PurchaseInquiry inquiry = getById(inquiryId);
        if (inquiry == null) {
            BusinessException.throwException("询价单不存在");
        }
        if (inquiry.getQuoteDeadline() != null && inquiry.getQuoteDeadline().isBefore(LocalDateTime.now())) {
            BusinessException.throwException("报价已过期，请重新询价");
        }
        Map<Long, PurchaseInquiryItem> existsItemMap = itemMapper.selectList(new LambdaQueryWrapper<PurchaseInquiryItem>()
                        .eq(PurchaseInquiryItem::getInquiryId, inquiryId))
                .stream()
                .collect(Collectors.toMap(PurchaseInquiryItem::getId, Function.identity()));
        BigDecimal totalQuoteAmount = BigDecimal.ZERO;
        for (PurchaseInquiryQuoteItemRequest itemRequest : request.getItems()) {
            PurchaseInquiryItem item = existsItemMap.get(itemRequest.getInquiryItemId());
            if (item == null) {
                BusinessException.throwException("询价明细不存在");
            }
            item.setQuotedPrice(itemRequest.getQuotedPrice());
            item.setQuotedQty(itemRequest.getQuotedQty());
            item.setDeliveryDays(itemRequest.getDeliveryDays());
            item.setMinOrderQty(itemRequest.getMinOrderQty());
            item.setRemark(itemRequest.getRemark());
            itemMapper.updateById(item);
            totalQuoteAmount = totalQuoteAmount.add(itemRequest.getQuotedPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuotedQty())));
        }
        LocalDateTime quotedTime = LocalDateTime.now();
        PurchaseInquiry update = new PurchaseInquiry();
        update.setStatus(1);
        update.setQuotedTime(quotedTime);
        update.setResponseHours(calcResponseHours(inquiry.getSendTime(), quotedTime));
        update.setTotalQuoteAmt(totalQuoteAmount);
        update.setQuoteValidDays(request.getQuoteValidDays());
        update.setQuoteExpireDate(request.getQuoteValidDays() == null ? null : LocalDate.now().plusDays(request.getQuoteValidDays()));
        update.setSupplierRemark(request.getSupplierRemark());
        boolean updated = update(new LambdaUpdateWrapper<PurchaseInquiry>()
                .eq(PurchaseInquiry::getId, inquiryId)
                .in(PurchaseInquiry::getStatus, 0, 1)
                .set(PurchaseInquiry::getStatus, update.getStatus())
                .set(PurchaseInquiry::getQuotedTime, update.getQuotedTime())
                .set(PurchaseInquiry::getResponseHours, update.getResponseHours())
                .set(PurchaseInquiry::getTotalQuoteAmt, update.getTotalQuoteAmt())
                .set(PurchaseInquiry::getQuoteValidDays, update.getQuoteValidDays())
                .set(PurchaseInquiry::getQuoteExpireDate, update.getQuoteExpireDate())
                .set(PurchaseInquiry::getSupplierRemark, update.getSupplierRemark()));
        if (!updated) {
            BusinessException.throwException("当前询价单状态不允许报价");
        }
    }

    /**
     * 选中某个询价报价，并将同组其他报价标记为未选中。
     *
     * @param inquiryId 询价单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void select(Long inquiryId) {
        PurchaseInquiry inquiry = getById(inquiryId);
        if (inquiry == null) {
            BusinessException.throwException("询价单不存在");
        }
        if (!Objects.equals(inquiry.getStatus(), 1)) {
            BusinessException.throwException("询价未收到报价，无法生成采购单");
        }
        if (inquiry.getQuoteExpireDate() != null && inquiry.getQuoteExpireDate().isBefore(LocalDate.now())) {
            BusinessException.throwException("报价已过期，请重新询价");
        }
        update(new LambdaUpdateWrapper<PurchaseInquiry>()
                .eq(PurchaseInquiry::getReqId, inquiry.getReqId())
                .ne(PurchaseInquiry::getId, inquiryId)
                .set(PurchaseInquiry::getStatus, 3));
        update(new LambdaUpdateWrapper<PurchaseInquiry>()
                .eq(PurchaseInquiry::getId, inquiryId)
                .set(PurchaseInquiry::getStatus, 2));
    }

    private PurchaseInquiryCompareVO toCompareVO(PurchaseInquiry inquiry,
                                                 BigDecimal bestQuoteAmount,
                                                 List<PurchaseInquiryItem> items) {
        String supplierGrade = querySupplierGrade(inquiry.getSupplierId());
        PurchaseInquiryScore score = scoreCalculator.calculate(bestQuoteAmount,
                inquiry.getTotalQuoteAmt(),
                maxDeliveryDays(items),
                supplierGrade);
        PurchaseInquiryCompareVO vo = new PurchaseInquiryCompareVO();
        vo.setInquiryId(inquiry.getId());
        vo.setInquiryNo(inquiry.getInquiryNo());
        vo.setSupplierId(inquiry.getSupplierId());
        vo.setSupplierName(inquiry.getSupplierName());
        vo.setSupplierGrade(supplierGrade);
        vo.setQuoteAmount(inquiry.getTotalQuoteAmt());
        vo.setDeliveryDays(maxDeliveryDays(items));
        vo.setQuoteExpireDate(inquiry.getQuoteExpireDate());
        vo.setPriceScore(score.getPriceScore());
        vo.setDeliveryScore(score.getDeliveryScore());
        vo.setSupplierGradeScore(score.getSupplierGradeScore());
        vo.setTotalScore(score.getTotalScore());
        vo.setRecommended(false);
        return vo;
    }

    private String querySupplierGrade(Long supplierId) {
        try {
            R<SupplierBriefResponse> response = supplierFeignClient.getBrief(supplierId);
            return Optional.ofNullable(response)
                    .map(R::getData)
                    .map(SupplierBriefResponse::getGrade)
                    .orElse("C");
        } catch (RuntimeException exception) {
            return "C";
        }
    }

    private Integer maxDeliveryDays(List<PurchaseInquiryItem> items) {
        return items.stream()
                .map(PurchaseInquiryItem::getDeliveryDays)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private BigDecimal calcResponseHours(LocalDateTime sendTime, LocalDateTime quotedTime) {
        if (sendTime == null || quotedTime == null) {
            return null;
        }
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(sendTime, quotedTime))
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }
}
