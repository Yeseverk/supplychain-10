package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 盘点审核请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class StocktakeAuditRequest {

    @NotNull(message = "审核人不能为空")
    private Long auditorId;
    private String auditRemark;
}
