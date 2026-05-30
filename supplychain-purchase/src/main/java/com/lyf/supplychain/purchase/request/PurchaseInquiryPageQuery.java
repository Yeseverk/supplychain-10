package com.lyf.supplychain.purchase.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购询价分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseInquiryPageQuery extends PageQuery {

    /**
     * 询价单号、供应商名称或备注关键词。
     */
    private String keyword;

    /**
     * 询价状态。
     */
    private Integer status;
}
