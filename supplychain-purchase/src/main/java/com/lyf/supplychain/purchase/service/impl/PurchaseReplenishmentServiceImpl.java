package com.lyf.supplychain.purchase.service.impl;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.warehouse.WarehouseFeignClient;
import com.lyf.supplychain.common.feign.warehouse.WarehouseInventoryWarningResponse;
import com.lyf.supplychain.purchase.config.PurchaseReplenishmentProperties;
import com.lyf.supplychain.purchase.model.PurchaseReplenishmentResult;
import com.lyf.supplychain.purchase.request.PurchaseItemRequest;
import com.lyf.supplychain.purchase.request.PurchaseRequisitionRequest;
import com.lyf.supplychain.purchase.service.PurchaseReplenishmentQuantityCalculator;
import com.lyf.supplychain.purchase.service.PurchaseReplenishmentService;
import com.lyf.supplychain.purchase.service.PurchaseRequisitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购自动补货服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Slf4j
@Service
public class PurchaseReplenishmentServiceImpl implements PurchaseReplenishmentService {

    private final WarehouseFeignClient warehouseFeignClient;

    private final PurchaseRequisitionService requisitionService;

    private final PurchaseReplenishmentQuantityCalculator quantityCalculator;

    private final PurchaseReplenishmentProperties properties;

    public PurchaseReplenishmentServiceImpl(WarehouseFeignClient warehouseFeignClient,
                                            PurchaseRequisitionService requisitionService,
                                            PurchaseReplenishmentQuantityCalculator quantityCalculator,
                                            PurchaseReplenishmentProperties properties) {
        this.warehouseFeignClient = warehouseFeignClient;
        this.requisitionService = requisitionService;
        this.quantityCalculator = quantityCalculator;
        this.properties = properties;
    }

    /**
     * 按 XXL-JOB 分片扫描库存预警并自动生成采购申请。
     *
     * @param shardIndex 当前分片下标
     * @param shardTotal 分片总数
     * @return 执行结果
     */
    @Override
    public PurchaseReplenishmentResult scanAndGenerate(int shardIndex, int shardTotal) {
        PurchaseReplenishmentResult result = new PurchaseReplenishmentResult();
        List<WarehouseInventoryWarningResponse> warnings = queryWarnings();
        int safeShardTotal = shardTotal <= 0 ? 1 : shardTotal;
        int safeShardIndex = Math.max(shardIndex, 0);
        for (WarehouseInventoryWarningResponse warning : warnings) {
            if (!matchShard(warning, safeShardIndex, safeShardTotal)) {
                continue;
            }
            result.incrementScanned();
            processWarning(warning, result);
        }
        return result;
    }

    private List<WarehouseInventoryWarningResponse> queryWarnings() {
        R<List<WarehouseInventoryWarningResponse>> response = warehouseFeignClient.warnings();
        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData();
    }

    private boolean matchShard(WarehouseInventoryWarningResponse warning, int shardIndex, int shardTotal) {
        Long skuId = warning.getSkuId();
        if (skuId == null) {
            return false;
        }
        return Math.floorMod(skuId, shardTotal) == shardIndex;
    }

    private void processWarning(WarehouseInventoryWarningResponse warning, PurchaseReplenishmentResult result) {
        int suggestQty = quantityCalculator.calculate(warning, properties.getSafetyStockMultiplier());
        if (suggestQty <= 0) {
            result.incrementSkipped();
            return;
        }
        Long previousTenantId = TenantContext.getTenantId();
        Long previousUserId = TenantContext.getUserId();
        try {
            TenantContext.set(warning.getTenantId(), properties.getApplyUserId());
            Long reqId = requisitionService.create(buildRequest(warning, suggestQty));
            result.addGenerated(reqId);
        } catch (BusinessException exception) {
            log.info("自动补货跳过，skuId={}, reason={}", warning.getSkuId(), exception.getMessage());
            result.incrementSkipped();
        } catch (RuntimeException exception) {
            log.error("自动补货生成采购申请失败，skuId={}", warning.getSkuId(), exception);
            result.incrementFailed();
        } finally {
            if (previousTenantId == null && previousUserId == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(previousTenantId, previousUserId);
            }
        }
    }

    private PurchaseRequisitionRequest buildRequest(WarehouseInventoryWarningResponse warning, int suggestQty) {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setSkuId(warning.getSkuId());
        item.setSkuCode(warning.getSkuCode());
        item.setSkuName(warning.getSkuName());
        item.setQuantity(suggestQty);
        item.setCurrentStock(warning.getAvailableQty());
        item.setSafetyStock(warning.getSafetyStock());
        item.setInTransitQty(warning.getInTransitQty());
        item.setExpectDate(LocalDate.now().plusDays(properties.getLeadTimeDays()));
        item.setRemark("库存预警自动生成，当前可用库存：" + warning.getAvailableQty());

        PurchaseRequisitionRequest request = new PurchaseRequisitionRequest();
        request.setReqSource(1);
        request.setTitle("库存预警自动补货-" + warning.getSkuCode());
        request.setWarehouseId(warning.getWarehouseId());
        request.setExpectDate(item.getExpectDate());
        request.setPriority(1);
        request.setApplyUserId(properties.getApplyUserId());
        request.setApplyUserName(properties.getApplyUserName());
        request.setRemark("XXL-JOB 根据 WMS 库存预警自动生成");
        request.setItems(List.of(item));
        return request;
    }
}
