package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量库位创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class LocationBatchRequest {

    @NotBlank(message = "区域不能为空")
    private String zone;
    @NotNull(message = "起始排不能为空")
    private Integer rowStart;
    @NotNull(message = "结束排不能为空")
    private Integer rowEnd;
    @NotNull(message = "起始列不能为空")
    private Integer columnStart;
    @NotNull(message = "结束列不能为空")
    private Integer columnEnd;
    @NotNull(message = "起始层不能为空")
    private Integer floorStart;
    @NotNull(message = "结束层不能为空")
    private Integer floorEnd;
    private Integer locationType;
}
