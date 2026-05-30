package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day03 采购、仓储、财务模块工程布局测试。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
class Day03ProjectLayoutTest {

    @Test
    void rootPomShouldAggregatePurchaseWarehouseAndFinanceModules() throws Exception {
        String pom = Files.readString(projectRoot().resolve("pom.xml"));

        assertThat(pom)
                .contains("<module>supplychain-purchase</module>")
                .contains("<module>supplychain-warehouse</module>")
                .contains("<module>supplychain-finance</module>");
    }

    @Test
    void gatewayRemoteConfigShouldRoutePmsWmsAndFms() throws Exception {
        String gatewayConfig = Files.readString(projectRoot().resolve("nacos-config/extension/supplychain-gateway.yml"));

        assertThat(gatewayConfig)
                .contains("Path=/api/pms/**")
                .contains("Path=/api/wms/**")
                .contains("Path=/api/fms/**")
                .contains("lb://supplychain-purchase")
                .contains("lb://supplychain-warehouse")
                .contains("lb://supplychain-finance");
    }

    @Test
    void commonRemoteConfigShouldContainSeataConfiguration() throws Exception {
        String commonConfig = Files.readString(projectRoot().resolve("nacos-config/common/supplychain-common.yml"));

        assertThat(commonConfig)
                .contains("seata:")
                .contains("tx-service-group: ${SEATA_TX_SERVICE_GROUP:supplychain-tx-group}")
                .contains("vgroup-mapping:")
                .contains("supplychain-tx-group: ${SEATA_CLUSTER:default}");
    }

    @Test
    void newBusinessModulesShouldHaveLocalAndRemoteConfigs() {
        Path root = projectRoot();

        assertThat(root.resolve("supplychain-purchase/src/main/resources/application.yml")).exists();
        assertThat(root.resolve("supplychain-warehouse/src/main/resources/application.yml")).exists();
        assertThat(root.resolve("supplychain-finance/src/main/resources/application.yml")).exists();
        assertThat(root.resolve("nacos-config/extension/supplychain-purchase.yml")).exists();
        assertThat(root.resolve("nacos-config/extension/supplychain-warehouse.yml")).exists();
        assertThat(root.resolve("nacos-config/extension/supplychain-finance.yml")).exists();
    }

    private Path projectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("nacos-config"))) {
            path = path.getParent();
        }
        return path;
    }
}
