package com.lyf.supplychain.common.idempotent;

import com.lyf.supplychain.common.redis.CommonRedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 幂等标记服务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class RedisIdempotentService {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotentService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 首次标记成功返回 true，重复标记返回 false。
     *
     * @param scene      业务场景
     * @param token      幂等令牌
     * @param expireTime 过期时间
     * @return 是否首次处理
     */
    public boolean markIfAbsent(String scene, String token, Duration expireTime) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                CommonRedisKeys.idempotent(scene, token),
                "1",
                expireTime
        );
        return Boolean.TRUE.equals(success);
    }
}
