package com.lyf.supplychain.purchase.service;

import com.lyf.supplychain.purchase.model.PurchaseReplenishmentResult;

/**
 * 采购自动补货服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PurchaseReplenishmentService {

    /**
     * 按 XXL-JOB 分片扫描库存预警并自动生成采购申请。
     *
     * @param shardIndex 当前分片下标
     * @param shardTotal 分片总数
     * @return 执行结果
     */
    PurchaseReplenishmentResult scanAndGenerate(int shardIndex, int shardTotal);
}
