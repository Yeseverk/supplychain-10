package com.lyf.supplychain.supplier.service;

import com.lyf.supplychain.supplier.model.NotificationCommand;

/**
 * 通知发送服务。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
public interface NotificationService {

    /**
     * 发送站内信并按需发送邮件。
     *
     * @param command 通知发送命令
     */
    void send(NotificationCommand command);
}
