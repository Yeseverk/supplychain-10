package com.lyf.supplychain.logistics.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 运单状态更新请求。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
public class WaybillStatusRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;
    private String remark;
}
