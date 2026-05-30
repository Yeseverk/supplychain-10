package com.lyf.supplychain.order.job;

import com.lyf.supplychain.order.model.PlatformOrderPullResult;
import com.lyf.supplychain.order.service.PlatformOrderPullService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 平台订单主动拉取任务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class OrderPlatformPullJob {

    private static final Logger log = LoggerFactory.getLogger(OrderPlatformPullJob.class);

    private final PlatformOrderPullService platformOrderPullService;

    public OrderPlatformPullJob(PlatformOrderPullService platformOrderPullService) {
        this.platformOrderPullService = platformOrderPullService;
    }

    /**
     * 按 XXL-JOB 分片拉取 Amazon、eBay 等非 Webhook 平台订单。
     */
    @XxlJob("omsPlatformOrderPullJob")
    public void pullPlatformOrders() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<PlatformOrderPullResult> results = platformOrderPullService.pullOrders(shardIndex, shardTotal);
        int pulledCount = results.stream().mapToInt(PlatformOrderPullResult::getPulledCount).sum();
        int importedCount = results.stream().mapToInt(PlatformOrderPullResult::getImportedCount).sum();
        int failedCount = results.stream().mapToInt(PlatformOrderPullResult::getFailedCount).sum();
        log.info("OMS平台订单拉取任务完成，shardIndex={}，shardTotal={}，pulledCount={}，importedCount={}，failedCount={}",
                shardIndex, shardTotal, pulledCount, importedCount, failedCount);
    }
}
