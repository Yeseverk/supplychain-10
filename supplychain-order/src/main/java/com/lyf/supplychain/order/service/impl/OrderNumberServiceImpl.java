package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.order.service.OrderNumberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单编号服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class OrderNumberServiceImpl implements OrderNumberService {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 生成业务编号。
     *
     * @param prefix 编号前缀
     * @return 业务编号
     */
    @Override
    public String nextNo(String prefix) {
        String date = LocalDate.now().toString().replace("-", "");
        int sequence = counters.computeIfAbsent(prefix + date, key -> new AtomicInteger()).incrementAndGet();
        return prefix + "-" + date + "-" + String.format("%04d", sequence);
    }
}
