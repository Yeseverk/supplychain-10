package com.lyf.supplychain.common.api;

import com.lyf.supplychain.common.constant.ResultCode;
import lombok.Data;

/**
 * 统一接口响应体。
 *
 * @param <T> 响应数据类型
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
public class R<T> {

    private Integer code;

    private String msg;

    private T data;

    private Long timestamp;

    private String traceId;

    /**
     * 构建无数据的成功响应。
     *
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建带数据的成功响应。
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构建失败响应。
     *
     * @param msg 错误消息
     * @param <T> 响应数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String msg) {
        return build(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 构建失败响应。
     *
     * @param resultCode 响应码
     * @param <T>        响应数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 构建失败响应。
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  响应数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(Integer code, String msg) {
        return build(code, msg, null);
    }

    /**
     * 构建统一响应。
     *
     * @param code 响应码
     * @param msg  响应消息
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 统一响应
     */
    public static <T> R<T> build(Integer code, String msg, T data) {
        R<T> response = new R<>();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
