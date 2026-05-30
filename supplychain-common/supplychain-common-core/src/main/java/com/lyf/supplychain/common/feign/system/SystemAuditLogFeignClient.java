package com.lyf.supplychain.common.feign.system;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统操作审计日志 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@FeignClient(name = "supplychain-system", path = "/internal/system")
public interface SystemAuditLogFeignClient {

    /**
     * 记录操作审计日志。
     *
     * @param request 审计日志记录请求
     * @return 审计日志ID
     */
    @PostMapping("/audit-logs")
    R<Long> record(@RequestBody SystemAuditLogRecordRequest request);
}
