package com.lyf.supplychain.system.service.impl;

import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.service.SystemEventDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 系统通知事件投递器。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Component
@ConditionalOnProperty(prefix = "supplychain.event.outbox", name = "dispatcher-type", havingValue = "local", matchIfMissing = true)
public class SystemNotificationEventDispatcher implements SystemEventDispatcher {

    private final SystemNotificationEventHandler notificationEventHandler;

    public SystemNotificationEventDispatcher(SystemNotificationEventHandler notificationEventHandler) {
        this.notificationEventHandler = notificationEventHandler;
    }

    /**
     * 投递系统通知事件，成功后由调用方更新 outbox 状态。
     *
     * @param event outbox 事件
     * @return true=通知事件已分发，false=非通知事件暂不处理
     */
    @Override
    public boolean dispatch(SysEventOutbox event) {
        return notificationEventHandler.handle(event);
    }
}
