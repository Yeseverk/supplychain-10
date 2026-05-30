package com.lyf.supplychain.common.constant;

import lombok.Getter;

/**
 * 通用接口响应码。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    FAIL(500, "系统内部错误"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权限访问"),
    DATA_NOT_FOUND(404, "数据不存在"),
    DATA_VERSION_CONFLICT(409, "数据已被他人修改，请刷新后重试");

    private final Integer code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
