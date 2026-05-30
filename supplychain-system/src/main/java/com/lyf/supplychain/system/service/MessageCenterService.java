package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.entity.SysMessage;
import com.lyf.supplychain.system.request.MessagePageQuery;

/**
 * 消息中心服务接口。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public interface MessageCenterService extends IService<SysMessage> {

    /**
     * 通过内部接口发送站内信。
     *
     * @param request 站内信发送请求
     * @return 消息ID
     */
    Long sendInternal(SystemMessageSendRequest request);

    /**
     * 查询当前用户未读消息数。
     *
     * @return 未读数量
     */
    Long unreadCount();

    /**
     * 分页查询当前用户消息。
     *
     * @param pageQuery 分页参数
     * @return 消息分页
     */
    PageResult<SysMessage> pageMine(MessagePageQuery pageQuery);

    /**
     * 标记当前用户单条消息为已读。
     *
     * @param id 消息ID
     */
    void markRead(Long id);

    /**
     * 标记当前用户所有消息为已读。
     */
    void markAllRead();
}
