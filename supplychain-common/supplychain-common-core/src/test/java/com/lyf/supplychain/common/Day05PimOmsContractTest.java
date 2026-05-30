package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Day05 PIM 与 OMS 完整实现合同测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class Day05PimOmsContractTest {

    private final Path projectRoot = findProjectRoot();

    @Test
    void shouldContainDay05PimOmsSchemaTables() throws Exception {
        String sql = Files.readString(projectRoot.resolve("sql/10_pim_oms_day05_schema.sql"));

        assertThat(sql).contains(
                "CREATE TABLE IF NOT EXISTS `product_category`",
                "CREATE TABLE IF NOT EXISTS `product_spu`",
                "CREATE TABLE IF NOT EXISTS `product_sku`",
                "CREATE TABLE IF NOT EXISTS `product_attr_template`",
                "CREATE TABLE IF NOT EXISTS `product_sku_price`",
                "CREATE TABLE IF NOT EXISTS `product_i18n`",
                "CREATE TABLE IF NOT EXISTS `product_image`",
                "CREATE TABLE IF NOT EXISTS `order_main`",
                "CREATE TABLE IF NOT EXISTS `order_item`",
                "CREATE TABLE IF NOT EXISTS `order_address`",
                "CREATE TABLE IF NOT EXISTS `order_log`",
                "CREATE TABLE IF NOT EXISTS `order_refund`",
                "CREATE TABLE IF NOT EXISTS `order_platform_raw`"
        );
    }

    @Test
    void shouldExposeAllDay05PimApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-product/src/main/java"));

        assertThat(controllers).contains(
                "/api/pim/categories",
                "/tree",
                "/api/pim/spus",
                "/{id}/submit",
                "/{id}/on-sale",
                "/{id}/off-sale",
                "/{spuId}/skus",
                "/batch",
                "/api/pim/skus",
                "/{id}/prices",
                "/{id}/i18n",
                "/{langCode}",
                "/translate",
                "/{id}/images",
                "/{id}/attrs",
                "/options"
        );
    }

    @Test
    void shouldExposeAllDay05OmsApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-order/src/main/java"));

        assertThat(controllers).contains(
                "/api/oms/orders",
                "/{id}/cancel",
                "/{id}/approve",
                "/{id}/reject",
                "/{id}/flag",
                "/{id}/sync",
                "/{id}/logs",
                "/{id}/split",
                "/merge",
                "/api/oms/refunds",
                "/{id}/audit",
                "/{id}/received",
                "/{id}/complete",
                "/api/oms/report",
                "/overview",
                "/today",
                "/api/oms/sync",
                "/logs",
                "/api/oms/webhook",
                "/amazon",
                "/tiktok"
        );
    }

    @Test
    void shouldConfigureDay05TechnicalDependencies() throws Exception {
        String productPom = Files.readString(projectRoot.resolve("supplychain-product/pom.xml"));
        String orderPom = Files.readString(projectRoot.resolve("supplychain-order/pom.xml"));
        String productConfig = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-product.yml"));
        String orderConfig = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-order.yml"));

        assertThat(productPom).contains("mybatis-plus-spring-boot3-starter", "xxl-job-core");
        assertThat(orderPom).contains(
                "mybatis-plus-spring-boot3-starter",
                "spring-boot-starter-data-redis",
                "spring-cloud-starter-openfeign",
                "spring-cloud-starter-alibaba-seata",
                "xxl-job-core"
        );
        assertThat(productConfig).contains("xxl-job", "mybatis-plus");
        assertThat(orderConfig).contains("xxl-job", "mybatis-plus", "redis");
    }

    @Test
    void shouldDocumentDay05InterviewHighlights() throws Exception {
        Path document = Path.of("/Users/liyunfei/privatefile/ob/2_工作/智晟未来/供应链/Day05_商品管理（PIM）+ 订单管理（OMS）完整实现.md");
        assumeTrue(Files.exists(document), "外部访谈文档不存在，跳过文档佐证检查");
        String day05 = Files.readString(document);

        assertThat(day05).contains(
                "Redis",
                "Feign",
                "Seata",
                "XXL-JOB",
                "状态机",
                "幂等",
                "Webhook 签名"
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
