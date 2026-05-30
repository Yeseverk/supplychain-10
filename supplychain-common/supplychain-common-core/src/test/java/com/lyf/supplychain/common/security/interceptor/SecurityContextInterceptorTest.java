package com.lyf.supplychain.common.security.interceptor;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.security.constant.SecurityConstants;
import com.lyf.supplychain.common.security.model.LoginUser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextInterceptorTest {

    private final SecurityContextInterceptor interceptor = new SecurityContextInterceptor(new ObjectMapper());

    @Test
    void daoFallbackShouldRestoreLoginUserFromSaTokenSession() {
        MemorySaTokenDao dao = new MemorySaTokenDao();
        String tokenValue = "token-501";
        String loginId = "501";
        String tokenKey = StpUtil.getStpLogic().splicingKeyTokenValue(tokenValue);
        String sessionKey = StpUtil.getStpLogic().splicingKeySession(loginId);
        LoginUser loginUser = LoginUser.builder()
                .userId(501L)
                .tenantId(101L)
                .username("admin@flexchain.local")
                .permissions(List.of("srm:supplier:list", "pms:order:list"))
                .roles(List.of("ROLE_TENANT_ADMIN"))
                .build();
        dao.set(tokenKey, loginId, 7200L);
        dao.setObject(sessionKey, new SaSession(sessionKey).set(SecurityConstants.LOGIN_USER_SESSION_KEY, loginUser), 7200L);

        LoginUser restored = interceptor.restoreFromTokenDaoFallback(tokenValue, dao);

        assertThat(restored).isNotNull();
        assertThat(restored.getUserId()).isEqualTo(501L);
        assertThat(restored.getTenantId()).isEqualTo(101L);
        assertThat(restored.getPermissions()).contains("srm:supplier:list", "pms:order:list");
    }

    @Test
    void daoFallbackShouldConvertMapLoginUserFromSaTokenSession() {
        MemorySaTokenDao dao = new MemorySaTokenDao();
        String tokenValue = "token-502";
        String loginId = "502";
        String tokenKey = StpUtil.getStpLogic().splicingKeyTokenValue(tokenValue);
        String sessionKey = StpUtil.getStpLogic().splicingKeySession(loginId);
        Map<String, Object> loginUserMap = new LinkedHashMap<>();
        loginUserMap.put("userId", 502L);
        loginUserMap.put("tenantId", 101L);
        loginUserMap.put("permissions", List.of("wms:inventory:list"));
        loginUserMap.put("roles", List.of("ROLE_WAREHOUSE"));
        dao.set(tokenKey, loginId, 7200L);
        dao.setObject(sessionKey, new SaSession(sessionKey).set(SecurityConstants.LOGIN_USER_SESSION_KEY, loginUserMap), 7200L);

        LoginUser restored = interceptor.restoreFromTokenDaoFallback(tokenValue, dao);

        assertThat(restored).isNotNull();
        assertThat(restored.getUserId()).isEqualTo(502L);
        assertThat(restored.getPermissions()).containsExactly("wms:inventory:list");
    }

    @Test
    void daoFallbackShouldReturnNullWhenSessionMissesLoginUser() {
        MemorySaTokenDao dao = new MemorySaTokenDao();
        String tokenValue = "token-503";
        String loginId = "503";
        dao.set(StpUtil.getStpLogic().splicingKeyTokenValue(tokenValue), loginId, 7200L);
        dao.setObject(StpUtil.getStpLogic().splicingKeySession(loginId), new SaSession(), 7200L);

        LoginUser restored = interceptor.restoreFromTokenDaoFallback(tokenValue, dao);

        assertThat(restored).isNull();
    }

    private static class MemorySaTokenDao implements SaTokenDao {

        private final Map<String, String> strings = new HashMap<>();

        private final Map<String, Object> objects = new HashMap<>();

        @Override
        public String get(String key) {
            return strings.get(key);
        }

        @Override
        public void set(String key, String value, long timeout) {
            strings.put(key, value);
        }

        @Override
        public void update(String key, String value) {
            strings.put(key, value);
        }

        @Override
        public void delete(String key) {
            strings.remove(key);
        }

        @Override
        public long getTimeout(String key) {
            return strings.containsKey(key) ? 7200L : NOT_VALUE_EXPIRE;
        }

        @Override
        public void updateTimeout(String key, long timeout) {
        }

        @Override
        public Object getObject(String key) {
            return objects.get(key);
        }

        @Override
        public void setObject(String key, Object object, long timeout) {
            objects.put(key, object);
        }

        @Override
        public void updateObject(String key, Object object) {
            objects.put(key, object);
        }

        @Override
        public void deleteObject(String key) {
            objects.remove(key);
        }

        @Override
        public long getObjectTimeout(String key) {
            return objects.containsKey(key) ? 7200L : NOT_VALUE_EXPIRE;
        }

        @Override
        public void updateObjectTimeout(String key, long timeout) {
        }

        @Override
        public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
            return List.of();
        }
    }
}
