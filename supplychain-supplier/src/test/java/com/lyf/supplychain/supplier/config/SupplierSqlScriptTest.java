package com.lyf.supplychain.supplier.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 供应商模块 SQL 脚本测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class SupplierSqlScriptTest {

    @Test
    void schemaScriptShouldContainAllSupplierTables() throws Exception {
        String sql = Files.readString(projectRoot().resolve("sql/03_supplier_schema.sql"));

        assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS `supplier`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_cert`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_contact`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_score_log`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_watchlist`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_tenant_config`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_risk_event`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_purchase_arrival`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_quality_inspection`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_quote_response`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_purchase_price`")
                .contains("CREATE TABLE IF NOT EXISTS `supplier_audit_log`");
    }

    @Test
    void systemSchemaScriptShouldContainMessageTable() throws Exception {
        String sql = Files.readString(projectRoot().resolve("sql/01_schema.sql"));

        assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS `sys_message`")
                .contains("CREATE TABLE IF NOT EXISTS `sys_event_outbox`")
                .contains("KEY `idx_tenant_receiver` (`tenant_id`, `receiver_type`, `receiver_id`, `read_status`)")
                .contains("KEY `idx_receiver_key` (`tenant_id`, `receiver_type`, `receiver_key`, `read_status`)")
                .contains("UNIQUE KEY `uk_event_id` (`event_id`)")
                .contains("UNIQUE KEY `uk_tenant_idempotent` (`tenant_id`, `idempotent_key`)");
    }

    @Test
    void schemaScriptShouldContainPortalAccountUniqueFallback() throws Exception {
        String sql = Files.readString(projectRoot().resolve("sql/03_supplier_schema.sql"));

        assertThat(sql)
                .contains("UNIQUE KEY `uk_portal_user_id` (`portal_user_id`)")
                .contains("UNIQUE KEY `uk_tenant_contact_email` (`tenant_id`, `contact_email`)");
    }

    @Test
    void initScriptShouldContainSupplierDictData() throws Exception {
        String sql = Files.readString(projectRoot().resolve("sql/04_supplier_init_data.sql"));

        assertThat(sql)
                .contains("supplier_cert_type")
                .contains("supplier_contact_type")
                .contains("company_size")
                .contains("supplier.layering.watchlist.grades")
                .contains("supplier.multi.min_count")
                .contains("supplier.multi.min_healthy_grade");
    }

    private Path projectRoot() {
        Path path = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (path != null && !Files.exists(path.resolve("sql"))) {
            path = path.getParent();
        }
        return path;
    }
}
