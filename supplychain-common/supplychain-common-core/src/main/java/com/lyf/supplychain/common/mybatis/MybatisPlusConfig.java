package com.lyf.supplychain.common.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import cn.hutool.core.util.ObjectUtil;
import com.lyf.supplychain.common.context.TenantContext;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis-Plus 通用插件配置。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Configuration
public class MybatisPlusConfig {
    // 在进行租户ID拼接的时候 如下的表 会自动进行忽略
    private static final Set<String> IGNORE_TENANT_TABLES = Set.of(
            "sys_tenant",
            "sys_role",
            "sys_role_menu",
            "sys_user_role",
            "sys_menu",
            "sys_dict_type",
            "sys_dict_item"
    );

    /**
     * 配置分页、多租户和乐观锁插件。
     *
     * @return MyBatis-Plus 插件链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // MP 内部的插件 指定方言
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 自定义的插件  实现了 租户ID 的拼接
        interceptor.addInnerInterceptor(tenantLineInnerInterceptor());
        // 乐观锁的实现
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    private TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new SupplychainTenantLineHandler());
        return tenantInterceptor;
    }

    /**
     * 基于 ThreadLocal 上下文提供租户过滤条件。
     *
     * @author liyunfei
     * @date 2026-05-15
     */
    private static class SupplychainTenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {

        @Override
        public LongValue getTenantId() {
            // 从当前登录上下文读取租户，未登录的内部任务默认使用 0 避免串租户
            Long tenantId = TenantContext.getTenantId();
            return new LongValue(ObjectUtil.defaultIfNull(tenantId, 0L));
        }

        @Override
        public boolean ignoreTable(String tableName) {
            return IGNORE_TENANT_TABLES.contains(tableName);
        }
    }
}
