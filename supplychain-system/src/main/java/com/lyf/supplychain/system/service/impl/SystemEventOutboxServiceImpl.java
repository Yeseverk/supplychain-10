package com.lyf.supplychain.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import com.lyf.supplychain.system.entity.SysEventOutbox;
import com.lyf.supplychain.system.mapper.SysEventOutboxMapper;
import com.lyf.supplychain.system.service.SystemEventDispatcher;
import com.lyf.supplychain.system.service.SystemEventOutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统可靠事件服务实现。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Slf4j
@Service
public class SystemEventOutboxServiceImpl implements SystemEventOutboxService {

    private static final int MAX_ERROR_MSG_LENGTH = 512;

    private final SysEventOutboxMapper eventOutboxMapper;

    private final SystemEventDispatcher eventDispatcher;

    public SystemEventOutboxServiceImpl(SysEventOutboxMapper eventOutboxMapper, SystemEventDispatcher eventDispatcher) {
        this.eventOutboxMapper = eventOutboxMapper;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * 发布可靠事件，只写入事件表，后续由 outbox 投递任务异步分发。
     *
     * @param request 事件发布请求
     * @return 事件记录ID
     */
    @Override
    public Long publish(SystemEventPublishRequest request) {
        validate(request);
        SysEventOutbox event = buildEvent(request);
        try {
            eventOutboxMapper.insert(event);
        } catch (DuplicateKeyException exception) {
            SysEventOutbox existing = findExistingEvent(request);
            if (ObjectUtil.isNotNull(existing)) {
                log.info("可靠事件重复发布，tenantId={}，eventId={}，idempotentKey={}",
                        request.getTenantId(), request.getEventId(), request.getIdempotentKey());
                return existing.getId();
            }
            throw exception;
        }
        return event.getId();
    }

    /**
     * 扫描待投递事件并进行分发，用于 outbox 本地消息表异步投递。
     *
     * @param batchSize 单批处理数量
     * @return 本次投递数量
     */
    @Override
    public int dispatchPending(Integer batchSize) {
        int limit = ObjectUtil.defaultIfNull(batchSize, 100);
        List<SysEventOutbox> pendingEvents = eventOutboxMapper.selectList(new LambdaQueryWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getStatus, EventConstants.Status.PENDING)
                .eq(SysEventOutbox::getIsDeleted, 0)
                .orderByAsc(SysEventOutbox::getCreateTime)
                .last("LIMIT " + limit));
        int dispatched = 0;
        for (SysEventOutbox event : pendingEvents) {
            dispatch(event);
            dispatched++;
        }
        return dispatched;
    }

