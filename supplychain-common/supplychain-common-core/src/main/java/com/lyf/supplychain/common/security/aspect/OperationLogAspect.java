package com.lyf.supplychain.common.security.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.feign.system.SystemAuditLogFeignClient;
import com.lyf.supplychain.common.feign.system.SystemAuditLogRecordRequest;
import com.lyf.supplychain.common.security.annotation.OperationLog;
import com.lyf.supplychain.common.security.context.SecurityContextHolder;
import com.lyf.supplychain.common.security.model.LoginUser;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 操作审计日志切面。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Aspect
@Component
public class OperationLogAspect {

    private final SystemAuditLogFeignClient auditLogFeignClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;

    @Autowired
    public OperationLogAspect(SystemAuditLogFeignClient auditLogFeignClient, ObjectMapper objectMapper) {
        this(auditLogFeignClient, objectMapper, Executors.newSingleThreadExecutor());
    }

    OperationLogAspect(SystemAuditLogFeignClient auditLogFeignClient, ObjectMapper objectMapper, Executor executor) {
        this.auditLogFeignClient = auditLogFeignClient;
        this.objectMapper = objectMapper;
        this.executor = executor;
    }

    /**
     * 记录标注了操作日志注解的业务方法。
     *
     * @param joinPoint 切点
     * @return 业务方法返回值
     * @throws Throwable 业务异常
     */
    @Around("@annotation(com.lyf.supplychain.common.security.annotation.OperationLog)")
    public Object record(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog operationLog = resolveAnnotation(joinPoint);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            submitAuditLog(joinPoint, operationLog, result, null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable throwable) {
            submitAuditLog(joinPoint, operationLog, null, throwable, System.currentTimeMillis() - start);
            throw throwable;
        }
    }

    /**
     * 关闭默认审计日志线程池。
     */
    @PreDestroy
    public void destroy() {
        if (executor instanceof ExecutorService executorService) {
            executorService.shutdown();
        }
    }

    private OperationLog resolveAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return AnnotationUtils.findAnnotation(method, OperationLog.class);
    }

    private void submitAuditLog(ProceedingJoinPoint joinPoint,
                                OperationLog operationLog,
                                Object result,
                                Throwable throwable,
                                long durationMs) {
        if (operationLog == null) {
            return;
        }
        SystemAuditLogRecordRequest request = buildAuditLog(joinPoint, operationLog, result, throwable, durationMs);
        executor.execute(() -> {
            try {
                auditLogFeignClient.record(request);
            } catch (Exception ignored) {
                // 审计日志写入失败不能影响主业务，失败场景由应用日志和后续补偿排查
            }
        });
    }

    private SystemAuditLogRecordRequest buildAuditLog(ProceedingJoinPoint joinPoint,
                                                      OperationLog operationLog,
                                                      Object result,
                                                      Throwable throwable,
                                                      long durationMs) {
        HttpServletRequest servletRequest = currentRequest();
        LoginUser loginUser = SecurityContextHolder.getLoginUser();
        SystemAuditLogRecordRequest request = new SystemAuditLogRecordRequest();
        request.setTenantId(loginUser == null || loginUser.getTenantId() == null ? 0L : loginUser.getTenantId());
        request.setUserId(loginUser == null || loginUser.getUserId() == null ? 0L : loginUser.getUserId());
        request.setUsername(loginUser == null || loginUser.getUsername() == null ? "SYSTEM" : loginUser.getUsername());
        request.setModule(operationLog.module());
        request.setAction(operationLog.action());
        request.setMethod(requestMethod(servletRequest, joinPoint));
        request.setRequestParams(operationLog.saveParam() ? requestParams(joinPoint, operationLog.sensitiveFields()) : null);
        request.setResponseCode(responseCode(result));
        request.setIpAddress(clientIp(servletRequest));
        request.setUserAgent(servletRequest == null ? null : servletRequest.getHeader("User-Agent"));
        request.setDurationMs(Math.toIntExact(Math.min(durationMs, Integer.MAX_VALUE)));
        request.setStatus(throwable == null ? 1 : 0);
        request.setErrorMsg(throwable == null ? null : truncate(throwable.getMessage(), 512));
        request.setOperateTime(LocalDateTime.now());
        return request;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String requestMethod(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        if (request == null) {
            return joinPoint.getSignature().toShortString();
        }
        return request.getMethod() + " " + request.getRequestURI();
    }

    private String requestParams(ProceedingJoinPoint joinPoint, String[] sensitiveFields) {
        Map<String, Object> params = new LinkedHashMap<>();
        String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] values = joinPoint.getArgs();
        for (int i = 0; i < values.length; i++) {
            String name = names == null || i >= names.length ? "arg" + i : names[i];
            params.put(name, safeValue(name, values[i], sensitiveFields));
        }
        try {
            return truncate(objectMapper.writeValueAsString(params), 4000);
        } catch (Exception exception) {
            return "{}";
        }
    }

    private Object safeValue(String name, Object value, String[] sensitiveFields) {
        if (value == null) {
            return null;
        }
        if (isSensitive(name, sensitiveFields)) {
            return "******";
        }
        if (isSyntheticParameterName(name) && value instanceof CharSequence) {
            return "******";
        }
        if (value instanceof MultipartFile file) {
            return Map.of("filename", file.getOriginalFilename(), "size", file.getSize());
        }
        if (value instanceof HttpServletRequest) {
            return "[HttpServletRequest]";
        }
        return value;
    }

    private boolean isSensitive(String name, String[] sensitiveFields) {
        return Arrays.stream(sensitiveFields)
                .anyMatch(field -> name != null && name.toLowerCase().contains(field.toLowerCase()));
    }

    private boolean isSyntheticParameterName(String name) {
        return name != null && name.matches("arg\\d+");
    }

    private Integer responseCode(Object result) {
        if (result instanceof R<?> response) {
            return response.getCode();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
