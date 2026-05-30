package com.lyf.supplychain.common.oss;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OSS 远程配置文件测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class OssConfigFileTest {

    @Test
    void remoteConfigShouldContainOssVariables() throws Exception {
        String yaml = Files.readString(projectRoot().resolve("nacos-config/common/supplychain-oss.yml"));

        assertThat(yaml)
                .contains("access-key-id")
                .contains("access-key-secret")
                .contains("endpoint")
                .contains("region")
                .contains("bucket-name");
    }

    private Path projectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("nacos-config"))) {
            path = path.getParent();
        }
        return path;
    }
}
