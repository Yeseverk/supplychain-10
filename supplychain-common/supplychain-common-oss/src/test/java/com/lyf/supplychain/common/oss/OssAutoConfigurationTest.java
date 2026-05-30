package com.lyf.supplychain.common.oss;

import com.aliyun.oss.OSS;
import com.lyf.supplychain.common.oss.autoconfigure.OssAutoConfiguration;
import com.lyf.supplychain.common.oss.properties.OssProperties;
import com.lyf.supplychain.common.oss.template.OssTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OSS 自动装配测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class OssAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OssAutoConfiguration.class));

    @Test
    void shouldNotCreateBeansWhenOssDisabled() {
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(OSS.class)
                .doesNotHaveBean(OssTemplate.class));
    }

    @Test
    void shouldCreateOssClientAndTemplateWhenOssEnabled() {
        contextRunner
                .withPropertyValues(
                        "supplychain.oss.enabled=true",
                        "supplychain.oss.endpoint=https://oss-cn-shenzhen.aliyuncs.com",
                        "supplychain.oss.region=cn-shenzhen",
                        "supplychain.oss.bucket-name=supplychain-dev",
                        "supplychain.oss.access-key-id=test-ak",
                        "supplychain.oss.access-key-secret=test-sk",
                        "supplychain.oss.object-prefix=dev",
                        "supplychain.oss.domain=https://static.example.com"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OSS.class);
                    assertThat(context).hasSingleBean(OssTemplate.class);
                    OssProperties properties = context.getBean(OssProperties.class);
                    assertThat(properties.getRegion()).isEqualTo("cn-shenzhen");
                    assertThat(properties.getBucketName()).isEqualTo("supplychain-dev");
                });
    }
}
