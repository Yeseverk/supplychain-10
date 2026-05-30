package com.lyf.supplychain.order.service.impl;

import com.lyf.supplychain.order.config.OrderPlatformPullProperties;
import com.lyf.supplychain.order.model.PlatformOrderPullContext;
import com.lyf.supplychain.order.model.PlatformOrderPullResult;
import com.lyf.supplychain.order.model.PlatformPulledOrder;
import com.lyf.supplychain.order.service.OrderMainService;
import com.lyf.supplychain.order.service.PlatformOrderPullClient;
import com.lyf.supplychain.order.service.PlatformOrderPullService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 平台订单主动拉取服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Service
public class PlatformOrderPullServiceImpl implements PlatformOrderPullService {

    private static final Logger log = LoggerFactory.getLogger(PlatformOrderPullServiceImpl.class);

    private final OrderPlatformPullProperties properties;
    private final OrderMainService orderMainService;
    private final Map<String, PlatformOrderPullClient> clientMap;

    public PlatformOrderPullServiceImpl(OrderPlatformPullProperties properties,
                                        OrderMainService orderMainService,
                                        List<PlatformOrderPullClient> clients) {
        this.properties = properties;
        this.orderMainService = orderMainService;
        this.clientMap = clients.stream().collect(Collectors.toMap(
                client -> normalize(client.platform()), Function.identity()));
    }

    /**
     * 按 XXL-JOB 分片拉取平台订单。
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @return 拉取结果
     */
    @Override
    public List<PlatformOrderPullResult> pullOrders(int shardIndex, int shardTotal) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return List.of();
        }
        List<String> platforms = shardingPlatforms(shardIndex, shardTotal);
        List<PlatformOrderPullResult> results = new ArrayList<>();
        for (String platform : platforms) {
            results.add(pullPlatform(platform));
        }
        return results;
    }

    private PlatformOrderPullResult pullPlatform(String platform) {
        PlatformOrderPullClient client = clientMap.get(normalize(platform));
        if (client == null) {
            log.warn("平台拉单客户端未配置，platform={}", platform);
            return result(platform, 0, 0, 1);
        }
        PlatformOrderPullContext context = PlatformOrderPullContext.builder()
                .platform(platform)
                .startTime(LocalDateTime.now().minusMinutes(properties.getLookbackMinutes()))
                .endTime(LocalDateTime.now())
                .batchSize(properties.getBatchSize())
                .build();
        List<PlatformPulledOrder> orders = client.pullOrders(context);
        int importedCount = 0;
        int failedCount = 0;
        for (PlatformPulledOrder order : orders) {
            try {
                orderMainService.importPlatformOrder(platform, order.getRawData());
                importedCount++;
            } catch (RuntimeException exception) {
                failedCount++;
                log.warn("平台订单导入失败，platform={}，platformOrderNo={}，原因={}",
                        platform, order.getPlatformOrderNo(), exception.getMessage());
            }
        }
        return result(platform, orders.size(), importedCount, failedCount);
    }

    private List<String> shardingPlatforms(int shardIndex, int shardTotal) {
        int total = Math.max(1, shardTotal);
        int index = Math.max(0, shardIndex);
        List<String> platforms = properties.getPlatforms() == null ? List.of() : properties.getPlatforms();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < platforms.size(); i++) {
            if (i % total == index) {
                result.add(platforms.get(i));
            }
        }
        return result;
    }

    private PlatformOrderPullResult result(String platform, int pulledCount, int importedCount, int failedCount) {
        return PlatformOrderPullResult.builder()
                .platform(platform)
                .pulledCount(pulledCount)
                .importedCount(importedCount)
                .failedCount(failedCount)
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
