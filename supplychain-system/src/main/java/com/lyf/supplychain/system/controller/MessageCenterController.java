package com.lyf.supplychain.system.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.system.entity.SysMessage;
import com.lyf.supplychain.system.request.MessagePageQuery;
import com.lyf.supplychain.system.service.MessageCenterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息中心控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping("/api/messages")
public class MessageCenterController {

    private final MessageCenterService messageCenterService;

    public MessageCenterController(MessageCenterService messageCenterService) {
        this.messageCenterService = messageCenterService;
    }

    /**
     * 查询当前用户未读消息数。
     *
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        return R.ok(messageCenterService.unreadCount());
    }

    /**
     * 分页查询当前用户消息。
     *
     * @param pageQuery 分页参数
     * @return 消息分页
     */
    @GetMapping("/page")
    public R<PageResult<SysMessage>> page(MessagePageQuery pageQuery) {
        return R.ok(messageCenterService.pageMine(pageQuery));
    }

    /**
     * 标记单条消息为已读。
     *
     * @param id 消息ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable("id") Long id) {
        messageCenterService.markRead(id);
        return R.ok();
    }

    /**
     * 标记当前用户全部消息为已读。
     *
     * @return 无数据响应
     */
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        messageCenterService.markAllRead();
        return R.ok();
    }
}
