package com.lyf.supplychain.supplier.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.supplier.SupplierBriefResponse;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.service.SupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 供应商内部调用接口。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@RestController
@RequestMapping("/internal/srm/suppliers")
public class SupplierInternalController {

    private final SupplierService supplierService;

    public SupplierInternalController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * 查询供应商简要信息。
     *
     * @param supplierId 供应商ID
     * @return 供应商简要信息
     */
    @GetMapping("/{supplierId}/brief")
    public R<SupplierBriefResponse> brief(@PathVariable("supplierId") Long supplierId) {
        Supplier supplier = supplierService.getById(supplierId);
        if (supplier == null) {
            return R.ok(null);
        }
        SupplierBriefResponse response = new SupplierBriefResponse();
        response.setId(supplier.getId());
        response.setSupplierCode(supplier.getSupplierCode());
        response.setSupplierName(supplier.getSupplierName());
        response.setGrade(supplier.getGrade());
        response.setStatus(supplier.getStatus());
        return R.ok(response);
    }
}
