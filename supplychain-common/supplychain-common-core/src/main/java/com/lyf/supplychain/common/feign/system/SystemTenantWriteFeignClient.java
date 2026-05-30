package com.lyf.supplychain.common.feign.system;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统租户写操作检查 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@FeignClient(name = "supplychain-system", path = "/internal/system")
public interface SystemTenantWriteFeignClient {

    /**
     * 检查租户当前是否允许写操作。
     *
     * @param request 写操作检查请求
     * @return 写操作检查结果
     */
    @PostMapping("/tenants/write-check")
    R<SystemTenantWriteCheckResponse> check(@RequestBody SystemTenantWriteCheckRequest request);
}
