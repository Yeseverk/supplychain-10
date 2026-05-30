package com.lyf.supplychain.system.service;

import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;

/**
 * 系统统一通知服务接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface SystemNotificationService {

    /**
     * 发送系统通知并触发可用通道分发。
     *
     * @param request 通知发送请求
     * @return 消息ID
     */
    Long send(SystemMessageSendRequest request);
}
