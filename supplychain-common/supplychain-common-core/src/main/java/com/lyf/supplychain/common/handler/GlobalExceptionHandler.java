package com.lyf.supplychain.common.handler;

import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局接口异常处理器。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理预期内的业务异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException exception) {
        return R.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理未登录异常。
     *
     * @param exception 未登录异常
     * @return 统一失败响应
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException exception) {
        return R.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理无权限异常。
     *
     * @param exception 无权限异常
     * @return 统一失败响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException exception) {
        return R.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> StrUtil.blankToDefault(error.getDefaultMessage(), error.getField() + "参数不合法"))
                .collect(Collectors.joining("；"));
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理查询参数绑定异常。
     *
     * @param exception 参数绑定异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> StrUtil.blankToDefault(error.getDefaultMessage(), error.getField() + "参数不合法"))
                .collect(Collectors.joining("；"));
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理单参数校验异常。
     *
     * @param exception 单参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), exception.getMessage());
    }

    /**
     * 处理未预期的系统异常。
     *
     * @param exception 系统异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return R.fail(ResultCode.FAIL);
    }
}
