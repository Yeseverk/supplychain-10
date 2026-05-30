package com.lyf.supplychain.common.security.xss;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XSS 请求包装器单元测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class XssFilterTest {

    @Test
    void wrapperShouldFilterScriptFromParameter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("name", "<script>alert(1)</script>供应商");

        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request);

        assertThat(wrapper.getParameter("name")).doesNotContain("<script>").contains("供应商");
    }
}
