package com.lyf.supplychain.supplier.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 供应商模块配置文件测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class SupplierConfigFileTest {

    @Test
    void localConfigShouldImportCommonAndSupplierRemoteConfig() {
        Properties properties = loadYaml(projectRoot().resolve("supplychain-supplier/src/main/resources/application.yml"));

        assertThat(properties.getProperty("spring.application.name")).isEqualTo("supplychain-supplier");
        assertThat(properties.getProperty("spring.config.import[0]"))
                .isEqualTo("optional:nacos:supplychain-common.yml?group=SUPPLYCHAIN_GROUP");
        assertThat(properties.getProperty("spring.config.import[1]"))
                .isEqualTo("optional:nacos:supplychain-oss.yml?group=SUPPLYCHAIN_GROUP");
        assertThat(properties.getProperty("spring.config.import[2]"))
                .isEqualTo("optional:nacos:supplychain-supplier.yml?group=SUPPLYCHAIN_GROUP");
    }

    @Test
    void remoteConfigShouldProvideSupplierServicePort() {
        Properties properties = loadYaml(projectRoot().resolve("nacos-config/extension/supplychain-supplier.yml"));

        assertThat(properties.getProperty("server.port")).isEqualTo("9202");
        assertThat(properties.getProperty("supplychain.supplier.cert-expire-warning-days")).isEqualTo("30");
        assertThat(properties).doesNotContainKey("supplychain.notification.mail-enabled");
        assertThat(properties).doesNotContainKey("supplychain.notification.websocket-enabled");
        assertThat(properties).doesNotContainKey("spring.mail.host");
        assertThat(properties.getProperty("xxl.job.executor.appname")).isEqualTo("supplychain-supply");
    }

    private Properties loadYaml(Path path) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new FileSystemResource(path));
        Properties properties = factoryBean.getObject();
        return properties == null ? new Properties() : properties;
    }

    private Path projectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("nacos-config"))) {
            path = path.getParent();
        }
        return path;
    }
}
