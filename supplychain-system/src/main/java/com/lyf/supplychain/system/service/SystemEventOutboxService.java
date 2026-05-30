package com.lyf.supplychain.system.service;

import com.lyf.supplychain.common.feign.system.SystemEventPublishRequest;

/**
 * 系统可靠事件服务接口。
 *
 * @author liyunfei
 * @date 2026-05-21
 */
public interface SystemEventOutboxService {

    /**
     * 发布可靠事件，先落库再按事件类型分发。
     *
     * @param request 事件发布请求
     * @return 事件记录ID
     */
    Long publish(SystemEventPublishRequest request);

    /**
     * 扫描待投递事件并进行分发。
     *
     * @param batchSize 单批处理数量
     * @return 本次投递数量
     */
    int dispatchPending(Integer batchSize);

    /**
     * 重试失败的可靠事件。
     *
     * @param maxRetryCount 最大重试次数
     * @param batchSize     单批处理数量
     * @return 本次重试数量
     */
    int retryFailed(Integer maxRetryCount, Integer batchSize);
}
