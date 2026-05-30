package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.lyf.supplychain.common.feign.system.SystemMessageSendRequest;
import com.lyf.supplychain.system.config.SystemNotificationProperties;
import com.lyf.supplychain.system.config.SystemWebSocketConfig;
import com.lyf.supplychain.system.service.impl.SystemNotificationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * APIFox WebSocket 手动联调测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Slf4j
@SpringBootTest(
        classes = SystemNotificationWebSocketManualIT.ManualTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=${manual.notification.server-port:19201}",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                "supplychain.notification.websocket-enabled=true",
                "supplychain.notification.mail-enabled=false"
        }
)
class SystemNotificationWebSocketManualIT {

    @Autowired
    private SystemNotificationService notificationService;

    @MockBean
    private MessageCenterService messageCenterService;

    @Value("${manual.notification.wait-seconds:20}")
    private long waitSeconds;

    @Value("${manual.notification.send-count:5}")
    private int sendCount;

    @Value("${manual.notification.interval-seconds:3}")
    private long intervalSeconds;

    @Value("${server.port}")
    private int serverPort;

    @Test
    void sendTenant101StationMessageForApifoxSubscribe() throws Exception {
        AtomicLong messageId = new AtomicLong(900000L);
        when(messageCenterService.sendInternal(any(SystemMessageSendRequest.class)))
                .thenAnswer(invocation -> messageId.incrementAndGet());

        log.info("WebSocket 测试服务已启动，请在 APIFox 连接 ws://localhost:{}/ws/notifications/websocket", serverPort);
        log.info("请订阅 destination=/topic/tenant/101/notifications，{} 秒后开始发送测试站内信", waitSeconds);
        waitForApifoxSubscribe();

        int actualSendCount = Math.max(sendCount, 1);
        for (int index = 1; index <= actualSendCount; index++) {
            notificationService.send(buildRequest(index));
            log.info("第 {} 条测试站内信已发送，请查看 APIFox 是否收到 /topic/tenant/101/notifications 推送", index);
            if (index < actualSendCount && intervalSeconds > 0) {
                TimeUnit.SECONDS.sleep(intervalSeconds);
            }
        }

        TimeUnit.SECONDS.sleep(3);
        verify(messageCenterService, org.mockito.Mockito.atLeast(actualSendCount)).sendInternal(any(SystemMessageSendRequest.class));
    }

    private SystemMessageSendRequest buildRequest(int index) {
        SystemMessageSendRequest request = new SystemMessageSendRequest();
        request.setTenantId(101L);
        request.setReceiverType("ROLE");
        request.setReceiverKey("ROLE_PURCHASE_MANAGER");
        request.setTitle("APIFox WebSocket 测试通知-" + index);
        request.setContent("这是第 " + index + " 条通过 system 统一通知中心发送的站内信。");
        request.setBizType("WEBSOCKET_MANUAL_TEST");
        request.setBizId("apifox-" + index + "-" + System.currentTimeMillis());
        request.setPriority("HIGH");
        return request;
    }

    private void waitForApifoxSubscribe() throws InterruptedException {
        if (waitSeconds <= 0) {
            return;
        }
        TimeUnit.SECONDS.sleep(waitSeconds);
    }

    /**
     * 手动联调测试应用，只加载 WebSocket 与通知服务，不连接数据库、Redis、Nacos 和邮件服务。
     *
     * @author liyunfei
     * @date 2026-05-20
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            RedisAutoConfiguration.class,
            MailSenderAutoConfiguration.class,
            MybatisPlusAutoConfiguration.class
    })
    @Import({SystemWebSocketConfig.class, SystemNotificationProperties.class, SystemNotificationServiceImpl.class})
    static class ManualTestApplication {
    }
}
