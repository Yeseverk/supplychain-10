package com.lyf.supplychain.common.oss.autoconfigure;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.lyf.supplychain.common.oss.properties.OssProperties;
import com.lyf.supplychain.common.oss.template.OssTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 自动装配。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
@AutoConfiguration
@ConditionalOnClass(OSS.class)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "supplychain.oss", name = "enabled", havingValue = "true")
public class OssAutoConfiguration {

    /**
     * 创建 OSS 客户端。
     *
     * @param properties OSS 配置属性
     * @return OSS 客户端
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public OSS ossClient(OssProperties properties) {
        validate(properties);
        DefaultCredentialProvider credentialProvider = buildCredentialProvider(properties);
        ClientBuilderConfiguration configuration = buildClientConfiguration(properties);
        return OSSClientBuilder.create()
                .endpoint(properties.getEndpoint())
                .credentialsProvider(credentialProvider)
                .clientConfiguration(configuration)
                .region(properties.getRegion())
                .build();
    }

    /**
     * 创建 OSS 操作模板。
     *
     * @param ossClient OSS 客户端
     * @param properties OSS 配置属性
     * @return OSS 操作模板
     */
    @Bean
    @ConditionalOnMissingBean
    public OssTemplate ossTemplate(OSS ossClient, OssProperties properties) {
        return new OssTemplate(ossClient, properties);
    }

    private DefaultCredentialProvider buildCredentialProvider(OssProperties properties) {
        if (StringUtils.hasText(properties.getSecurityToken())) {
            return new DefaultCredentialProvider(
                    properties.getAccessKeyId(),
                    properties.getAccessKeySecret(),
                    properties.getSecurityToken()
            );
        }
        return new DefaultCredentialProvider(properties.getAccessKeyId(), properties.getAccessKeySecret());
    }

    private ClientBuilderConfiguration buildClientConfiguration(OssProperties properties) {
        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setMaxConnections(properties.getMaxConnections());
        configuration.setConnectionTimeout(properties.getConnectionTimeout());
        configuration.setSocketTimeout(properties.getSocketTimeout());
        configuration.setMaxErrorRetry(properties.getMaxErrorRetry());
        configuration.setSignatureVersion(SignVersion.valueOf(properties.getSignatureVersion()));
        return configuration;
    }

    private void validate(OssProperties properties) {
        Assert.hasText(properties.getEndpoint(), "OSS endpoint不能为空");
        Assert.hasText(properties.getRegion(), "OSS region不能为空");
        Assert.hasText(properties.getBucketName(), "OSS bucketName不能为空");
        Assert.hasText(properties.getAccessKeyId(), "OSS accessKeyId不能为空");
        Assert.hasText(properties.getAccessKeySecret(), "OSS accessKeySecret不能为空");
    }
}
