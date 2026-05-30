package com.lyf.supplychain.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderCreateRequest {

    @NotBlank(message = "平台不能为空")
    private String platform;

    @NotBlank(message = "平台订单号不能为空")
    private String platformOrderNo;

    private Long storeId;

    @NotBlank(message = "币种不能为空")
    private String currency;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal shippingFee = BigDecimal.ZERO;

    private BigDecimal exchangeRate = BigDecimal.ONE;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private LocalDate deliveryDeadline;

    private LocalDateTime platformOrderTime;

    private LocalDateTime platformPayTime;

    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

    @Valid
    @NotNull(message = "收货地址不能为空")
    private OrderAddressRequest address;
}
