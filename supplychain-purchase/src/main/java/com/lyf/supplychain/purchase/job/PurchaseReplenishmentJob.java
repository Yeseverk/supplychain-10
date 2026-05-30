package com.lyf.supplychain.purchase.job;

import com.lyf.supplychain.purchase.model.PurchaseReplenishmentResult;
import com.lyf.supplychain.purchase.service.PurchaseReplenishmentService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 采购自动补货 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class PurchaseReplenishmentJob {

    private final PurchaseReplenishmentService replenishmentService;

    public PurchaseReplenishmentJob(PurchaseReplenishmentService replenishmentService) {
        this.replenishmentService = replenishmentService;
    }

    /**
     * XXL-JOB 入口，按分片扫描 WMS 库存预警并生成采购申请。
     */
    @XxlJob("purchaseReplenishmentJob")
    public void execute() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        PurchaseReplenishmentResult result = replenishmentService.scanAndGenerate(shardIndex, shardTotal);
        String message = "采购自动补货完成，扫描=" + result.getScannedCount()
                + "，生成=" + result.getGeneratedCount()
                + "，跳过=" + result.getSkippedCount()
                + "，失败=" + result.getFailedCount();
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
