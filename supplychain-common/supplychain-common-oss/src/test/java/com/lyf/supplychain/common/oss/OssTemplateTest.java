package com.lyf.supplychain.common.oss;

import com.aliyun.oss.OSS;
import com.lyf.supplychain.common.oss.properties.OssProperties;
import com.lyf.supplychain.common.oss.template.OssTemplate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * OSS 操作模板测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class OssTemplateTest {

    @Test
    void shouldBuildObjectNameWithPrefix() {
        OssProperties properties = new OssProperties();
        properties.setObjectPrefix("srm/certs/");
        OssTemplate template = new OssTemplate(mock(OSS.class), properties);

        String objectName = template.buildObjectName("/license.pdf");

        assertThat(objectName).isEqualTo("srm/certs/license.pdf");
    }

    @Test
    void shouldBuildPublicUrlWithDomain() {
        OssProperties properties = new OssProperties();
        properties.setDomain("https://static.example.com/");
        OssTemplate template = new OssTemplate(mock(OSS.class), properties);

        String url = template.buildPublicUrl("srm/certs/license.pdf");

        assertThat(url).isEqualTo("https://static.example.com/srm/certs/license.pdf");
    }

    @Test
    void shouldBuildPublicUrlWithBucketEndpointWhenDomainMissing() {
        OssProperties properties = new OssProperties();
        properties.setBucketName("supplychain-dev");
        properties.setEndpoint("https://oss-cn-shenzhen.aliyuncs.com");
        OssTemplate template = new OssTemplate(mock(OSS.class), properties);

        String url = template.buildPublicUrl("srm/certs/license.pdf");

        assertThat(url).isEqualTo("https://supplychain-dev.oss-cn-shenzhen.aliyuncs.com/srm/certs/license.pdf");
    }

    @Test
    void shouldRenameFileWithOriginalExtension() {
        OssTemplate template = new OssTemplate(mock(OSS.class), new OssProperties());

        String renamedFileName = template.renameFile("营业执照.PDF");

        assertThat(renamedFileName)
                .endsWith(".pdf")
                .matches("[0-9a-f]{32}\\.pdf");
    }

    @Test
    void shouldBuildDirectoryObjectNameByTenantBizTypeAndDate() {
        OssProperties properties = new OssProperties();
        OssTemplate template = new OssTemplate(mock(OSS.class), properties);

        String objectName = template.buildDirectoryObjectName(101L, "supplier_cert", "license.png", LocalDate.of(2026, 5, 16));

        assertThat(objectName)
                .startsWith("101/supplier_cert/2026-05-16/")
                .endsWith(".png")
                .matches("101/supplier_cert/2026-05-16/[0-9a-f]{32}\\.png");
    }

    @Test
    void shouldBuildDirectoryObjectNameWithGlobalPrefix() {
        OssProperties properties = new OssProperties();
        properties.setObjectPrefix("dev");
        OssTemplate template = new OssTemplate(mock(OSS.class), properties);

        String objectName = template.buildObjectName(template.buildDirectoryObjectName(101L, "supplier_cert", "license.jpg", LocalDate.of(2026, 5, 16)));

        assertThat(objectName)
                .startsWith("dev/101/supplier_cert/2026-05-16/")
                .endsWith(".jpg");
    }
}
