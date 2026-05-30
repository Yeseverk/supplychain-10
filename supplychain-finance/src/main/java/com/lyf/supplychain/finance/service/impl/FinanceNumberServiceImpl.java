package com.lyf.supplychain.finance.service.impl;

import com.lyf.supplychain.finance.service.FinanceNumberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 财务单号服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class FinanceNumberServiceImpl implements FinanceNumberService {

    private final AtomicInteger counter = new AtomicInteger();

    /**
     * 生成应付账款单号。
     *
     * @return 应付账款单号
     */
    @Override
    public String nextPayableNo() {
        return "PAY-%s-%04d".formatted(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                counter.incrementAndGet());
    }

    /**
     * 生成平台账单编号。
     *
     * @return 平台账单编号
     */
    @Override
    public String nextBillNo() {
        return "BILL-%s-%04d".formatted(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                counter.incrementAndGet());
    }
}
