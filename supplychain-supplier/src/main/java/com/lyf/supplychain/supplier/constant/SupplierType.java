package com.lyf.supplychain.supplier.constant;

import lombok.Getter;

import java.util.Arrays;

/**
 * 供应商类型枚举。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Getter
public enum SupplierType {

    FACTORY(1, "工厂供应商"),
    TRADER(2, "贸易商"),
    LOGISTICS(3, "物流服务商");

    private final Integer code;

    private final String description;

    SupplierType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断供应商类型是否合法。
     *
     * @param code 类型编码
     * @return true=合法
     */
    public static boolean contains(Integer code) {
        return Arrays.stream(values()).anyMatch(type -> type.getCode().equals(code));
    }

    /**
     * 根据类型编码获取类型名称。
     *
     * @param code 类型编码
     * @return 类型名称
     */
    public static String descriptionOf(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .map(SupplierType::getDescription)
                .orElse("未知");
    }
}
