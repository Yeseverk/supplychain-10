package com.lyf.supplychain.order.service;

import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;

/**
 * 平台库存同步网关。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface PlatformInventorySyncGateway {

    /**
     * 将当前平台可售库存同步到外部电商平台。
     *
     * @param allocation 平台库存分配
     * @return 同步结果描述
     */
    String syncInventory(PlatformInventoryAllocation allocation);
}
