package com.lyf.supplychain.supplier.constant;

import lombok.Getter;

/**
 * 供应商模块业务错误码。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Getter
public enum SupplierErrorCode {

    SUPPLIER_NOT_FOUND(11001, "供应商不存在"),
    SUPPLIER_STATUS_NOT_ALLOWED(11002, "供应商状态不允许此操作"),
    SUPPLIER_NAME_EXISTS(11003, "供应商名称已存在"),
    SUPPLIER_EMAIL_EXISTS(11004, "该邮箱已被使用"),
    SUPPLIER_PURCHASE_ORDER_EXISTS(11005, "该供应商有未完成的采购单，不能删除"),
    SUPPLIER_AUDIT_REMARK_REQUIRED(11006, "审核意见不能为空"),
    SUPPLIER_SELF_AUDIT_NOT_ALLOWED(11007, "提交人不能审核自己的供应商"),
    SUPPLIER_BUSINESS_LICENSE_REQUIRED(11008, "必须上传营业执照才能提交审核"),
    SUPPLIER_CERT_FILE_TYPE_NOT_ALLOWED(11009, "仅支持JPG、JPEG、PNG、PDF格式文件"),
    SUPPLIER_CERT_FILE_CONTENT_MISMATCH(11010, "文件内容与MIME类型不符"),
    SUPPLIER_OSS_NOT_CONFIGURED(11011, "OSS文件存储未启用");

    private final Integer code;

    private final String message;

    SupplierErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
