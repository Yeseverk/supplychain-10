package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单异常标记请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderFlagRequest {

    @NotBlank(message = "异常原因不能为空")
    private String reason;
}
