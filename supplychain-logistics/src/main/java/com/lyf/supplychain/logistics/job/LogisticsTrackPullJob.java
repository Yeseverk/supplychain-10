package com.lyf.supplychain.logistics.job;

import com.lyf.supplychain.logistics.service.LogisticsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * 物流轨迹拉取 XXL-JOB 任务。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
public class LogisticsTrackPullJob {

    private final LogisticsService logisticsService;

    public LogisticsTrackPullJob(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 拉取待运输运单的最新轨迹。
     */
    @XxlJob("logisticsTrackPullJob")
    public void execute() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        int count = logisticsService.pullTracks(shardIndex, shardTotal);
        String message = "物流轨迹拉取完成，分片=" + shardIndex + "/" + shardTotal + "，处理运单数=" + count;
        XxlJobHelper.log(message);
        XxlJobHelper.handleSuccess(message);
    }
}
