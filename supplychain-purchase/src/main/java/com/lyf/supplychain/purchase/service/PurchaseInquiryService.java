package com.lyf.supplychain.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.purchase.entity.PurchaseInquiry;
import com.lyf.supplychain.purchase.request.PurchaseInquiryPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseInquiryRequest;
import com.lyf.supplychain.purchase.request.PurchaseInquiryQuoteRequest;
import com.lyf.supplychain.purchase.vo.PurchaseInquiryCompareVO;

import java.util.List;

/**
 * 采购询价服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface PurchaseInquiryService extends IService<PurchaseInquiry> {

    /**
     * 创建询价单并写入询价明细。
     *
     * @param request 询价请求
     * @return 询价单ID
     */
    Long create(PurchaseInquiryRequest request);

    /**
     * 分页查询询价单列表。
     *
     * @param query 分页参数
     * @return 询价单分页结果
     */
    PageResult<PurchaseInquiry> pageInquiries(PurchaseInquiryPageQuery query);

    /**
     * 查询同一采购申请下的报价对比数据。
     *
     * @param reqId 采购申请ID
     * @return 询价报价列表
     */
    List<PurchaseInquiryCompareVO> compare(Long reqId);

    /**
     * 供应商提交询价报价。
     *
     * @param inquiryId 询价单ID
     * @param request   报价请求
     */
    void quote(Long inquiryId, PurchaseInquiryQuoteRequest request);

    /**
     * 选中某个询价报价，并将同组其他报价标记为未选中。
     *
     * @param inquiryId 询价单ID
     */
    void select(Long inquiryId);
}
