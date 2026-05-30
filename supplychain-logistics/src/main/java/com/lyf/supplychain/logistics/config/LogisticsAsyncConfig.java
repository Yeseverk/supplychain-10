package com.lyf.supplychain.logistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 物流模块线程池配置。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Configuration
public class LogisticsAsyncConfig {

    /**
     * 物流轨迹拉取线程池。
     *
     * @return 线程池执行器
     */
    @Bean("logisticsTrackExecutor")
    public ThreadPoolTaskExecutor logisticsTrackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("tms-track-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
