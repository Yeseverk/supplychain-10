package com.lyf.supplychain.common.feign.system;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统服务可靠事件 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@FeignClient(name = "supplychain-system", path = "/internal/system")
public interface SystemEventFeignClient {

    /**
     * 发布可靠事件。
     *
     * @param request 事件发布请求
     * @return 事件记录ID
     */
    @PostMapping("/events")
    R<Long> publish(@RequestBody SystemEventPublishRequest request);
}
