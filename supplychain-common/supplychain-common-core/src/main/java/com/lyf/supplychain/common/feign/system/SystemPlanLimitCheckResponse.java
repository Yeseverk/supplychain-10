package com.lyf.supplychain.common.feign.system;

import lombok.Data;

/**
 * 租户套餐限制检查结果。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class SystemPlanLimitCheckResponse {

    private Integer planType;
    private String featureCode;
    private String featureName;
    private Integer limitValue;
    private String limitUnit;
    private Integer currentUsage;
}
