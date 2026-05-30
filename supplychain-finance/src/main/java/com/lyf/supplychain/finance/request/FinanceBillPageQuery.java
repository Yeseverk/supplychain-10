package com.lyf.supplychain.finance.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台账单分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceBillPageQuery extends PageQuery {

    /**
     * 关键词：账单号/平台/店铺/平台账单号.
     */
    private String keyword;

    /**
     * 状态：0=待解析 1=解析中 2=待对账 3=对账完成 4=解析失败.
     */
    private Integer status;
}
