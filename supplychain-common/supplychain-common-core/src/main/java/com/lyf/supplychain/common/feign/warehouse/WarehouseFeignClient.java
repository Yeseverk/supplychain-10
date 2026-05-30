package com.lyf.supplychain.common.feign.warehouse;

import com.lyf.supplychain.common.api.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 仓储服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@FeignClient(name = "supplychain-warehouse", path = "/internal/wms")
public interface WarehouseFeignClient {

    /**
     * 采购入库增加库存。
     *
     * @param request 入库请求
     * @return 无数据响应
     */
    @PostMapping("/inventory/inbound")
    R<Void> inbound(@RequestBody WarehouseInboundRequest request);

    /**
     * 采购退货减少库存。
     *
     * @param request 出库请求
     * @return 无数据响应
     */
    @PostMapping("/inventory/outbound")
    R<Void> outbound(@RequestBody WarehouseOutboundRequest request);

    /**
     * 创建 WMS 出库单。
     *
     * @param request 出库单创建请求
     * @return 出库单ID
     */
    @PostMapping("/outbound/orders")
    R<Long> createOutboundOrder(@RequestBody WarehouseOutboundOrderCreateRequest request);

    /**
     * 查询库存预警列表。
     *
     * @return 库存预警列表
     */
    @GetMapping("/inventory/warnings")
    R<List<WarehouseInventoryWarningResponse>> warnings();
}
