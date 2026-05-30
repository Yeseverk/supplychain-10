package com.lyf.supplychain.system.service;

import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.system.entity.SysUserRole;
import com.lyf.supplychain.system.mapper.SysMenuMapper;
import com.lyf.supplychain.system.mapper.SysRoleMapper;
import com.lyf.supplychain.system.mapper.SysUserRoleMapper;
import com.lyf.supplychain.system.service.impl.RbacPermissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RBAC 权限缓存服务单元测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class RbacPermissionServiceImplTest {

    @Test
    void listPermissionsShouldReadFromCacheFirst() {
        MemoryStringRedisTemplate redisTemplate = new MemoryStringRedisTemplate();
        redisTemplate.store.put(CommonRedisKeys.permission(101L, 501L), "[\"srm:supplier:list\"]");
        QueryCounter counter = new QueryCounter();
        RbacPermissionService service = service(redisTemplate, counter);

        List<String> permissions = service.listPermissions(101L, 501L);

        assertThat(permissions).containsExactly("srm:supplier:list");
        assertThat(counter.permissionQueryCount).isZero();
    }

    @Test
    void listPermissionsShouldQueryDatabaseAndWriteCacheWhenMissed() {
        MemoryStringRedisTemplate redisTemplate = new MemoryStringRedisTemplate();
        QueryCounter counter = new QueryCounter();
        RbacPermissionService service = service(redisTemplate, counter);

        List<String> permissions = service.listPermissions(101L, 501L);

        assertThat(permissions).containsExactly("srm:supplier:list");
        assertThat(counter.roleQueryCount).isEqualTo(1);
        assertThat(counter.permissionQueryCount).isEqualTo(1);
        assertThat(redisTemplate.store.get(CommonRedisKeys.roles(101L, 501L))).isEqualTo("[\"ROLE_PURCHASE\"]");
        assertThat(redisTemplate.store.get(CommonRedisKeys.permission(101L, 501L))).isEqualTo("[\"srm:supplier:list\"]");
    }

    @Test
    void evictRolePermissionCacheShouldDeleteAffectedUserCaches() {
        MemoryStringRedisTemplate redisTemplate = new MemoryStringRedisTemplate();
        redisTemplate.store.put(CommonRedisKeys.permission(101L, 501L), "[\"srm:supplier:list\"]");
        redisTemplate.store.put(CommonRedisKeys.roles(101L, 501L), "[\"ROLE_PURCHASE\"]");
        redisTemplate.store.put(CommonRedisKeys.dataScope(101L, 501L), "3");
        QueryCounter counter = new QueryCounter();
        RbacPermissionService service = service(redisTemplate, counter);

        service.evictRolePermissionCache(101L, 301L);

        assertThat(redisTemplate.store).doesNotContainKeys(
                CommonRedisKeys.permission(101L, 501L),
                CommonRedisKeys.roles(101L, 501L),
                CommonRedisKeys.dataScope(101L, 501L));
        assertThat(redisTemplate.deletedKeys).hasSize(1);
    }

    private RbacPermissionService service(MemoryStringRedisTemplate redisTemplate, QueryCounter counter) {
        return new RbacPermissionServiceImpl(
                menuMapper(counter),
                roleMapper(counter),
                userRoleMapper(),
                new FixedObjectProvider(redisTemplate));
    }

    private SysMenuMapper menuMapper(QueryCounter counter) {
        return proxy(SysMenuMapper.class, (proxy, method, args) -> {
            if ("selectPermissionsByUserId".equals(method.getName())) {
                counter.permissionQueryCount++;
                return List.of("srm:supplier:list");
            }
            return defaultValue(method.getReturnType());
        });
    }

    private SysRoleMapper roleMapper(QueryCounter counter) {
        return proxy(SysRoleMapper.class, (proxy, method, args) -> {
            if ("selectRoleCodesByUserId".equals(method.getName())) {
                counter.roleQueryCount++;
                return List.of("ROLE_PURCHASE");
            }
            if ("selectDataScopeByUserId".equals(method.getName())) {
                counter.dataScopeQueryCount++;
                return 3;
            }
            return defaultValue(method.getReturnType());
        });
    }

    private SysUserRoleMapper userRoleMapper() {
        return proxy(SysUserRoleMapper.class, (proxy, method, args) -> {
            if ("selectList".equals(method.getName())) {
                SysUserRole userRole = new SysUserRole();
                userRole.setTenantId(101L);
                userRole.setUserId(501L);
                userRole.setRoleId(301L);
                return List.of(userRole);
            }
            return defaultValue(method.getReturnType());
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private Object defaultValue(Class<?> returnType) {
        if (Boolean.TYPE == returnType) {
            return false;
        }
        if (Integer.TYPE == returnType || Long.TYPE == returnType) {
            return 0;
        }
        return null;
    }

    private static class QueryCounter {

        private int roleQueryCount;

        private int permissionQueryCount;

        private int dataScopeQueryCount;
    }

    private static class MemoryStringRedisTemplate extends StringRedisTemplate {

        private final Map<String, String> store = new HashMap<>();

        private final List<Collection<String>> deletedKeys = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public ValueOperations<String, String> opsForValue() {
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(),
                    new Class<?>[]{ValueOperations.class},
                    (proxy, method, args) -> {
                        if ("get".equals(method.getName())) {
                            return store.get(args[0]);
                        }
                        if ("set".equals(method.getName())) {
                            store.put((String) args[0], (String) args[1]);
                            return null;
                        }
                        return null;
                    });
        }

        @Override
        public Long delete(Collection<String> keys) {
            deletedKeys.add(keys);
            long count = 0;
            for (String key : keys) {
                if (store.remove(key) != null) {
                    count++;
                }
            }
            return count;
        }
    }

    private record FixedObjectProvider(StringRedisTemplate redisTemplate) implements ObjectProvider<StringRedisTemplate> {

        @Override
        public StringRedisTemplate getObject(Object... args) {
            return redisTemplate;
        }

        @Override
        public StringRedisTemplate getIfAvailable() {
            return redisTemplate;
        }

        @Override
        public StringRedisTemplate getIfUnique() {
            return redisTemplate;
        }

        @Override
        public StringRedisTemplate getObject() {
            return redisTemplate;
        }
    }
}
