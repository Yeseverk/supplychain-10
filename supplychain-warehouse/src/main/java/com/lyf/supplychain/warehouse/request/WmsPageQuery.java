package com.lyf.supplychain.warehouse.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WMS 业务列表分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsPageQuery extends PageQuery {

    /**
     * 单号、仓库、来源单或任务名称关键词。
     */
    private String keyword;

    /**
     * 业务状态。
     */
    private Integer status;
}
