package com.lyf.supplychain.supplier.constant;

import lombok.Getter;

import java.util.Arrays;

/**
 * 供应商状态枚举。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Getter
public enum SupplierStatus {

    DRAFT(0, "草稿"),
    PENDING_AUDIT(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    DISABLED(4, "已停用");

    private final Integer code;

    private final String description;

    SupplierStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取状态名称。
     *
     * @param code 状态码
     * @return 状态名称
     */
    public static String descriptionOf(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.getCode().equals(code))
                .findFirst()
                .map(SupplierStatus::getDescription)
                .orElse("未知");
    }
}
