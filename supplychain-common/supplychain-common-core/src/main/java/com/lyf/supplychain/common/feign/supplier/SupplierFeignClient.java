package com.lyf.supplychain.common.feign.supplier;

import com.lyf.supplychain.common.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 供应商服务 Feign 客户端。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@FeignClient(name = "supplychain-supplier", path = "/internal/srm")
public interface SupplierFeignClient {

    /**
     * 查询供应商简要信息。
     *
     * @param supplierId 供应商ID
     * @return 供应商简要信息
     */
    @GetMapping("/suppliers/{supplierId}/brief")
    R<SupplierBriefResponse> getBrief(@PathVariable("supplierId") Long supplierId);
}
