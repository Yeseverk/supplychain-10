package com.lyf.supplychain.warehouse.service.impl;

import com.lyf.supplychain.warehouse.service.WmsNumberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WMS 单号生成服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WmsNumberServiceImpl implements WmsNumberService {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 生成 WMS 业务单号。
     *
     * @param prefix 单号前缀
     * @return 业务单号
     */
    @Override
    public String nextNo(String prefix) {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int sequence = counters.computeIfAbsent(prefix + day, key -> new AtomicInteger()).incrementAndGet();
        return "%s-%s-%04d".formatted(prefix, day, sequence);
    }
}
