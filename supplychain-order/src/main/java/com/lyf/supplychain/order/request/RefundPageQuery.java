package com.lyf.supplychain.order.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OMS 退款分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RefundPageQuery extends PageQuery {

    /**
     * 退款单、订单号、平台退款单或退款原因关键词。
     */
    private String keyword;

    /**
     * 退款状态。
     */
    private Integer status;
}
