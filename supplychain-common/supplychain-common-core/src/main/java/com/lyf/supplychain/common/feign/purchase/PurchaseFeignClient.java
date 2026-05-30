package com.lyf.supplychain.common.feign.purchase;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 采购服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@FeignClient(name = "supplychain-purchase", path = "/internal/pms")
public interface PurchaseFeignClient {

    /**
     * 创建采购申请。
     *
     * @param request 采购申请创建请求
     * @return 采购申请ID
     */
    @PostMapping("/requisitions")
    R<Long> createRequisition(@RequestBody PurchaseRequisitionCreateRequest request);

    /**
     * 标记采购订单已结清。
     *
     * @param poId 采购订单ID
     * @return 无数据响应
     */
    @PostMapping("/orders/{poId}/settled")
    R<Void> markSettled(@PathVariable("poId") Long poId);
}
