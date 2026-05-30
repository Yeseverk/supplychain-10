package com.lyf.supplychain.logistics.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 物流费率保存请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class RateRequest {

    @Valid
    @NotEmpty(message = "费率不能为空")
    private List<RateItemRequest> rates;
}
