package com.lyf.supplychain.common.security.config;

import com.lyf.supplychain.common.security.interceptor.SecurityContextInterceptor;
import com.lyf.supplychain.common.security.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 公共安全自动配置。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class CommonSecurityAutoConfiguration implements WebMvcConfigurer {

    private final SecurityContextInterceptor securityContextInterceptor;

    public CommonSecurityAutoConfiguration(SecurityContextInterceptor securityContextInterceptor) {
        this.securityContextInterceptor = securityContextInterceptor;
    }

    /**
     * 注册请求级安全上下文拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**");
    }
}
