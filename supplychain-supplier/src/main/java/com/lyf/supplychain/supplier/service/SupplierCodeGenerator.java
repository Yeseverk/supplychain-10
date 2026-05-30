package com.lyf.supplychain.supplier.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lyf.supplychain.supplier.constant.SupplierRedisKeys;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 供应商编码生成器。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Slf4j
@Component
public class SupplierCodeGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String CODE_PREFIX = "SUP-";

    private final StringRedisTemplate redisTemplate;

    private final SupplierMapper supplierMapper;

    private final Clock clock;

    public SupplierCodeGenerator(StringRedisTemplate redisTemplate, SupplierMapper supplierMapper, Clock clock) {
        this.redisTemplate = redisTemplate;
        this.supplierMapper = supplierMapper;
        this.clock = clock;
    }

    /**
     * 生成供应商编码，优先使用 Redis 自增长，Redis 不可用时使用 MySQL 最大编码兜底。
     *
     * @param tenantId 租户ID
     * @return 供应商编码
     */
    public String generate(Long tenantId) {
        // 当天的日期格式化成字符串
        String dateText = LocalDate.now(clock).format(DATE_FORMATTER);
        String prefix = CODE_PREFIX + dateText + "-";
        // 这一步 如果是Redis中第一次新增 成功 1 如果是Redis中已经有了 成功 100
        long sequence = nextRedisSequence(tenantId, dateText);
        if (sequence <= 0) {
            // Redis不可用 走MySQL进行兜底
            sequence = nextMysqlSequence(tenantId, prefix);
        }
        // 根据租户的ID 供应商编码前缀 当前可用的序号 进行拼接
        // 但是在拼接的过程中 也有可能 当前的这个序号已经被其他线程使用了
        // 所以我们还要判断拼接好后的这个序号是否存在 / 已经被使用
        // 如果存在 / 已经被使用后了 就累加
        return nextAvailableCode(tenantId, prefix, sequence);
    }

    private long nextRedisSequence(Long tenantId, String dateText) {
        try {
            Long sequence = redisTemplate.opsForValue().increment(SupplierRedisKeys.supplierCode(tenantId, dateText));
            return sequence == null ? 0L : sequence;
        } catch (RuntimeException exception) {
            log.warn("Redis生成供应商编码失败，切换MySQL兜底。tenantId={}, date={}, error={}",
                    tenantId, dateText, exception.getMessage());
            return 0L;
        }
    }

    private long nextMysqlSequence(Long tenantId, String prefix) {
        // MySQL查询 条件 tenantId
        String maxCode = supplierMapper.selectMaxSupplierCode(tenantId, prefix);
        if (maxCode == null || maxCode.length() < prefix.length() + 4) {
            return 1L;
        }
        return Long.parseLong(maxCode.substring(maxCode.length() - 4)) + 1;
    }

    private String nextAvailableCode(Long tenantId, String prefix, long sequence) {
        long currentSequence = sequence;
        while (true) {
            String code = prefix + String.format("%04d", currentSequence);
            Long count = supplierMapper.selectCount(Wrappers.<Supplier>lambdaQuery()
                    .eq(Supplier::getTenantId, tenantId)
                    .eq(Supplier::getSupplierCode, code));
            if (count == null || count == 0) {
                return code;
            }
            currentSequence++;
        }
    }
}
