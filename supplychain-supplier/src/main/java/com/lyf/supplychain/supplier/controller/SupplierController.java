package com.lyf.supplychain.supplier.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.annotation.PlanLimit;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.supplier.request.SupplierAuditRequest;
import com.lyf.supplychain.supplier.request.SupplierCertUploadRequest;
import com.lyf.supplychain.supplier.request.SupplierCreateRequest;
import com.lyf.supplychain.supplier.request.SupplierPageQuery;
import com.lyf.supplychain.supplier.request.SupplierUpdateRequest;
import com.lyf.supplychain.supplier.service.SupplierService;
import com.lyf.supplychain.supplier.vo.SupplierDetailVO;
import com.lyf.supplychain.supplier.vo.SupplierListVO;
import com.lyf.supplychain.supplier.vo.SupplierCertUploadVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * 供应商信息管理控制器。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@RestController
@RequestMapping({"/api/srm/suppliers", "/srm/suppliers"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.SRM_SUPPLIER_LIST)
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * 分页查询供应商列表。
     *
     * @param query 查询参数
     * @return 分页供应商列表
     */
    @GetMapping
    public R<PageResult<SupplierListVO>> pageSuppliers(SupplierPageQuery query) {
        return R.ok(supplierService.pageSuppliers(query));
    }

    /**
     * 查询供应商详情。
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    @GetMapping("/{id}")
    public R<SupplierDetailVO> detail(@PathVariable("id") Long id) {
        return R.ok(supplierService.detail(id));
    }

    /**
     * 新增供应商。
     *
     * @param request 新增请求
     * @return 供应商ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "新增供应商")
    @PlanLimit(feature = "supplier.max", bizName = "供应商")
    @OperationLog(module = "供应商管理", action = "新增供应商", type = OperationLog.Type.INSERT)
    public R<Long> create(@Valid @RequestBody SupplierCreateRequest request) {
        return R.ok(supplierService.create(request));
    }

    /**
     * 编辑供应商。
     *
     * @param id      供应商ID
     * @param request 编辑请求
     * @return 无数据响应
     */
    @PutMapping("/{id}")
    @TenantWriteGuard(scene = "编辑供应商")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        supplierService.update(id, request);
        return R.ok();
    }

    /**
     * 上传供应商资质文件。
     *
     * @param id 供应商ID
     * @param file 资质文件
     * @param certType 资质类型
     * @param certName 资质名称
     * @param issueDate 颁发日期
     * @param expireDate 到期日期
     * @param certNo 证书编号
     * @param remark 备注
     * @return 上传结果
     */
    @PostMapping("/{id}/certs")
    @TenantWriteGuard(scene = "上传供应商资质")
    public R<SupplierCertUploadVO> uploadCert(@PathVariable("id") Long id,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam("certType") Integer certType,
                                              @RequestParam("certName") String certName,
                                              @RequestParam(value = "issueDate", required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issueDate,
                                              @RequestParam(value = "expireDate", required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate expireDate,
                                              @RequestParam(value = "certNo", required = false) String certNo,
                                              @RequestParam(value = "remark", required = false) String remark) {
        SupplierCertUploadRequest request = new SupplierCertUploadRequest();
        request.setFile(file);
        request.setCertType(certType);
        request.setCertName(certName);
        request.setIssueDate(issueDate);
        request.setExpireDate(expireDate);
        request.setCertNo(certNo);
        request.setRemark(remark);
        return R.ok(supplierService.uploadCert(id, request));
    }

    /**
     * 提交供应商审核。
     * 从草稿 --> 待审核
     * @param id 供应商ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/submit")
    @TenantWriteGuard(scene = "提交供应商审核")
    @OperationLog(module = "供应商管理", action = "提交供应商审核", type = OperationLog.Type.UPDATE)
    public R<Void> submitAudit(@PathVariable("id") Long id) {
        supplierService.submitAudit(id);
        return R.ok();
    }

    /**
     * 审核通过供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/approve")
    @TenantWriteGuard(scene = "审核通过供应商")
    @OperationLog(module = "供应商管理", action = "审核通过供应商", type = OperationLog.Type.UPDATE)
    public R<Void> approve(@PathVariable("id") Long id, @RequestBody SupplierAuditRequest request) {
        supplierService.approve(id, request);
        return R.ok();
    }

    /**
     * 审核拒绝供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/reject")
    @TenantWriteGuard(scene = "审核拒绝供应商")
    @OperationLog(module = "供应商管理", action = "审核拒绝供应商", type = OperationLog.Type.UPDATE)
    public R<Void> reject(@PathVariable("id") Long id, @RequestBody SupplierAuditRequest request) {
        supplierService.reject(id, request);
        return R.ok();
    }

    /**
     * 要求供应商补充资料。
     *
     * @param id      供应商ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/supplement")
    @TenantWriteGuard(scene = "要求供应商补充资料")
    @OperationLog(module = "供应商管理", action = "要求供应商补充资料", type = OperationLog.Type.UPDATE)
    public R<Void> requestSupplement(@PathVariable("id") Long id, @RequestBody SupplierAuditRequest request) {
        supplierService.requestSupplement(id, request);
        return R.ok();
    }

    /**
     * 停用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/disable")
    @TenantWriteGuard(scene = "停用供应商")
    @OperationLog(module = "供应商管理", action = "停用供应商", type = OperationLog.Type.UPDATE)
    public R<Void> disable(@PathVariable("id") Long id, @RequestBody SupplierAuditRequest request) {
        supplierService.disable(id, request);
        return R.ok();
    }

    /**
     * 重新启用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/enable")
    @TenantWriteGuard(scene = "重新启用供应商")
    @OperationLog(module = "供应商管理", action = "重新启用供应商", type = OperationLog.Type.UPDATE)
    public R<Void> enable(@PathVariable("id") Long id, @RequestBody SupplierAuditRequest request) {
        supplierService.enable(id, request);
        return R.ok();
    }

    /**
     * 删除供应商。
     *
     * @param id 供应商ID
     * @return 无数据响应
     */
    @DeleteMapping("/{id}")
    @TenantWriteGuard(scene = "删除供应商")
    @OperationLog(module = "供应商管理", action = "删除供应商", type = OperationLog.Type.DELETE)
    public R<Void> delete(@PathVariable("id") Long id) {
        supplierService.delete(id);
        return R.ok();
    }
}
