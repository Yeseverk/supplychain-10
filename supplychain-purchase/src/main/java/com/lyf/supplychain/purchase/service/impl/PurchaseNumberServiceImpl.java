package com.lyf.supplychain.purchase.service.impl;

import com.lyf.supplychain.purchase.service.PurchaseNumberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 采购单据编号服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class PurchaseNumberServiceImpl implements PurchaseNumberService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 生成采购模块业务单号。
     *
     * @param prefix 单号前缀
     * @return 业务单号
     */
    @Override
    public String nextNo(String prefix) {
        String date = LocalDate.now().format(DAY_FORMATTER);
        String key = prefix + date;
        int sequence = counters.computeIfAbsent(key, ignored -> new AtomicInteger()).incrementAndGet();
        return "%s-%s-%04d".formatted(prefix, date, sequence);
    }
}
