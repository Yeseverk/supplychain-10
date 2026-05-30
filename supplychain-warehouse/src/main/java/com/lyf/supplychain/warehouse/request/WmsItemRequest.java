package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * WMS 单据明细请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class WmsItemRequest {

    private Long itemId;
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;
    @NotBlank(message = "SKU名称不能为空")
    private String skuName;
    @NotNull(message = "数量不能为空")
    private Integer quantity;
    private Integer actualQty;
    private Integer defectiveQty;
    private Long locationId;
    private String locationCode;
    private BigDecimal unitCost;
    private String remark;
}
