package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.entity.SysMessage;
import com.lyf.supplychain.system.mapper.SysMessageMapper;
import com.lyf.supplychain.system.request.MessagePageQuery;
import com.lyf.supplychain.system.service.MessageCenterService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息中心服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class MessageCenterServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements MessageCenterService {

    public MessageCenterServiceImpl(SysMessageMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    /**
     * 通过内部接口发送站内信，集中由 system 模块负责消息落库。
     *
     * @param request 站内信发送请求
     * @return 消息ID
     */
    @Override
    public Long sendInternal(SystemMessageSendRequest request) {
        if (ObjectUtil.isNull(request)) {
            BusinessException.throwException("站内信发送请求不能为空");
        }
        if (StrUtil.isBlank(request.getTitle())) {
            BusinessException.throwException("站内信标题不能为空");
        }
        SysMessage message = buildInternalMessage(request);
        save(message);
        return message.getId();
    }

    /**
     * 查询当前用户未读消息数，包含直接发给用户和发给当前角色的消息。
     *
     * @return 未读数量
     */
    @Override
    public Long unreadCount() {
        LoginUser user = requireLoginUser();
        return count(messageQuery(user, true));
    }

    /**
     * 分页查询当前用户消息。
     *
     * @param pageQuery 分页参数
     * @return 消息分页
     */
    @Override
    public PageResult<SysMessage> pageMine(MessagePageQuery pageQuery) {
        pageQuery.normalize();
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage> wrapper =
                messageQuery(requireLoginUser(), false);
        if (StrUtil.isNotBlank(pageQuery.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysMessage::getTitle, pageQuery.getKeyword())
                    .or().like(SysMessage::getContent, pageQuery.getKeyword())
                    .or().like(SysMessage::getBizType, pageQuery.getKeyword())
                    .or().like(SysMessage::getReceiverKey, pageQuery.getKeyword()));
        }
        if (pageQuery.getReadStatus() != null) {
            wrapper.eq(SysMessage::getReadStatus, pageQuery.getReadStatus());
        }
        if (StrUtil.isNotBlank(pageQuery.getType())) {
            wrapper.eq(SysMessage::getBizType, pageQuery.getType());
        }
        Page<SysMessage> page = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()),
                wrapper.orderByAsc(SysMessage::getReadStatus)
                        .orderByDesc(SysMessage::getCreateTime));
        return PageResult.from(page);
    }

    /**
     * 将当前用户可见的单条消息标记为已读。
     *
     * @param id 消息ID
     */
    @Override
    public void markRead(Long id) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            BusinessException.throwException("消息ID必须大于0");
        }
        LoginUser user = requireLoginUser();
        boolean success = update(new LambdaUpdateWrapper<SysMessage>()
                .set(SysMessage::getReadStatus, 1)
                .set(SysMessage::getReadTime, LocalDateTime.now())
                .eq(SysMessage::getId, id)
                .eq(SysMessage::getTenantId, user.getTenantId())
                .eq(SysMessage::getReadStatus, 0)
                .eq(SysMessage::getIsDeleted, 0)
                .and(scope -> visibleMessageUpdateScope(scope, user)));
        if (!success) {
            BusinessException.throwException("消息不存在或已读");
        }
    }

    /**
     * 将当前用户直接接收的未读消息全部标记为已读。
     */
    @Override
    public void markAllRead() {
        LoginUser user = requireLoginUser();
        update(new LambdaUpdateWrapper<SysMessage>()
                .set(SysMessage::getReadStatus, 1)
                .set(SysMessage::getReadTime, LocalDateTime.now())
                .eq(SysMessage::getTenantId, user.getTenantId())
                .eq(SysMessage::getReadStatus, 0)
                .eq(SysMessage::getIsDeleted, 0)
                .and(scope -> visibleMessageUpdateScope(scope, user)));
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage> messageQuery(LoginUser user, boolean unreadOnly) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getTenantId, user.getTenantId())
                        .eq(SysMessage::getIsDeleted, 0)
                        .and(scope -> visibleMessageQueryScope(scope, user));
        if (unreadOnly) {
            wrapper.eq(SysMessage::getReadStatus, 0);
        }
        return wrapper;
    }

    private void visibleMessageQueryScope(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMessage> scope, LoginUser user) {
        List<String> roles = CollUtil.emptyIfNull(user.getRoles());
        scope.eq(SysMessage::getReceiverType, "SYSTEM")
                .or()
                .nested(nested -> nested.eq(SysMessage::getReceiverType, "USER")
                        .eq(SysMessage::getReceiverId, user.getUserId()));
        if (CollUtil.isNotEmpty(roles)) {
            scope.or()
                    .nested(nested -> nested.eq(SysMessage::getReceiverType, "ROLE")
                            .in(SysMessage::getReceiverKey, roles));
        }
    }

    private void visibleMessageUpdateScope(LambdaUpdateWrapper<SysMessage> scope, LoginUser user) {
        List<String> roles = CollUtil.emptyIfNull(user.getRoles());
        scope.eq(SysMessage::getReceiverType, "SYSTEM")
                .or()
                .nested(nested -> nested.eq(SysMessage::getReceiverType, "USER")
                        .eq(SysMessage::getReceiverId, user.getUserId()));
        if (CollUtil.isNotEmpty(roles)) {
            scope.or()
                    .nested(nested -> nested.eq(SysMessage::getReceiverType, "ROLE")
                            .in(SysMessage::getReceiverKey, roles));
        }
    }

    private SysMessage buildInternalMessage(SystemMessageSendRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SysMessage message = new SysMessage();
        message.setTenantId(ObjectUtil.defaultIfNull(request.getTenantId(), 0L));
        message.setReceiverId(ObjectUtil.defaultIfNull(request.getReceiverId(), 0L));
        message.setReceiverType(StrUtil.blankToDefault(request.getReceiverType(), "ROLE"));
        message.setReceiverKey(request.getReceiverKey());
        message.setTitle(StrUtil.blankToDefault(request.getTitle(), "系统通知"));
        message.setContent(StrUtil.blankToDefault(request.getContent(), ""));
        message.setBizType(StrUtil.blankToDefault(request.getBizType(), "SYSTEM"));
        message.setBizId(request.getBizId());
        message.setPriority(StrUtil.blankToDefault(request.getPriority(), "NORMAL"));
        message.setReadStatus(0);
        message.setCreateTime(now);
        message.setUpdateTime(now);
        message.setIsDeleted(0);
        return message;
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        if (ObjectUtil.isNull(loginUser)) {
            BusinessException.throwException("请先登录");
        }
        return loginUser;
    }
}
