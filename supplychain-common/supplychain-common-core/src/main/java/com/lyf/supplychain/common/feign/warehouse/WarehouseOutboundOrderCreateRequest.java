package com.lyf.supplychain.common.feign.warehouse;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * WMS 出库单创建请求。
 *
 * @author liyunfei
 * @date 2026-05-30
 */
@Data
public class WarehouseOutboundOrderCreateRequest {

    private Long tenantId;

    private Integer outboundType;

    private Long warehouseId;

    private String warehouseName;

    private String refType;

    private Long refId;

    private String refNo;

    private LocalDate planDate;

    private String remark;

    private List<WarehouseStockItem> items;
}
