package com.lyf.supplychain.supplier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.util.concurrent.Executor;

/**
 * 供应商异步任务与基础组件配置。
 *
 * @author liyunfei
 * @date 2026-05-18
 */
@Configuration
public class SupplierAsyncConfig {

    /**
     * 供应商详情聚合异步线程池。
     *
     * @return 异步执行器
     */
    @Bean("supplierTaskExecutor")
    public Executor supplierTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("supplier-detail-");
        executor.initialize();
        return executor;
    }

    /**
     * 供应商资质到期扫描异步线程池。
     *
     * @return 异步执行器
     */
    @Bean("supplierCertExpireExecutor")
    public ThreadPoolTaskExecutor supplierCertExpireExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("supplier-cert-expire-");
        executor.initialize();
        return executor;
    }

    /**
     * 系统时钟，便于编码生成规则在测试中替换。
     *
     * @return 系统默认时区时钟
     */
    @Bean
    public Clock supplierClock() {
        return Clock.systemDefaultZone();
    }
}
