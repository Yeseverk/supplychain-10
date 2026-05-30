package com.lyf.supplychain.supplier.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 供应商分页查询参数。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplierPageQuery extends PageQuery {
    // 供应商的名称 模糊查询
    private String supplierName;
    // 供应商类型 精准查询
    private Integer supplierType;
    // 供应商状态 精准查询
    private Integer status;
    // 供应商等级 精准查询
    private String grade;
    // 供应商 引入的 开始时间
    private LocalDateTime createStartTime;
    // 供应商 引入的 结束时间
    private LocalDateTime createEndTime;
}
