package com.lyf.supplychain.system.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 系统模块配置文件测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class SystemConfigFileTest {

    @Test
    void remoteConfigShouldProvideUnifiedNotificationChannelConfig() {
        Properties properties = loadYaml(projectRoot().resolve("nacos-config/extension/supplychain-system.yml"));

        assertThat(properties.getProperty("supplychain.notification.mail-enabled"))
                .isEqualTo("${SUPPLYCHAIN_MAIL_ENABLED:false}");
        assertThat(properties.getProperty("supplychain.notification.websocket-enabled"))
                .isEqualTo("${SUPPLYCHAIN_WEBSOCKET_ENABLED:true}");
        assertThat(properties.getProperty("supplychain.notification.default-from"))
                .isEqualTo("${MAIL_FROM:no-reply@supplychain.local}");
        assertThat(properties.getProperty("spring.mail.host")).isEqualTo("${MAIL_HOST:}");
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
