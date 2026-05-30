package com.lyf.supplychain.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 拣货进度请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class PickProgressRequest {

    private Long pickUserId;
    @Valid
    @NotEmpty(message = "拣货明细不能为空")
    private List<WmsItemRequest> items;
}
