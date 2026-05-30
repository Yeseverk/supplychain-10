package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Day06 TMS 与跨境合规合同测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class Day06TmsContractTest {

    private final Path projectRoot = findProjectRoot();

    @Test
    void shouldContainDay06TmsSchemaTables() throws Exception {
        String sql = Files.readString(projectRoot.resolve("sql/11_tms_day06_schema.sql"));

        assertThat(sql).contains(
                "CREATE TABLE IF NOT EXISTS `logistics_carrier`",
                "CREATE TABLE IF NOT EXISTS `logistics_channel`",
                "CREATE TABLE IF NOT EXISTS `logistics_rate`",
                "CREATE TABLE IF NOT EXISTS `logistics_waybill`",
                "CREATE TABLE IF NOT EXISTS `logistics_track`",
                "CREATE TABLE IF NOT EXISTS `logistics_fee_record`",
                "CREATE TABLE IF NOT EXISTS `logistics_bill_record`",
                "CREATE TABLE IF NOT EXISTS `logistics_return`",
                "CREATE TABLE IF NOT EXISTS `tax_vat_rate`"
        );
    }

    @Test
    void shouldExposeAllDay06TmsApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-logistics/src/main/java"));

        assertThat(controllers).contains(
                "/api/tms/carriers",
                "/api/tms/channels",
                "/{id}/disable",
                "/{id}/rates",
                "/api/tms/waybills",
                "/batch",
                "/{id}/label",
                "/labels/batch",
                "/{id}/status",
                "/{id}/cancel",
                "/api/tms/recommend",
                "/api/tms/fee/estimate",
                "/{id}/tracks",
                "/exceptions",
                "/{id}/tracks/refresh",
                "/api/tms/returns",
                "/{id}/arrive",
                "/api/tms/fees",
                "/import-bill",
                "/api/tms/report",
                "/channel-efficiency",
                "/fee-summary",
                "/api/tms/webhook",
                "/dhl",
                "/fedex"
        );
    }

    @Test
    void shouldRegisterLogisticsModuleAndRoute() throws Exception {
        String pom = Files.readString(projectRoot.resolve("pom.xml"));
        String gateway = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-gateway.yml"));
        String logisticsPom = Files.readString(projectRoot.resolve("supplychain-logistics/pom.xml"));
        String logisticsConfig = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-logistics.yml"));

        assertThat(pom).contains("<module>supplychain-logistics</module>", "<artifactId>supplychain-logistics</artifactId>");
        assertThat(gateway).contains("lb://supplychain-logistics", "Path=/api/tms/**");
        assertThat(logisticsPom).contains(
                "mybatis-plus-spring-boot3-starter",
                "spring-cloud-starter-openfeign",
                "spring-cloud-starter-alibaba-seata",
                "xxl-job-core"
        );
        assertThat(logisticsConfig).contains("xxl-job", "mybatis-plus");
    }

    @Test
    void shouldDocumentDay06InterviewHighlights() throws Exception {
        Path document = Path.of("/Users/liyunfei/privatefile/ob/2_工作/智晟未来/供应链/Day06_物流管理系统（TMS）+ 跨境合规完整实现.md");
        assumeTrue(Files.exists(document), "外部访谈文档不存在，跳过文档佐证检查");
        String day06 = Files.readString(document);

        assertThat(day06).contains(
                "渠道过滤规则",
                "轨迹标准化",
                "材积重",
                "Adapter Pattern",
                "Feign",
                "Seata",
                "XXL-JOB",
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
