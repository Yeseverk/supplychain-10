package com.lyf.supplychain.order.service;

import com.lyf.supplychain.order.model.PlatformOrderPullResult;

import java.util.List;

/**
 * 平台订单主动拉取服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformOrderPullService {

    /**
     * 按 XXL-JOB 分片拉取平台订单。
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @return 拉取结果
     */
    List<PlatformOrderPullResult> pullOrders(int shardIndex, int shardTotal);
}
