package com.lyf.supplychain.finance.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资金流水分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceCashFlowPageQuery extends PageQuery {

    /**
     * 关键词：来源单号/来源类型/币种/备注.
     */
    private String keyword;

    /**
     * 流水类型：1=收入 2=支出.
     */
    private Integer flowType;
}
