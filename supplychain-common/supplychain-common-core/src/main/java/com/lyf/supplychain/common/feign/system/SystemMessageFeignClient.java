package com.lyf.supplychain.common.feign.system;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 系统服务消息中心 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@FeignClient(name = "supplychain-system", path = "/internal/system")
public interface SystemMessageFeignClient {

    /**
     * 发送系统站内信。
     *
     * @param request 站内信发送请求
     * @return 消息ID
     */
    @PostMapping("/messages")
    R<Long> send(@RequestBody SystemMessageSendRequest request);
}
