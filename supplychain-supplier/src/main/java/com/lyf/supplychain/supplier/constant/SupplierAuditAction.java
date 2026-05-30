package com.lyf.supplychain.supplier.constant;

import lombok.Getter;

/**
 * 供应商审核动作枚举。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Getter
public enum SupplierAuditAction {

    SUBMIT("提交审核"),
    APPROVE("审核通过"),
    REJECT("审核拒绝"),
    SUPPLEMENT("要求补充"),
    DISABLE("停用供应商"),
    ENABLE("重新启用");

    private final String description;

    SupplierAuditAction(String description) {
        this.description = description;
    }
}
