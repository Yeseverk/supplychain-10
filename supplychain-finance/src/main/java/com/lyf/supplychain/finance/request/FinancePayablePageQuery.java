package com.lyf.supplychain.finance.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应付账款分页查询参数。
 *
 * @author liyunfei
 * @date 2026-05-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinancePayablePageQuery extends PageQuery {

    /**
     * 关键词：应付单号/来源单号/供应商名称/发票号/备注.
     */
    private String keyword;

    /**
     * 状态：0=待对账 1=待付款 2=部分付款 3=已结清 4=逾期.
     */
    private Integer status;
}
