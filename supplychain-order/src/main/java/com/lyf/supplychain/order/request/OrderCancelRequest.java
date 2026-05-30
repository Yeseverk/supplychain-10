package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单取消请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderCancelRequest {

    @NotBlank(message = "取消原因不能为空")
    private String reason;
}
