package com.lyf.supplychain.purchase.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购收货单分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseReceiptPageQuery extends PageQuery {

    /**
     * 关键词：收货单号/采购单号/收货人/备注.
     */
    private String keyword;

    /**
     * 状态：0=待质检 1=入库中 2=部分入库 3=全部入库 4=拒收.
     */
    private Integer status;
}
