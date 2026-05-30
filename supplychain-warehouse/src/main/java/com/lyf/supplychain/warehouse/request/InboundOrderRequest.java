package com.lyf.supplychain.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 入库单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class InboundOrderRequest {

    @NotNull(message = "入库类型不能为空")
    private Integer inboundType;
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;
    private String refType;
    private Long refId;
    private String refNo;
    private LocalDate expectedDate;
    private String remark;
    @Valid
    @NotEmpty(message = "入库明细不能为空")
    private List<WmsItemRequest> items;
}
