package com.lyf.supplychain.order.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 订单合单请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class OrderMergeRequest {

    @NotEmpty(message = "合单订单不能为空")
    private List<Long> orderIds;
}
