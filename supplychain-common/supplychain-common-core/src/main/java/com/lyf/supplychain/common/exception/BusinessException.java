package com.lyf.supplychain.common.exception;

import com.lyf.supplychain.common.constant.ResultCode;
import lombok.Getter;

/**
 * 携带自定义响应码的业务异常。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 抛出业务异常。
     *
     * @param resultCode 响应码
     */
    public static void throwException(ResultCode resultCode) {
        throw new BusinessException(resultCode);
    }

    /**
     * 抛出业务异常。
     *
     * @param message 错误消息
     */
    public static void throwException(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常。
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public static void throwException(Integer code, String message) {
        throw new BusinessException(code, message);
    }
}