    /**
     * 扫描失败事件并重新分发，用于 XXL-JOB 补偿可靠通知链路。
     *
     * @param maxRetryCount 最大重试次数
     * @param batchSize     单批处理数量
     * @return 本次重试数量
     */
    @Override
    public int retryFailed(Integer maxRetryCount, Integer batchSize) {
        int retryLimit = ObjectUtil.defaultIfNull(maxRetryCount, 5);
        int limit = ObjectUtil.defaultIfNull(batchSize, 100);
        List<SysEventOutbox> failedEvents = eventOutboxMapper.selectList(new LambdaQueryWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getStatus, EventConstants.Status.FAILED)
                .lt(SysEventOutbox::getRetryCount, retryLimit)
                .eq(SysEventOutbox::getIsDeleted, 0)
                .orderByAsc(SysEventOutbox::getUpdateTime)
                .last("LIMIT " + limit));
        int retried = 0;
        for (SysEventOutbox event : failedEvents) {
            if (resetForRetry(event.getId())) {
                event.setStatus(EventConstants.Status.PENDING);
                dispatch(event);
                retried++;
            }
        }
        return retried;
    }

    private void validate(SystemEventPublishRequest request) {
        if (ObjectUtil.isNull(request)) {
            BusinessException.throwException("事件发布请求不能为空");
        }
        if (StrUtil.isBlank(request.getEventId())) {
            BusinessException.throwException("事件ID不能为空");
        }
        if (StrUtil.isBlank(request.getEventType())) {
            BusinessException.throwException("事件类型不能为空");
        }
        if (StrUtil.isBlank(request.getIdempotentKey())) {
            BusinessException.throwException("事件幂等键不能为空");
        }
        if (StrUtil.isBlank(request.getPayload())) {
            BusinessException.throwException("事件载荷不能为空");
        }
    }

    private SysEventOutbox buildEvent(SystemEventPublishRequest request) {
        LocalDateTime now = LocalDateTime.now();
        SysEventOutbox event = new SysEventOutbox();
        event.setTenantId(ObjectUtil.defaultIfNull(request.getTenantId(), 0L));
        event.setEventId(request.getEventId());
        event.setEventType(request.getEventType());
        event.setSourceService(request.getSourceService());
        event.setBizType(request.getBizType());
        event.setBizId(request.getBizId());
        event.setIdempotentKey(request.getIdempotentKey());
        event.setPayload(request.getPayload());
        event.setStatus(EventConstants.Status.PENDING);
        event.setRetryCount(0);
        event.setOccurredTime(ObjectUtil.defaultIfNull(request.getOccurredTime(), now));
        event.setCreateTime(now);
        event.setUpdateTime(now);
        event.setIsDeleted(0);
        return event;
    }

    private SysEventOutbox findExistingEvent(SystemEventPublishRequest request) {
        return eventOutboxMapper.selectOne(new LambdaQueryWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getTenantId, ObjectUtil.defaultIfNull(request.getTenantId(), 0L))
                .and(wrapper -> wrapper.eq(SysEventOutbox::getEventId, request.getEventId())
                        .or()
                        .eq(SysEventOutbox::getIdempotentKey, request.getIdempotentKey()))
                .eq(SysEventOutbox::getIsDeleted, 0)
                .last("LIMIT 1"));
    }

    private void dispatch(SysEventOutbox event) {
        try {
            if (eventDispatcher.dispatch(event)) {
                markDispatched(event.getId());
                return;
            }
            markIgnored(event.getId(), "暂不支持的事件类型：" + event.getEventType());
        } catch (Exception exception) {
            markFailed(event.getId(), exception);
            log.error("可靠事件分发失败，eventId={}，eventType={}，bizType={}，bizId={}",
                    event.getEventId(), event.getEventType(), event.getBizType(), event.getBizId(), exception);
        }
    }

    private void markDispatched(Long eventId) {
        SysEventOutbox update = new SysEventOutbox();
        update.setStatus(EventConstants.Status.DISPATCHED);
        update.setDispatchTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        eventOutboxMapper.update(update, new LambdaUpdateWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getId, eventId)
                .eq(SysEventOutbox::getStatus, EventConstants.Status.PENDING));
    }

    private void markIgnored(Long eventId, String reason) {
        SysEventOutbox update = new SysEventOutbox();
        update.setStatus(EventConstants.Status.IGNORED);
        update.setErrorMsg(limitError(reason));
        update.setUpdateTime(LocalDateTime.now());
        eventOutboxMapper.update(update, new LambdaUpdateWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getId, eventId)
                .eq(SysEventOutbox::getStatus, EventConstants.Status.PENDING));
    }

    private void markFailed(Long eventId, Exception exception) {
        SysEventOutbox update = new SysEventOutbox();
        update.setStatus(EventConstants.Status.FAILED);
        update.setErrorMsg(limitError(exception.getMessage()));
        update.setUpdateTime(LocalDateTime.now());
        eventOutboxMapper.update(update, new LambdaUpdateWrapper<SysEventOutbox>()
                .setSql("retry_count = retry_count + 1")
                .eq(SysEventOutbox::getId, eventId)
                .eq(SysEventOutbox::getStatus, EventConstants.Status.PENDING));
    }

    private boolean resetForRetry(Long eventId) {
        SysEventOutbox update = new SysEventOutbox();
        update.setStatus(EventConstants.Status.PENDING);
        update.setErrorMsg(null);
        update.setUpdateTime(LocalDateTime.now());
        return eventOutboxMapper.update(update, new LambdaUpdateWrapper<SysEventOutbox>()
                .eq(SysEventOutbox::getId, eventId)
                .eq(SysEventOutbox::getStatus, EventConstants.Status.FAILED)) > 0;
    }

    private String limitError(String message) {
        String error = StrUtil.blankToDefault(message, "事件分发失败");
        if (error.length() <= MAX_ERROR_MSG_LENGTH) {
            return error;
        }
        return error.substring(0, MAX_ERROR_MSG_LENGTH);
    }
}
