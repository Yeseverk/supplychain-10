package com.lyf.supplychain.finance.job;

import com.lyf.supplychain.finance.service.FinanceSettlementBiService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 汇率刷新 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class FinanceExchangeRateJob {

    private final FinanceSettlementBiService financeSettlementBiService;

    public FinanceExchangeRateJob(FinanceSettlementBiService financeSettlementBiService) {
        this.financeSettlementBiService = financeSettlementBiService;
    }

    /**
     * 每日刷新财务汇率。
     */
    @XxlJob("financeExchangeRateJob")
    public void execute() {
        Map<String, Object> result = financeSettlementBiService.refreshExchangeRates();
        String message = "汇率刷新任务完成，结果=" + result;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
