package com.lyf.supplychain.system.model.saas;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户套餐变更请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class TenantPlanChangeRequest {

    @NotNull(message = "套餐类型不能为空")
    private Integer planType;

    private LocalDateTime planEndTime;
}
