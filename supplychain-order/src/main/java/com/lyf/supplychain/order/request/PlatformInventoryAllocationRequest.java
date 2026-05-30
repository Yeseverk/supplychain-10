package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 平台库存分配保存请求。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Data
public class PlatformInventoryAllocationRequest {

    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @NotBlank(message = "平台不能为空")
    private String platform;

    private Long storeId;

    @NotNull(message = "分配库存不能为空")
    @Min(value = 0, message = "分配库存不能小于0")
    private Integer allocatedQty;

    @Min(value = 0, message = "分配比例不能小于0")
    @Max(value = 100, message = "分配比例不能大于100")
    private Integer allocationRatio;
}
