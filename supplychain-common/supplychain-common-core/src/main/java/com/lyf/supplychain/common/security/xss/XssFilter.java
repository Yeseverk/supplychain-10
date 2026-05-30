package com.lyf.supplychain.common.security.xss;

import com.lyf.supplychain.common.security.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * XSS 全局过滤器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class XssFilter extends OncePerRequestFilter {

    private final SecurityProperties properties;

    public XssFilter(SecurityProperties properties) {
        this.properties = properties;
    }

    /**
     * 包装普通请求并过滤参数中的危险 HTML。
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isXssEnabled() || isMultipart(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
}
