package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * FMS 与 BI 完整实现合同测试。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
class FmsBiContractTest {

    private final Path projectRoot = findProjectRoot();

    @Test
    void shouldContainFmsBiSchemaTables() throws Exception {
        String sql = readFiles(projectRoot.resolve("sql"), ".sql");

        assertThat(sql).contains(
                "CREATE TABLE IF NOT EXISTS `finance_exchange_rate`",
                "CREATE TABLE IF NOT EXISTS `finance_platform_bill`",
                "CREATE TABLE IF NOT EXISTS `finance_bill_item`",
                "CREATE TABLE IF NOT EXISTS `finance_profit_snapshot`",
                "CREATE TABLE IF NOT EXISTS `finance_vat_record`",
                "CREATE TABLE IF NOT EXISTS `finance_cash_flow`",
                "CREATE TABLE IF NOT EXISTS `bi_kpi_threshold`"
        );
    }

    @Test
    void shouldExposeAllFmsApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-finance/src/main/java"));

        assertThat(controllers).contains(
                "/api/fms/exchange-rates",
                "/refresh",
                "/api/fms/bills",
                "/upload",
                "/{id}/parse",
                "/{id}/items",
                "/{id}/confirm",
                "/summary",
                "/api/fms/profit",
                "/sku",
                "/store",
                "/trend",
                "/loss-warning",
                "/api/fms/receivables",
                "/api/fms/payables",
                "/{id}/apply",
                "/{id}/approve",
                "/{id}/paid",
                "/api/fms/vat",
                "/generate",
                "/{id}/export",
                "/api/fms/cash-flow",
                "/forecast"
        );
    }

    @Test
    void shouldExposeAllBiApiPaths() throws Exception {
        String controllers = readJavaSources(projectRoot.resolve("supplychain-finance/src/main/java"));

        assertThat(controllers).contains(
                "/api/bi/dashboard/overview",
                "/api/bi/dashboard/realtime",
                "/api/bi/sales/trend",
                "/api/bi/sales/platform-compare",
                "/api/bi/inventory/health",
                "/api/bi/inventory/turnover",
                "/api/bi/reorder/suggestions",
                "/api/bi/reorder/to-purchase",
                "/api/bi/reorder/forecast",
                "/api/bi/kpi/dashboard",
                "/api/bi/kpi/trend",
                "/api/bi/kpi/thresholds",
                "/api/bi/ai/query",
                "/api/bi/ai/templates",
                "/api/bi/export"
        );
    }

    @Test
    void shouldConfigureFinanceForFmsBiTechnologies() throws Exception {
        String financePom = Files.readString(projectRoot.resolve("supplychain-finance/pom.xml"));
        String gateway = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-gateway.yml"));
        String financeConfig = Files.readString(projectRoot.resolve("nacos-config/extension/supplychain-finance.yml"));

        assertThat(gateway).contains("lb://supplychain-finance", "Path=/api/fms/**", "Path=/api/bi/**");
        assertThat(financePom).contains(
                "mybatis-plus-spring-boot3-starter",
                "spring-cloud-starter-openfeign",
                "spring-cloud-starter-alibaba-seata",
                "spring-boot-starter-data-redis",
                "xxl-job-core"
        );
        assertThat(financeConfig).contains("xxl-job", "mybatis-plus", "redis", "bi:");
    }

    @Test
    void shouldDocumentFmsBiInterviewHighlights() throws Exception {
        Path documentRoot = Path.of("/Users/liyunfei/privatefile/ob/10_Blog/SAAS柔性供应链");
        assumeTrue(Files.exists(documentRoot), "外部访谈文档目录不存在，跳过文档佐证检查");
        String fmsBi = readFiles(documentRoot, ".md");

        assertThat(fmsBi).contains(
                "多平台结算报告解析",
                "SKU 级利润核算",
                "汇兑损益",
                "Redis",
                "XXL-JOB",
                "自然语言查询",
                "销售预测",
                "智能补货"
        );
    }

    private String readJavaSources(Path root) throws Exception {
        return readFiles(root, ".java");
    }

    private String readFiles(Path root, String suffix) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(suffix)).toList()) {
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
