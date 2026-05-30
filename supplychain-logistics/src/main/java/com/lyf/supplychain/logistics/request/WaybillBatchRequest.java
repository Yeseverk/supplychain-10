package com.lyf.supplychain.logistics.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量创建运单请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class WaybillBatchRequest {

    @Valid
    @NotEmpty(message = "运单列表不能为空")
    private List<WaybillRequest> waybills;
}
