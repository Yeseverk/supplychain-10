package com.lyf.supplychain.logistics.client;

import com.lyf.supplychain.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 物流商 API 适配器注册表。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Component
public class LogisticsCarrierApiClientRegistry {

    private final List<LogisticsCarrierApiClient> clients;

    public LogisticsCarrierApiClientRegistry(List<LogisticsCarrierApiClient> clients) {
        this.clients = clients;
    }

    /**
     * 根据物流商编码选择适配器。
     *
     * @param carrierCode 物流商编码
     * @return 物流商适配器
     */
    public LogisticsCarrierApiClient getClient(String carrierCode) {
        return clients.stream()
                .filter(client -> client.supports(carrierCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(16013, "物流商API适配器不存在"));
    }
}
