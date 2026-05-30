package com.lyf.supplychain.common.redis;

import com.lyf.supplychain.common.idempotent.RedisIdempotentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 公共 Redis 基础设施单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class CommonRedisInfrastructureTest {

    @Test
    void commonRedisKeysShouldBuildStableKeys() {
        assertThat(CommonRedisKeys.permission(501L)).isEqualTo("supplychain:permission:501");
        assertThat(CommonRedisKeys.idempotent("order:create", "REQ-001")).isEqualTo("supplychain:idempotent:order:create:REQ-001");
        assertThat(CommonRedisKeys.lock("inventory", "SKU001")).isEqualTo("supplychain:lock:inventory:SKU001");
        assertThat(CommonRedisKeys.wmsInventorySku(101L, 1001L)).isEqualTo("supplychain:wms:inventory:sku:101:1001");
    }

    @Test
    void idempotentServiceShouldReturnTrueOnlyFirstTime() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("supplychain:idempotent:order:create:REQ-001"), eq("1"), eq(Duration.ofMinutes(5))))
                .thenReturn(true)
                .thenReturn(false);
        RedisIdempotentService service = new RedisIdempotentService(redisTemplate);

        assertThat(service.markIfAbsent("order:create", "REQ-001", Duration.ofMinutes(5))).isTrue();
        assertThat(service.markIfAbsent("order:create", "REQ-001", Duration.ofMinutes(5))).isFalse();
    }

    @Test
    void lockTemplateShouldReleaseLockByCompareTokenScript() {
        StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
        when(redisTemplate.execute(Mockito.any(DefaultRedisScript.class), eq(List.of("supplychain:lock:inventory:SKU001")), eq("token-1")))
                .thenReturn(1L);
        RedisDistributedLockTemplate lockTemplate = new RedisDistributedLockTemplate(redisTemplate);

        boolean unlocked = lockTemplate.unlock("supplychain:lock:inventory:SKU001", "token-1");

        assertThat(unlocked).isTrue();
    }
}
