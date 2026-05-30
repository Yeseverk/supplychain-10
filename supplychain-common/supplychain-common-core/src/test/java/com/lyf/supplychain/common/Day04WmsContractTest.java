package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Day04 WMS 完整实现合同测试。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
class Day04WmsContractTest {

    private final Path projectRoot = findProjectRoot();

    @Test
    void shouldContainDay04WmsSchemaTables() throws Exception {
        String sql = Files.readString(projectRoot.resolve("sql/09_wms_day04_schema.sql"));

        assertThat(sql).contains(
                "CREATE TABLE IF NOT EXISTS `warehouse`",
                "CREATE TABLE IF NOT EXISTS `warehouse_location`",
                "CREATE TABLE IF NOT EXISTS `inventory`",
                "CREATE TABLE IF NOT EXISTS `inventory_log`",
                "CREATE TABLE IF NOT EXISTS `inbound_order`",
                "CREATE TABLE IF NOT EXISTS `inbound_order_item`",
                "CREATE TABLE IF NOT EXISTS `outbound_order`",
                "CREATE TABLE IF NOT EXISTS `outbound_order_item`",
                "CREATE TABLE IF NOT EXISTS `stocktake_task`",
                "CREATE TABLE IF NOT EXISTS `stocktake_item`",
                "CREATE TABLE IF NOT EXISTS `transfer_order`",
                "CREATE TABLE IF NOT EXISTS `transfer_order_item`",
                "CREATE TABLE IF NOT EXISTS `inventory_warning_event`"
        );
    }

    @Test
    void shouldExposeAllDay04WmsApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-warehouse/src/main/java"));

        assertThat(controllers).contains(
                "/api/wms/warehouses",
                "/{wid}/locations",
                "/{wid}/locations/batch",
                "/{wid}/locations/available",
                "/api/wms/inventory",
                "/sku/{skuId}",
                "/warnings",
                "/logs",
                "/snapshot",
                "/adjust",
                "/api/wms/inbound",
                "/{id}/confirm",
                "/api/wms/outbound",
                "/{id}/picklist",
                "/{id}/pick",
                "/api/wms/transfers",
                "/{id}/approve",
                "/{id}/ship",
                "/{id}/receive",
                "/api/wms/stocktake",
                "/{id}/count",
                "/{id}/audit",
                "/api/wms/report",
                "/overview",
                "/health",
                "/trend"
        );
    }

    @Test
    void shouldCreateLightweightProductAndOrderBoundaryModules() throws Exception {
        String pom = Files.readString(projectRoot.resolve("pom.xml"));
        String gateway = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-gateway.yml"));

        assertThat(pom).contains("<module>supplychain-product</module>", "<module>supplychain-order</module>");
        assertThat(gateway).contains("lb://supplychain-product", "lb://supplychain-order", "Path=/api/pim/**", "Path=/api/oms/**");
        assertThat(projectRoot.resolve("supplychain-product/src/main/resources/application.yml")).exists();
        assertThat(projectRoot.resolve("supplychain-order/src/main/resources/application.yml")).exists();
        assertThat(projectRoot.resolve("nacos-config/extension/supplychain-product.yml")).exists();
        assertThat(projectRoot.resolve("nacos-config/extension/supplychain-order.yml")).exists();
    }

    @Test
    void shouldDocumentDay04InterviewHighlights() throws Exception {
        Path document = Path.of("/Users/liyunfei/privatefile/ob/2_工作/智晟未来/供应链/Day04_仓储管理系统（WMS）完整实现.md");
        assumeTrue(Files.exists(document), "外部访谈文档不存在，跳过文档佐证检查");
        String day04 = Files.readString(document);

        assertThat(day04).contains(
                "FIFO",
                "CAS",
                "库存流水不可篡改",
                "库位锁定",
                "加权平均成本",
                "Feign",
                "Seata",
                "XXL-JOB"
        );
    }

    private String readJavaSources(Path root) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java")).toList()) {
                builder.append(Files.readString(path)).append('\n');
            }
        }
        return builder.toString();
    }

    private Path findProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位项目根目录");
    }
}
