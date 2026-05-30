package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 平台库存分配调整请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PlatformInventoryAllocationAdjustRequest {

    @NotNull(message = "调整后的分配库存不能为空")
    @Min(value = 0, message = "调整后的分配库存不能小于0")
    private Integer allocatedQty;

    @Min(value = 0, message = "分配比例不能小于0")
    private Integer allocationRatio;
}
