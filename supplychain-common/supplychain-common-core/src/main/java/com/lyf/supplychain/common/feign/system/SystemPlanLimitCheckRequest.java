package com.lyf.supplychain.common.feign.system;

import lombok.Data;

/**
 * 租户套餐限制检查请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SystemPlanLimitCheckRequest {

    private Long tenantId;
    private String featureCode;
    private String bizName;
    private Integer currentUsage;
}
