package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import com.lyf.supplychain.system.entity.SysMessage;
import com.lyf.supplychain.system.mapper.SysMessageMapper;
import com.lyf.supplychain.system.service.impl.MessageCenterServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消息中心服务单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@ExtendWith(MockitoExtension.class)
class MessageCenterServiceTest {

    @Mock
    private SysMessageMapper messageMapper;

    private MessageCenterService messageCenterService;

    @BeforeEach
    void setUp() {
        messageCenterService = new MessageCenterServiceImpl(messageMapper);
        SecurityContextHolder.setLoginUser(LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .roles(List.of("ROLE_PURCHASE"))
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void unreadCountShouldUseCurrentTenantAndUser() {
        when(messageMapper.selectCount(any(Wrapper.class))).thenReturn(3L);

        Long count = messageCenterService.unreadCount();

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void sendInternalShouldPersistUnreadMessageAndReturnId() {
        when(messageMapper.insert(any(SysMessage.class))).thenAnswer(invocation -> {
            SysMessage message = invocation.getArgument(0);
            message.setId(8001L);
            return 1;
        });
        SystemMessageSendRequest request = new SystemMessageSendRequest();
        request.setTenantId(101L);
        request.setReceiverId(501L);
        request.setReceiverType("USER");
        request.setReceiverKey("501");
        request.setTitle("供应商审核通过");
        request.setContent("供应商已经审核通过");
        request.setBizType("SUPPLIER_AUDIT");
        request.setBizId("1001");
        request.setPriority("HIGH");

        Long messageId = messageCenterService.sendInternal(request);

        assertThat(messageId).isEqualTo(8001L);
        verify(messageMapper).insert(org.mockito.ArgumentMatchers.argThat(message ->
                message.getTenantId().equals(101L)
                        && message.getReceiverId().equals(501L)
                        && message.getReadStatus() == 0
                        && message.getIsDeleted() == 0
        ));
    }
}
