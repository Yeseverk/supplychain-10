package com.lyf.supplychain.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 调拨单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Data
public class TransferOrderRequest {

    @NotNull(message = "调出仓库不能为空")
    private Long fromWarehouseId;
    private String fromWarehouseName;
    @NotNull(message = "调入仓库不能为空")
    private Long toWarehouseId;
    private String toWarehouseName;
    private String transferReason;
    private String logisticsCompany;
    private String trackingNo;
    private LocalDate planDate;
    private String remark;
    @Valid
    @NotEmpty(message = "调拨明细不能为空")
    private List<WmsItemRequest> items;
}
