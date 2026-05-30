package com.lyf.supplychain.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.common.redis.RedisDistributedLockTemplate;
import com.lyf.supplychain.warehouse.config.WmsInventoryCacheProperties;
import com.lyf.supplychain.warehouse.entity.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WMS 库存缓存服务测试。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@ExtendWith(MockitoExtension.class)
class WmsInventoryCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisDistributedLockTemplate lockTemplate;

    private WmsInventoryCacheService cacheService;

    @BeforeEach
    void setUp() {
        WmsInventoryCacheProperties properties = new WmsInventoryCacheProperties();
        properties.setRandomSeconds(0L);
        cacheService = new WmsInventoryCacheService(redisTemplate, lockTemplate, new ObjectMapper(), properties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getSkuDetailShouldReturnCacheHitWithoutDbLoader() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setSkuId(1001L);
        inventory.setQuantity(10);
        when(valueOperations.get(CommonRedisKeys.wmsInventorySku(101L, 1001L)))
                .thenReturn(new ObjectMapper().writeValueAsString(List.of(inventory)));

        List<Inventory> result = cacheService.getSkuDetail(101L, 1001L, () -> {
            throw new AssertionError("缓存命中时不应该回源数据库");
        });

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkuId()).isEqualTo(1001L);
        verify(lockTemplate, never()).execute(any(), any(), any());
    }

    @Test
    void getSkuDetailShouldLoadDbWithLockAndWriteCacheWhenMissed() {
        when(valueOperations.get(CommonRedisKeys.wmsInventorySku(101L, 1001L))).thenReturn(null);
        when(lockTemplate.execute(eq(CommonRedisKeys.lock("wms:inventory:sku", "101:1001")),
                any(Duration.class), any())).thenAnswer(invocation -> {
                    Supplier<List<Inventory>> supplier = invocation.getArgument(2);
                    return supplier.get();
                });
        AtomicInteger loadTimes = new AtomicInteger();

        List<Inventory> result = cacheService.getSkuDetail(101L, 1001L, () -> {
            loadTimes.incrementAndGet();
            Inventory inventory = new Inventory();
            inventory.setSkuId(1001L);
            inventory.setQuantity(20);
            return List.of(inventory);
        });

        assertThat(result).hasSize(1);
        assertThat(loadTimes).hasValue(1);
        verify(valueOperations).set(eq(CommonRedisKeys.wmsInventorySku(101L, 1001L)),
                any(String.class), eq(Duration.ofMinutes(60)));
    }
}
