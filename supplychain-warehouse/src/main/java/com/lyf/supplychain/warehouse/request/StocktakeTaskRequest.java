package com.lyf.supplychain.warehouse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 盘点任务创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class StocktakeTaskRequest {

    @NotNull(message = "盘点类型不能为空")
    private Integer taskType;
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @NotNull(message = "计划盘点日期不能为空")
    private LocalDate planDate;
    private String remark;
}
