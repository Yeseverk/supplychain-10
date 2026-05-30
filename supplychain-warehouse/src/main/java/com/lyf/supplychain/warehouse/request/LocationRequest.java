package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库位保存请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class LocationRequest {

    @NotBlank(message = "区域不能为空")
    private String zone;
    @NotNull(message = "排号不能为空")
    private Integer rowNo;
    @NotNull(message = "列号不能为空")
    private Integer columnNo;
    @NotNull(message = "层号不能为空")
    private Integer floorNo;
    private Integer locationType;
    private BigDecimal maxWeightKg;
    private BigDecimal maxVolumeL;
    private String remark;
}
