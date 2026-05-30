package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyf.supplychain.warehouse.entity.Inventory;
import com.lyf.supplychain.warehouse.entity.InventoryLog;
import com.lyf.supplychain.warehouse.mapper.InventoryLogMapper;
import com.lyf.supplychain.warehouse.mapper.InventoryMapper;
import com.lyf.supplychain.warehouse.service.WmsReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WMS 报表服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WmsReportServiceImpl implements WmsReportService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;

    public WmsReportServiceImpl(InventoryMapper inventoryMapper, InventoryLogMapper inventoryLogMapper) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }

    /**
     * 查询库存总览。
     *
     * @return 总览指标
     */
    @Override
    public Map<String, Object> overview() {
        int quantity = 0;
        int available = 0;
        int defective = 0;
        int inTransit = 0;
        BigDecimal value = BigDecimal.ZERO;
        for (Inventory inventory : inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>())) {
            quantity += safe(inventory.getQuantity());
            available += safe(inventory.getQuantity()) - safe(inventory.getFrozenQty())
                    - safe(inventory.getDefectiveQty()) - safe(inventory.getReservedQty());
            defective += safe(inventory.getDefectiveQty());
            inTransit += safe(inventory.getInTransitQty());
            value = value.add(inventory.getTotalCost() == null ? BigDecimal.ZERO : inventory.getTotalCost());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("quantity", quantity);
        result.put("available", available);
        result.put("defective", defective);
        result.put("inTransit", inTransit);
        result.put("inventoryValue", value);
        return result;
    }

    /**
     * 查询库存健康度。
     *
     * @return 健康度指标
     */
    @Override
    public Map<String, Object> health() {
        long warningCount = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>())
                .stream()
                .filter(inventory -> safe(inventory.getQuantity()) - safe(inventory.getFrozenQty())
                        - safe(inventory.getDefectiveQty()) - safe(inventory.getReservedQty()) <= safe(inventory.getSafetyStock()))
                .count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("warningSkuCount", warningCount);
        result.put("healthScore", Math.max(0, 100 - warningCount * 5));
        return result;
    }

    /**
     * 查询出入库趋势。
     *
     * @return 趋势指标
     */
    @Override
    public Map<String, Object> trend() {
        long inboundTimes = inventoryLogMapper.selectCount(new LambdaQueryWrapper<InventoryLog>()
                .in(InventoryLog::getLogType, 1, 3, 5, 7));
        long outboundTimes = inventoryLogMapper.selectCount(new LambdaQueryWrapper<InventoryLog>()
                .in(InventoryLog::getLogType, 2, 4, 6, 8));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inboundTimes", inboundTimes);
        result.put("outboundTimes", outboundTimes);
        return result;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
