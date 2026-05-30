package com.lyf.supplychain.order.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OMS 订单分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageQuery extends PageQuery {

    /**
     * 内部订单、平台订单、平台或运单关键词。
     */
    private String keyword;

    /**
     * 订单状态。
     */
    private Integer status;
}
