package com.lyf.supplychain.common.redis;

import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Redis 分布式锁模板。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class RedisDistributedLockTemplate {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLockTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在分布式锁保护下执行逻辑。
     *
     * @param lockKey    锁 Key
     * @param expireTime 锁过期时间
     * @param supplier   业务逻辑
     * @param <T>        返回类型
     * @return 业务返回
     */
    public <T> T execute(String lockKey, Duration expireTime, Supplier<T> supplier) {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, token, expireTime);
        if (!Boolean.TRUE.equals(locked)) {
            BusinessException.throwException("操作太频繁，请稍后再试");
        }
        try {
            return supplier.get();
        } finally {
            unlock(lockKey, token);
        }
    }

    /**
     * 释放锁，只允许锁持有者删除。
     *
     * @param lockKey 锁 Key
     * @param token   锁令牌
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String token) {
        if (StrUtil.hasBlank(lockKey, token)) {
            return false;
        }
        Long result = redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        return Long.valueOf(1L).equals(result);
    }
}
