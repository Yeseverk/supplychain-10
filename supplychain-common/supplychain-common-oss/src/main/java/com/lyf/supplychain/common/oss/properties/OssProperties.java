package com.lyf.supplychain.common.oss.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置属性。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@Data
@ConfigurationProperties(prefix = "supplychain.oss")
public class OssProperties {

    private Boolean enabled = false;

    private String endpoint;

    private String region;

    private String bucketName;

    private String accessKeyId;

    private String accessKeySecret;

    private String securityToken;

    private String objectPrefix;

    private String domain;

    private String signatureVersion = "V4";

    private Integer maxConnections = 200;

    private Integer connectionTimeout = 10000;

    private Integer socketTimeout = 10000;

    private Integer maxErrorRetry = 3;
}
