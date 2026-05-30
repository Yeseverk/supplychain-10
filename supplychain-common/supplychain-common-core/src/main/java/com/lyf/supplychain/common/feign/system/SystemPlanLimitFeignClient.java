package com.lyf.supplychain.common.feign.system;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统套餐限制 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@FeignClient(name = "supplychain-system", path = "/internal/system")
public interface SystemPlanLimitFeignClient {

    /**
     * 检查租户套餐是否允许执行当前功能。
     *
     * @param request 套餐限制检查请求
     * @return 套餐限制检查结果
     */
    @PostMapping("/plan-limits/check")
    R<SystemPlanLimitCheckResponse> check(@RequestBody SystemPlanLimitCheckRequest request);
}
