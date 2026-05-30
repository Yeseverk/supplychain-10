package com.lyf.supplychain.common.notification;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.event.EventConstants;
import com.lyf.supplychain.common.feign.system.SystemEventFeignClient;
import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 可靠通知事件发布器。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
@Slf4j
@Component
public class ReliableNotificationEventPublisher {

    private static final String DEFAULT_RECEIVER_TYPE = "ROLE";

    private static final String DEFAULT_BIZ_TYPE = "SYSTEM";

    private static final String DEFAULT_PRIORITY = "NORMAL";

    private static final String DEFAULT_SOURCE_SERVICE = "supplychain-unknown";

    private final SystemEventFeignClient systemEventFeignClient;

    private final ObjectMapper objectMapper;

    public ReliableNotificationEventPublisher(SystemEventFeignClient systemEventFeignClient, ObjectMapper objectMapper) {
        this.systemEventFeignClient = systemEventFeignClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 发布可靠通知事件，由 system 事件中心负责落库、幂等和通知分发。
     *
     * @param command 通知发布命令
     */
    public void publish(ReliableNotificationCommand command) {
        if (command == null) {
            return;
        }
        SystemMessageSendRequest messageRequest = buildMessageRequest(command);
        SystemEventPublishRequest eventRequest = buildEventRequest(command, messageRequest);
        if (eventRequest == null) {
            return;
        }
        try {
            R<Long> response = systemEventFeignClient.publish(eventRequest);
            if (response == null || response.getData() == null) {
                log.warn("system 可靠事件中心未返回事件ID，bizType={}，bizId={}", command.getBizType(), command.getBizId());
            }
        } catch (Exception exception) {
            log.error("可靠通知事件发布失败，sourceService={}，bizType={}，bizId={}",
                    command.getSourceService(), command.getBizType(), command.getBizId(), exception);
        }
    }

    private SystemMessageSendRequest buildMessageRequest(ReliableNotificationCommand command) {
        SystemMessageSendRequest request = new SystemMessageSendRequest();
        request.setTenantId(ObjectUtil.defaultIfNull(command.getTenantId(), 0L));
        request.setReceiverId(ObjectUtil.defaultIfNull(command.getReceiverId(), 0L));
        request.setReceiverType(StrUtil.blankToDefault(command.getReceiverType(), DEFAULT_RECEIVER_TYPE));
        request.setReceiverKey(command.getReceiverKey());
        request.setTitle(StrUtil.blankToDefault(command.getTitle(), "系统通知"));
        request.setContent(StrUtil.blankToDefault(command.getContent(), ""));
        request.setBizType(StrUtil.blankToDefault(command.getBizType(), DEFAULT_BIZ_TYPE));
        request.setBizId(command.getBizId());
        request.setPriority(StrUtil.blankToDefault(command.getPriority(), DEFAULT_PRIORITY));
        request.setMailTo(command.getMailTo());
        request.setMailSubject(command.getMailSubject());
        request.setMailContent(command.getMailContent());
        return request;
    }

    private SystemEventPublishRequest buildEventRequest(ReliableNotificationCommand command,
                                                        SystemMessageSendRequest messageRequest) {
        try {
            SystemEventPublishRequest eventRequest = new SystemEventPublishRequest();
            eventRequest.setTenantId(messageRequest.getTenantId());
            eventRequest.setEventId(UUID.fastUUID().toString(true));
            eventRequest.setEventType(EventConstants.EventType.SYSTEM_NOTIFICATION);
            eventRequest.setSourceService(StrUtil.blankToDefault(command.getSourceService(), DEFAULT_SOURCE_SERVICE));
            eventRequest.setBizType(messageRequest.getBizType());
            eventRequest.setBizId(messageRequest.getBizId());
            eventRequest.setIdempotentKey(buildIdempotentKey(command, messageRequest));
            eventRequest.setPayload(objectMapper.writeValueAsString(messageRequest));
            eventRequest.setOccurredTime(LocalDateTime.now());
            return eventRequest;
        } catch (JsonProcessingException exception) {
            log.error("可靠通知事件序列化失败，sourceService={}，bizType={}，bizId={}",
                    command.getSourceService(), command.getBizType(), command.getBizId(), exception);
            return null;
        }
    }

    private String buildIdempotentKey(ReliableNotificationCommand command, SystemMessageSendRequest request) {
        String rawKey = StrUtil.join(":",
                "NOTIFICATION",
                request.getTenantId(),
                request.getReceiverType(),
                ObjectUtil.defaultIfNull(request.getReceiverId(), 0L),
                StrUtil.blankToDefault(request.getReceiverKey(), "-"),
                StrUtil.blankToDefault(request.getBizType(), DEFAULT_BIZ_TYPE),
                StrUtil.blankToDefault(request.getBizId(), "-"),
                StrUtil.blankToDefault(request.getTitle(), "系统通知"),
                StrUtil.blankToDefault(command.getContent(), ""));
        return DigestUtil.md5Hex(rawKey);
    }
}
