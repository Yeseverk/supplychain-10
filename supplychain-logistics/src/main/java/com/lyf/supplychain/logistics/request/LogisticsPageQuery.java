package com.lyf.supplychain.logistics.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TMS 业务列表分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogisticsPageQuery extends PageQuery {

    /**
     * 运单、订单、渠道、物流商或目的地关键词。
     */
    private String keyword;

    /**
     * 业务状态。
     */
    private Integer status;
}
