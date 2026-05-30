package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderItemRequest {

    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    private String platformSkuId;

    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private BigDecimal discount = BigDecimal.ZERO;
}
