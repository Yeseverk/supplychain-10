package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款创建请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class RefundCreateRequest {

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "退款类型不能为空")
    private Integer refundType;

    @NotBlank(message = "退款原因不能为空")
    private String refundReason;

    private String reasonDetail;

    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    private String evidenceUrls;
}
