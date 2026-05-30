package com.lyf.supplychain.supplier.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.mapper.SupplierMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 供应商编码生成器测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@ExtendWith(MockitoExtension.class)
class SupplierCodeGeneratorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierCodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-16T01:23:45Z"), ZoneId.of("Asia/Shanghai"));
        codeGenerator = new SupplierCodeGenerator(redisTemplate, supplierMapper, clock);
    }

    @Test
    void generateShouldUseRedisIncrementFirst() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("supplychain:supplier:code:0:20260516")).thenReturn(7L);
        when(supplierMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        String code = codeGenerator.generate(0L);

        assertThat(code).isEqualTo("SUP-20260516-0007");
    }

    @Test
    void generateShouldFallbackToMysqlMaxCodeWhenRedisUnavailable() {
        when(redisTemplate.opsForValue()).thenThrow(new IllegalStateException("redis unavailable"));
        when(supplierMapper.selectMaxSupplierCode(eq(0L), eq("SUP-20260516-"))).thenReturn("SUP-20260516-0012");
        when(supplierMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        String code = codeGenerator.generate(0L);

        assertThat(code).isEqualTo("SUP-20260516-0013");
    }
}
