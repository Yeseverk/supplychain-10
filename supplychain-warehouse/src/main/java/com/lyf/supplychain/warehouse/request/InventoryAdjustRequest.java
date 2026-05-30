package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人工库存调整请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class InventoryAdjustRequest {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;
    private Long locationId;
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;
    private String skuName;
    @NotNull(message = "调整数量不能为空")
    private Integer changeQty;
    private BigDecimal unitCost;
    private Integer logType;
    private String refType;
    private String refNo;
    private Long refId;
    private Long operatorId;
    private String operatorName;
    private String remark;
}
