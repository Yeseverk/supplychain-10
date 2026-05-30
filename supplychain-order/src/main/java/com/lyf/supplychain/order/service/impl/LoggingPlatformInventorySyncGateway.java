package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import com.lyf.supplychain.order.service.PlatformInventorySyncGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 日志型平台库存同步网关。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class LoggingPlatformInventorySyncGateway implements PlatformInventorySyncGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingPlatformInventorySyncGateway.class);

    /**
     * 记录平台同步动作，后续可替换为 Amazon、Shopify、eBay 等平台真实适配器。
     *
     * @param allocation 平台库存分配
     * @return 同步结果描述
     */
    @Override
    public String syncInventory(PlatformInventoryAllocation allocation) {
        String message = "已模拟同步平台库存，platform=%s，skuId=%s，availableQty=%s"
                .formatted(allocation.getPlatform(), allocation.getSkuId(), allocation.getAvailableQty());
        log.info(message);
        return message;
    }
}
