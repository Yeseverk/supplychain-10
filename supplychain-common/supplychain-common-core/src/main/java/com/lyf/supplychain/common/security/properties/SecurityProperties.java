package com.lyf.supplychain.common.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 公共安全配置属性。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Data
@ConfigurationProperties(prefix = "supplychain.security")
public class SecurityProperties {

    private boolean xssEnabled = true;
}
