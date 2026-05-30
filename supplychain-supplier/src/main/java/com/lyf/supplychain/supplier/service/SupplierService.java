package com.lyf.supplychain.supplier.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.supplier.entity.Supplier;
import com.lyf.supplychain.supplier.request.SupplierAuditRequest;
import com.lyf.supplychain.supplier.request.SupplierCertUploadRequest;
import com.lyf.supplychain.supplier.request.SupplierCreateRequest;
import com.lyf.supplychain.supplier.request.SupplierPageQuery;
import com.lyf.supplychain.supplier.request.SupplierUpdateRequest;
import com.lyf.supplychain.supplier.vo.SupplierDetailVO;
import com.lyf.supplychain.supplier.vo.SupplierListVO;
import com.lyf.supplychain.supplier.vo.SupplierCertUploadVO;

/**
 * 供应商信息管理服务接口。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * 分页查询供应商列表。
     *
     * @param query 查询参数
     * @return 分页供应商列表
     */
    PageResult<SupplierListVO> pageSuppliers(SupplierPageQuery query);

    /**
     * 查询供应商详情。
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    SupplierDetailVO detail(Long id);

    /**
     * 新增供应商。
     *
     * @param request 新增请求
     * @return 供应商ID
     */
    Long create(SupplierCreateRequest request);

    /**
     * 编辑供应商。
     *
     * @param id      供应商ID
     * @param request 编辑请求
     */
    void update(Long id, SupplierUpdateRequest request);

    /**
     * 上传供应商资质文件。
     *
     * @param id 供应商ID
     * @param request 上传请求
     * @return 上传结果
     */
    SupplierCertUploadVO uploadCert(Long id, SupplierCertUploadRequest request);

    /**
     * 提交供应商审核。
     *
     * @param id 供应商ID
     */
    void submitAudit(Long id);

    /**
     * 审核通过供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    void approve(Long id, SupplierAuditRequest request);

    /**
     * 审核拒绝供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    void reject(Long id, SupplierAuditRequest request);

    /**
     * 要求供应商补充资料。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    void requestSupplement(Long id, SupplierAuditRequest request);

    /**
     * 停用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    void disable(Long id, SupplierAuditRequest request);

    /**
     * 重新启用供应商。
     *
     * @param id      供应商ID
     * @param request 审核请求
     */
    void enable(Long id, SupplierAuditRequest request);

    /**
     * 删除供应商。
     *
     * @param id 供应商ID
     */
    void delete(Long id);
}
