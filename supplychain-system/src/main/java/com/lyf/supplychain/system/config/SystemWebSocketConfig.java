package com.lyf.supplychain.system.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 系统通知 WebSocket 配置。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(SystemNotificationProperties.class)
public class SystemWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册统一通知 WebSocket 端点。
     *
     * @param registry 端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 配置统一通知消息代理。
     *
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
