package com.lyf.supplychain.warehouse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.redis.CommonRedisKeys;
import com.lyf.supplychain.common.redis.RedisDistributedLockTemplate;
import com.lyf.supplychain.warehouse.config.WmsInventoryCacheProperties;
import com.lyf.supplychain.warehouse.entity.Inventory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * WMS 库存 Redis 缓存服务。
 *
 * @author liyunfei
 * @date 2026-05-24
 */
@Slf4j
@Service
public class WmsInventoryCacheService {

    private static final TypeReference<List<Inventory>> INVENTORY_LIST_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final RedisDistributedLockTemplate lockTemplate;
    private final ObjectMapper objectMapper;
    private final WmsInventoryCacheProperties properties;

    public WmsInventoryCacheService(StringRedisTemplate redisTemplate,
                                    RedisDistributedLockTemplate lockTemplate,
                                    ObjectMapper objectMapper,
                                    WmsInventoryCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.lockTemplate = lockTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 读取 SKU 多仓库存缓存，未命中时使用分布式锁回源数据库。
     *
     * @param tenantId 租户ID
     * @param skuId    SKU ID
     * @param loader   回源查询
     * @return SKU 多仓库存详情
     */
    public List<Inventory> getSkuDetail(Long tenantId, Long skuId, Supplier<List<Inventory>> loader) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return loader.get();
        }
        String cacheKey = CommonRedisKeys.wmsInventorySku(tenantId, skuId);
        try {
            List<Inventory> cached = read(cacheKey);
            if (cached != null) {
                return cached;
            }
            String lockKey = CommonRedisKeys.lock("wms:inventory:sku", tenantId + ":" + skuId);
            return lockTemplate.execute(lockKey, Duration.ofSeconds(properties.getLockSeconds()), () -> loadAndCache(cacheKey, loader));
        } catch (Exception exception) {
            log.warn("读取WMS库存缓存失败，tenantId={}，skuId={}，fallback=db，原因={}",
                    tenantId, skuId, exception.getMessage());
            return loader.get();
        }
    }

    /**
     * 在事务提交后删除 SKU 库存缓存。
     *
     * @param tenantId 租户ID
     * @param skuId    SKU ID
     */
    public void evictSkuDetailAfterCommit(Long tenantId, Long skuId) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return;
        }
        Runnable evictTask = () -> evictSkuDetail(tenantId, skuId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictTask.run();
                }
            });
            return;
        }
        evictTask.run();
    }

    private List<Inventory> loadAndCache(String cacheKey, Supplier<List<Inventory>> loader) {
        List<Inventory> cached = read(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<Inventory> inventories = loader.get();
        write(cacheKey, inventories);
        return inventories;
    }

    private List<Inventory> read(String cacheKey) {
        String value = redisTemplate.opsForValue().get(cacheKey);
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, INVENTORY_LIST_TYPE);
        } catch (Exception exception) {
            log.warn("WMS库存缓存反序列化失败，cacheKey={}", cacheKey, exception);
            redisTemplate.delete(cacheKey);
            return null;
        }
    }

    private void write(String cacheKey, List<Inventory> inventories) {
        try {
            long ttlMinutes = inventories == null || inventories.isEmpty()
                    ? properties.getEmptyTtlMinutes()
                    : properties.getTtlMinutes();
            long randomSeconds = Math.max(0L, properties.getRandomSeconds());
            long extraSeconds = randomSeconds == 0 ? 0 : ThreadLocalRandom.current().nextLong(randomSeconds + 1);
            String value = objectMapper.writeValueAsString(inventories == null ? List.of() : inventories);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofMinutes(ttlMinutes).plusSeconds(extraSeconds));
        } catch (Exception exception) {
            log.warn("写入WMS库存缓存失败，cacheKey={}", cacheKey, exception);
        }
    }

    private void evictSkuDetail(Long tenantId, Long skuId) {
        try {
            redisTemplate.delete(CommonRedisKeys.wmsInventorySku(tenantId, skuId));
        } catch (Exception exception) {
            log.warn("删除WMS库存缓存失败，tenantId={}，skuId={}，原因={}", tenantId, skuId, exception.getMessage());
        }
    }
}
