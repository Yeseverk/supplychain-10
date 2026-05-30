package com.lyf.supplychain.purchase.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购订单分页查询。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderPageQuery extends PageQuery {

    private Integer status;

    private Long supplierId;

    private String poNo;
}
