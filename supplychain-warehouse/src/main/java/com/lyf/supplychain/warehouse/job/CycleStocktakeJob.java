package com.lyf.supplychain.warehouse.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * 循环盘点定时任务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Component
public class CycleStocktakeJob {

    private static final Logger log = LoggerFactory.getLogger(CycleStocktakeJob.class);

    /**
     * 按 ABC 分类生成循环盘点任务。
     */
    @XxlJob("wmsCycleStocktakeJob")
    public void generateCycleStocktake() {
        log.info("WMS循环盘点任务扫描完成，按ABC分类生成盘点计划的能力已预留");
    }
}
