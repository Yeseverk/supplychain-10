package com.lyf.supplychain.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Day03 采购、仓储、财务和 Seata 脚本约束测试。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
class Day03SqlScriptTest {

    private final Path projectRoot = findProjectRoot();

    @Test
    void shouldProvidePurchaseWarehouseFinanceAndSeataScripts() throws Exception {
        assertThat(Files.exists(projectRoot.resolve("sql/05_purchase_schema.sql"))).isTrue();
        assertThat(Files.exists(projectRoot.resolve("sql/06_warehouse_schema.sql"))).isTrue();
        assertThat(Files.exists(projectRoot.resolve("sql/07_finance_schema.sql"))).isTrue();
        assertThat(Files.exists(projectRoot.resolve("sql/08_seata_schema.sql"))).isTrue();
    }

    @Test
    void purchaseScriptShouldContainDay03CoreTables() throws Exception {
        String sql = Files.readString(projectRoot.resolve("sql/05_purchase_schema.sql"));

        assertThat(sql).contains(
                "CREATE TABLE IF NOT EXISTS `purchase_requisition`",
                "CREATE TABLE IF NOT EXISTS `purchase_requisition_item`",
                "CREATE TABLE IF NOT EXISTS `purchase_inquiry`",
                "CREATE TABLE IF NOT EXISTS `purchase_inquiry_item`",
                "CREATE TABLE IF NOT EXISTS `purchase_order`",
                "CREATE TABLE IF NOT EXISTS `purchase_order_item`",
                "CREATE TABLE IF NOT EXISTS `purchase_receipt`",
                "CREATE TABLE IF NOT EXISTS `purchase_receipt_item`",
                "CREATE TABLE IF NOT EXISTS `purchase_return`"
        );
    }

    @Test
    void warehouseFinanceAndSeataScriptsShouldContainIntegrationTables() throws Exception {
        String warehouseSql = Files.readString(projectRoot.resolve("sql/06_warehouse_schema.sql"));
        String financeSql = Files.readString(projectRoot.resolve("sql/07_finance_schema.sql"));
        String seataSql = Files.readString(projectRoot.resolve("sql/08_seata_schema.sql"));

        assertThat(warehouseSql).contains(
                "CREATE TABLE IF NOT EXISTS `warehouse_inventory`",
                "CREATE TABLE IF NOT EXISTS `warehouse_inventory_log`"
        );
        assertThat(financeSql).contains(
                "CREATE TABLE IF NOT EXISTS `finance_payable`",
                "CREATE TABLE IF NOT EXISTS `finance_payment_record`"
        );
        assertThat(seataSql).contains("CREATE TABLE IF NOT EXISTS `undo_log`");
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
