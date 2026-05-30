package com.lyf.supplychain.purchase.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.purchase.entity.PurchaseInquiry;
import com.lyf.supplychain.purchase.request.PurchaseInquiryPageQuery;
import com.lyf.supplychain.purchase.request.PurchaseInquiryRequest;
import com.lyf.supplychain.purchase.request.PurchaseInquiryQuoteRequest;
import com.lyf.supplychain.purchase.service.PurchaseInquiryService;
import com.lyf.supplychain.purchase.vo.PurchaseInquiryCompareVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 采购询价接口控制器。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@RestController
@RequestMapping({"/api/pms/inquiries", "/pms/inquiries"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.PMS_ORDER_MANAGE)
public class PurchaseInquiryController {

    private final PurchaseInquiryService inquiryService;

    public PurchaseInquiryController(PurchaseInquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    /**
     * 创建询价单。
     *
     * @param request 创建请求
     * @return 询价单ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建询价单")
    public R<Long> create(@Valid @RequestBody PurchaseInquiryRequest request) {
        return R.ok(inquiryService.create(request));
    }

    /**
     * 分页查询询价单。
     *
     * @param query 分页参数
     * @return 询价单分页结果
     */
    @GetMapping({"", "/page"})
    public R<PageResult<PurchaseInquiry>> pageInquiries(PurchaseInquiryPageQuery query) {
        return R.ok(inquiryService.pageInquiries(query));
    }

    /**
     * 对比采购申请下的供应商报价。
     *
     * @param reqId 采购申请ID
     * @return 询价比价结果
     */
    @GetMapping("/compare")
    public R<List<PurchaseInquiryCompareVO>> compare(@RequestParam("reqId") Long reqId) {
        return R.ok(inquiryService.compare(reqId));
    }

    /**
     * 供应商提交报价。
     *
     * @param id      询价单ID
     * @param request 报价请求
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/quote")
    @TenantWriteGuard(scene = "供应商提交报价")
    public R<Void> quote(@PathVariable("id") Long id, @Valid @RequestBody PurchaseInquiryQuoteRequest request) {
        inquiryService.quote(id, request);
        return R.ok();
    }

    /**
     * 选中供应商报价。
     *
     * @param id 询价单ID
     * @return 无数据响应
     */
    @PutMapping("/{id:\\d+}/select")
    @TenantWriteGuard(scene = "选中供应商报价")
    public R<Void> select(@PathVariable("id") Long id) {
        inquiryService.select(id);
        return R.ok();
    }
}
