package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 订单拆单请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderSplitRequest {

    @NotEmpty(message = "拆单SKU不能为空")
    private List<Long> skuIds;
}
