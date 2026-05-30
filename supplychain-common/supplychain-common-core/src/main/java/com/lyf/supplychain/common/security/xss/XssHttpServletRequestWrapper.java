package com.lyf.supplychain.common.security.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XSS 参数过滤请求包装器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 过滤单个请求参数。
     *
     * @param name 参数名
     * @return 过滤后的参数值
     */
    @Override
    public String getParameter(String name) {
        return clean(super.getParameter(name));
    }

    /**
     * 过滤请求参数数组。
     *
     * @param name 参数名
     * @return 过滤后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values).map(this::clean).toArray(String[]::new);
    }

    /**
     * 过滤请求参数 Map。
     *
     * @return 过滤后的参数 Map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Arrays.stream(entry.getValue())
                        .map(this::clean)
                        .toArray(String[]::new)));
    }

    /**
     * 过滤请求头。
     *
     * @param name 请求头名称
     * @return 过滤后的请求头
     */
    @Override
    public String getHeader(String name) {
        return clean(super.getHeader(name));
    }

    private String clean(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return HtmlUtil.filter(value);
    }
}
