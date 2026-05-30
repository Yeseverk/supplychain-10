package com.lyf.supplychain.system.service;

import com.lyf.supplychain.system.entity.SysEventOutbox;

/**
 * 系统事件投递器接口。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
public interface SystemEventDispatcher {

    /**
     * 投递单条 outbox 事件。
     *
     * @param event outbox 事件
     * @return true=已处理成功，false=事件类型被忽略
     */
    boolean dispatch(SysEventOutbox event);
}
