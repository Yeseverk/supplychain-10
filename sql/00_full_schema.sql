-- ============================================================
-- FlexChain seller workspace consolidated SQL
-- Full schema exported from current supplychain_dev database. Run before 01_demo_seed.sql.
-- Generated from local MySQL via mysqldump.
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `supplychain_dev` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `supplychain_dev`;
DROP TABLE IF EXISTS `bi_kpi_threshold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bi_kpi_threshold` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `kpi_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'KPI编码',
  `kpi_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'KPI名称',
  `warning_value` decimal(12,4) NOT NULL COMMENT '预警阈值',
  `danger_value` decimal(12,4) NOT NULL COMMENT '危险阈值',
  `compare_type` tinyint NOT NULL COMMENT '比较类型：1小于触发 2大于触发',
  `notify_roles` json DEFAULT NULL COMMENT '通知角色列表',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_kpi` (`tenant_id`,`kpi_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KPI预警阈值配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_bill_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_bill_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `item_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '明细类型',
  `order_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台订单号',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `platform_sku` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台SKU',
  `amount` decimal(12,4) NOT NULL COMMENT '金额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '货币',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说明',
  `transaction_date` date DEFAULT NULL COMMENT '交易日期',
  `is_matched` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否匹配内部订单',
  `match_order_id` bigint DEFAULT NULL COMMENT '匹配订单ID',
  PRIMARY KEY (`id`),
  KEY `idx_bill_id` (`bill_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_is_matched` (`is_matched`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_cash_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_cash_flow` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `flow_date` date NOT NULL COMMENT '流水日期',
  `flow_type` tinyint NOT NULL COMMENT '类型：1收入 2支出',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `source_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源单据编号',
  `amount_cny` decimal(14,2) NOT NULL COMMENT '人民币金额',
  `amount_origin` decimal(14,2) DEFAULT NULL COMMENT '原币金额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原币货币',
  `exchange_rate` decimal(10,6) DEFAULT NULL COMMENT '汇率',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_source` (`tenant_id`,`source_type`,`source_id`),
  KEY `idx_tenant_date` (`tenant_id`,`flow_date`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_flow_type` (`flow_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金流水记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_exchange_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_exchange_rate` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `rate_date` date NOT NULL COMMENT '汇率日期',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '外币货币代码',
  `rate_to_cny` decimal(12,6) NOT NULL COMMENT '1单位外币兑换人民币汇率',
  `rate_source` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_FALLBACK' COMMENT '汇率来源',
  `is_official` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否官方汇率',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_currency` (`rate_date`,`currency`),
  KEY `idx_currency_date` (`currency`,`rate_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='汇率表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_payable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_payable` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `payable_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '应付账款单号',
  `source_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型：PURCHASE_ORDER采购单 TMS_LOGISTICS_BILL物流账单',
  `source_biz_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源业务单号',
  `po_id` bigint DEFAULT NULL COMMENT '采购单ID',
  `po_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '采购单编号',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '供应商名称',
  `invoice_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发票编号',
  `invoice_date` date DEFAULT NULL COMMENT '发票日期',
  `payable_amount` decimal(12,2) NOT NULL COMMENT '应付金额',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已付金额',
  `remaining_amount` decimal(12,2) GENERATED ALWAYS AS ((`payable_amount` - `paid_amount`)) STORED COMMENT '剩余金额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `payment_days` int NOT NULL DEFAULT '0' COMMENT '账期天数',
  `due_date` date NOT NULL COMMENT '到期日',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待付 1部分已付 2已结清 3作废',
  `overdue_days` int NOT NULL DEFAULT '0' COMMENT '逾期天数',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_payable_no` (`tenant_id`,`payable_no`),
  UNIQUE KEY `uk_po_id` (`po_id`),
  UNIQUE KEY `uk_tenant_source_biz` (`tenant_id`,`source_type`,`source_biz_no`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_due_date` (`due_date`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应付账款表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_payment_record` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `payable_id` bigint NOT NULL COMMENT '应付账款ID',
  `payment_amount` decimal(12,2) NOT NULL COMMENT '付款金额',
  `payment_date` date NOT NULL COMMENT '付款日期',
  `payment_method` tinyint NOT NULL DEFAULT '1' COMMENT '付款方式',
  `voucher_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '凭证号',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payable_id` (`payable_id`),
  KEY `idx_payment_date` (`payment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='付款记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_platform_bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_platform_bill` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `bill_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内部账单号',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台',
  `store_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `store_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺名称',
  `platform_bill_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台账单ID',
  `settlement_start` date NOT NULL COMMENT '结算开始日期',
  `settlement_end` date NOT NULL COMMENT '结算结束日期',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单货币',
  `total_sales` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '销售收入总额',
  `total_refund` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '退款总额',
  `referral_fee` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '平台佣金',
  `fba_fee` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT 'FBA费用',
  `storage_fee` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '仓储费',
  `advertising_fee` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '广告费',
  `other_fee` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '其他费用',
  `net_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '净到账金额',
  `cny_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '折合人民币净到账金额',
  `exchange_rate` decimal(10,6) NOT NULL DEFAULT '1.000000' COMMENT '结算汇率',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待解析 1解析中 2已解析 3对账完成 4有差异',
  `source_file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始账单文件地址',
  `import_time` datetime DEFAULT NULL COMMENT '导入时间',
  `import_user_id` bigint DEFAULT NULL COMMENT '导入操作人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_bill_no` (`tenant_id`,`bill_no`),
  UNIQUE KEY `uk_platform_bill_id` (`tenant_id`,`platform`,`store_id`,`platform_bill_id`),
  KEY `idx_tenant_platform_period` (`tenant_id`,`platform`,`settlement_start`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台结算账单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_profit_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_profit_snapshot` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `snapshot_type` tinyint NOT NULL COMMENT '快照类型：1日 2月',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台',
  `store_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺ID',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家代码',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始货币',
  `exchange_rate` decimal(10,6) NOT NULL COMMENT '汇率',
  `order_count` int NOT NULL DEFAULT '0' COMMENT '订单数',
  `sales_qty` int NOT NULL DEFAULT '0' COMMENT '销售数量',
  `gross_revenue` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '原币收入',
  `gross_revenue_cny` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '人民币收入',
  `purchase_cost` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '采购成本',
  `logistics_fee` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '物流费',
  `platform_fee` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '平台费',
  `fba_storage_fee` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '仓储费',
  `advertising_fee` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '广告费',
  `refund_loss` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '退款损失',
  `vat_fee` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT 'VAT费用',
  `other_cost` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '其他成本',
  `total_cost` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '总成本',
  `gross_profit` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '毛利润',
  `net_profit` decimal(14,4) NOT NULL DEFAULT '0.0000' COMMENT '净利润',
  `gross_margin` decimal(8,4) NOT NULL DEFAULT '0.0000' COMMENT '毛利率',
  `net_margin` decimal(8,4) NOT NULL DEFAULT '0.0000' COMMENT '净利率',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_snapshot` (`tenant_id`,`snapshot_type`,`snapshot_date`,`sku_id`,`platform`,`store_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_snapshot_date` (`snapshot_date`),
  KEY `idx_platform_date` (`platform`,`snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU利润快照表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `finance_vat_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finance_vat_record` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申报国家',
  `vat_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'VAT登记号',
  `period` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申报期',
  `taxable_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '应税销售额',
  `vat_rate` decimal(6,4) NOT NULL COMMENT 'VAT税率',
  `vat_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '应缴VAT',
  `local_currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '当地货币',
  `cny_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '折合人民币',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待申报 1已申报 2已缴纳 3异常',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申报文件地址',
  `declare_time` datetime DEFAULT NULL COMMENT '申报时间',
  `pay_time` datetime DEFAULT NULL COMMENT '缴纳时间',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_country_period` (`tenant_id`,`country_code`,`period`),
  KEY `idx_status` (`status`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VAT申报记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inbound_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inbound_order` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `inbound_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '入库单号',
  `inbound_type` tinyint NOT NULL COMMENT '入库类型',
  `warehouse_id` bigint NOT NULL COMMENT '目标仓库ID',
  `warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `ref_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `ref_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `ref_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源单号',
  `expected_date` date DEFAULT NULL COMMENT '预计到货日期',
  `actual_date` date DEFAULT NULL COMMENT '实际入库日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待入库 1入库中 2完成 3取消',
  `total_sku_count` int NOT NULL DEFAULT '0' COMMENT 'SKU种数',
  `total_qty` int NOT NULL DEFAULT '0' COMMENT '计划数量',
  `actual_qty` int NOT NULL DEFAULT '0' COMMENT '实际数量',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_inbound_no` (`tenant_id`,`inbound_no`),
  KEY `idx_warehouse_status` (`warehouse_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inbound_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inbound_order_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `inbound_id` bigint NOT NULL COMMENT '入库单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `expected_qty` int NOT NULL COMMENT '计划入库数量',
  `actual_qty` int NOT NULL DEFAULT '0' COMMENT '实际入库数量',
  `defective_qty` int NOT NULL DEFAULT '0' COMMENT '不良数量',
  `location_id` bigint DEFAULT NULL COMMENT '上架库位ID',
  `location_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上架库位编码',
  `unit_cost` decimal(12,4) DEFAULT NULL COMMENT '入库成本单价',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待入库 1已入库 2部分入库',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_inbound_id` (`inbound_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID，NULL代表仓库汇总',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU名称',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '实物库存',
  `frozen_qty` int NOT NULL DEFAULT '0' COMMENT '冻结库存',
  `in_transit_qty` int NOT NULL DEFAULT '0' COMMENT '在途库存',
  `defective_qty` int NOT NULL DEFAULT '0' COMMENT '不良品库存',
  `reserved_qty` int NOT NULL DEFAULT '0' COMMENT '预留库存',
  `safety_stock` int NOT NULL DEFAULT '0' COMMENT '安全库存',
  `max_stock` int DEFAULT NULL COMMENT '最大库存',
  `reorder_point` int DEFAULT NULL COMMENT '补货点',
  `avg_cost` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '加权平均成本',
  `total_cost` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '总成本',
  `last_inbound_time` datetime DEFAULT NULL COMMENT '最后入库时间',
  `last_outbound_time` datetime DEFAULT NULL COMMENT '最后出库时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_location_sku` (`warehouse_id`,`location_id`,`sku_id`),
  KEY `idx_tenant_sku` (`tenant_id`,`sku_id`),
  KEY `idx_warehouse_sku` (`warehouse_id`,`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inventory_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `log_type` tinyint NOT NULL COMMENT '流水类型：1采购入库 2销售出库 3调拨入库 4调拨出库 5盘盈 6盘亏 7退货入库 8报损 11冻结 12解冻',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU名称',
  `change_qty` int NOT NULL COMMENT '变动数量',
  `before_qty` int NOT NULL COMMENT '变动前数量',
  `after_qty` int NOT NULL COMMENT '变动后数量',
  `ref_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单据类型',
  `ref_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单号',
  `ref_id` bigint DEFAULT NULL COMMENT '关联单据ID',
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '批次号',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_sku_time` (`tenant_id`,`sku_id`,`operate_time`),
  KEY `idx_ref_no` (`ref_no`),
  KEY `idx_log_type` (`log_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inventory_platform_allocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_platform_allocation` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台编码：AMAZON/SHOPIFY/EBAY/TIKTOK等',
  `store_id` bigint NOT NULL DEFAULT '0' COMMENT '店铺ID，0表示平台通用配额',
  `allocated_qty` int NOT NULL DEFAULT '0' COMMENT '分配库存数量',
  `frozen_qty` int NOT NULL DEFAULT '0' COMMENT '已冻结数量，平台订单创建后冻结',
  `available_qty` int NOT NULL DEFAULT '0' COMMENT '平台可售数量',
  `sold_qty` int NOT NULL DEFAULT '0' COMMENT '已销售数量，订单出库后确认',
  `allocation_ratio` int DEFAULT NULL COMMENT '分配比例，0-100',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=停用 1=启用',
  `last_sync_status` tinyint NOT NULL DEFAULT '0' COMMENT '最近同步状态：0=未同步 1=成功 2=失败',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `last_sync_message` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_sku_platform_store` (`tenant_id`,`sku_id`,`platform`,`store_id`),
  KEY `idx_tenant_platform` (`tenant_id`,`platform`),
  KEY `idx_tenant_sku` (`tenant_id`,`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台库存分配表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inventory_warning_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_warning_event` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `warning_level` tinyint NOT NULL COMMENT '预警级别：1紧张 2不足 3零库存 4积压',
  `available_qty` int NOT NULL COMMENT '可售库存',
  `safety_stock` int NOT NULL COMMENT '安全库存',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1生效 2解除',
  `first_detected_time` datetime NOT NULL COMMENT '首次发现时间',
  `last_detected_time` datetime NOT NULL COMMENT '最近发现时间',
  `resolved_time` datetime DEFAULT NULL COMMENT '解除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warning_active` (`tenant_id`,`warehouse_id`,`sku_id`,`warning_level`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警事件表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_bill_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_bill_record` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `bill_batch_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单导入批次号',
  `carrier_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物流商编码',
  `waybill_id` bigint DEFAULT NULL COMMENT '匹配到的系统运单ID',
  `payable_id` bigint DEFAULT NULL COMMENT '财务应付账款ID',
  `waybill_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内部运单号',
  `tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流商运单号',
  `billing_weight_g` decimal(10,2) DEFAULT NULL COMMENT '账单计费重量',
  `base_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '基础运费',
  `fuel_surcharge` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '燃油附加费',
  `peak_surcharge` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '旺季附加费',
  `remote_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '偏远费',
  `other_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '其他费用',
  `actual_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账单实际总费用',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `diff_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实际费用与预估费用差异金额',
  `diff_rate` decimal(8,4) NOT NULL DEFAULT '0.0000' COMMENT '费用差异率',
  `reconcile_status` tinyint NOT NULL COMMENT '对账状态：0=自动通过 1=待人工复核 2=未匹配',
  `confirm_time` datetime DEFAULT NULL COMMENT '账单确认时间',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '对账备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_batch_no` (`tenant_id`,`bill_batch_no`),
  KEY `idx_tracking_no` (`tracking_no`),
  KEY `idx_reconcile_status` (`reconcile_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流商账单导入对账记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_carrier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_carrier` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `carrier_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物流商编码',
  `carrier_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物流商名称',
  `carrier_name_en` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流商英文名',
  `carrier_type` tinyint NOT NULL COMMENT '物流商类型',
  `logo_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Logo地址',
  `api_base_url` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API基础地址',
  `api_key` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API Key',
  `api_secret` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API Secret',
  `api_account` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API账号',
  `api_version` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API版本',
  `track_api_url` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '轨迹API地址',
  `support_label` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否支持面单',
  `support_track` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否支持轨迹',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=停用 1=正常',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_carrier_code` (`tenant_id`,`carrier_code`),
  KEY `idx_carrier_type` (`carrier_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流商主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_channel` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `carrier_id` bigint NOT NULL COMMENT '物流商ID',
  `channel_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码',
  `channel_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `channel_type` tinyint NOT NULL COMMENT '渠道类型',
  `country_codes` json NOT NULL COMMENT '适用国家',
  `min_weight_g` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '最小重量克',
  `max_weight_g` decimal(10,2) NOT NULL COMMENT '最大重量克',
  `max_length_mm` int DEFAULT NULL COMMENT '最长边毫米',
  `max_girth_mm` int DEFAULT NULL COMMENT '围长毫米',
  `allow_battery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许含电',
  `allow_liquid` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许液体',
  `allow_powder` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许粉末',
  `allow_food` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许食品',
  `min_days` decimal(4,1) NOT NULL COMMENT '最短时效',
  `max_days` decimal(4,1) NOT NULL COMMENT '最长时效',
  `volume_factor` int NOT NULL DEFAULT '5000' COMMENT '材积系数',
  `declared_value_limit` decimal(12,2) DEFAULT NULL COMMENT '申报价值上限',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_carrier_channel_code` (`carrier_id`,`channel_code`),
  KEY `idx_carrier_id` (`carrier_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流渠道表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_fee_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_fee_record` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `waybill_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运单号',
  `base_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '基础运费',
  `fuel_surcharge` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '燃油附加费',
  `peak_surcharge` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '旺季附加费',
  `remote_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '偏远费',
  `oversize_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '超尺寸费',
  `insurance_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '保价费',
  `other_fee` decimal(12,4) NOT NULL DEFAULT '0.0000' COMMENT '其他费',
  `estimated_total` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '预估总费用',
  `actual_total` decimal(12,2) DEFAULT NULL COMMENT '实际总费用',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `billing_weight_g` decimal(10,2) NOT NULL COMMENT '计费重量',
  `rate_id` bigint DEFAULT NULL COMMENT '费率ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waybill_id` (`waybill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运单费用记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_rate` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `channel_id` bigint NOT NULL COMMENT '渠道ID',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目的国',
  `zone` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `first_weight_g` decimal(10,2) NOT NULL COMMENT '首重克',
  `first_weight_price` decimal(10,4) NOT NULL COMMENT '首重价格',
  `extra_weight_g` decimal(10,2) NOT NULL DEFAULT '500.00' COMMENT '续重克',
  `extra_weight_price` decimal(10,4) NOT NULL COMMENT '续重价格',
  `min_charge` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '最低收费',
  `fuel_rate` decimal(6,4) NOT NULL DEFAULT '0.0000' COMMENT '燃油费率',
  `peak_rate` decimal(6,4) NOT NULL DEFAULT '0.0000' COMMENT '旺季费率',
  `remote_area_fee` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '偏远附加费',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_channel_country` (`channel_id`,`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流渠道费率表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_return` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `return_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退货单号',
  `original_waybill_id` bigint DEFAULT NULL COMMENT '原运单ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `refund_id` bigint DEFAULT NULL COMMENT '退款单ID',
  `return_type` tinyint NOT NULL COMMENT '退货类型',
  `carrier_id` bigint DEFAULT NULL COMMENT '物流商ID',
  `return_tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退货运单号',
  `from_country` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源国',
  `to_warehouse_id` bigint DEFAULT NULL COMMENT '目标仓库',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `expected_arrive_date` date DEFAULT NULL COMMENT '预计到仓',
  `actual_arrive_date` date DEFAULT NULL COMMENT '实际到仓',
  `label_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退货面单',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_return_no` (`tenant_id`,`return_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退货运单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_track`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_track` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `waybill_id` bigint NOT NULL COMMENT '运单ID',
  `tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物流商运单号',
  `track_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '轨迹编码',
  `track_stage` tinyint NOT NULL COMMENT '轨迹阶段',
  `raw_status` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始状态',
  `status_desc` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中文状态',
  `location` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置',
  `location_country` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置国家',
  `track_time` datetime NOT NULL COMMENT '轨迹时间',
  `fetch_time` datetime NOT NULL COMMENT '抓取时间',
  `is_exception` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否异常',
  `exception_type` tinyint DEFAULT NULL COMMENT '异常类型',
  `exception_desc` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '异常描述',
  PRIMARY KEY (`id`),
  KEY `idx_waybill_id` (`waybill_id`),
  KEY `idx_tracking_no` (`tracking_no`),
  KEY `idx_is_exception` (`is_exception`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流轨迹记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `logistics_waybill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logistics_waybill` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `waybill_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内部运单号',
  `tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流商运单号',
  `carrier_id` bigint NOT NULL COMMENT '物流商ID',
  `channel_id` bigint NOT NULL COMMENT '渠道ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `receiver_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收件人',
  `receiver_phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目的国',
  `state` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '州省',
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `address_line1` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址1',
  `address_line2` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址2',
  `zip_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮编',
  `actual_weight_g` decimal(10,2) NOT NULL COMMENT '实际重量',
  `volume_weight_g` decimal(10,2) DEFAULT NULL COMMENT '材积重',
  `charge_weight_g` decimal(10,2) NOT NULL COMMENT '计费重量',
  `length_mm` int DEFAULT NULL COMMENT '长度',
  `width_mm` int DEFAULT NULL COMMENT '宽度',
  `height_mm` int DEFAULT NULL COMMENT '高度',
  `package_count` int NOT NULL DEFAULT '1' COMMENT '包裹数',
  `declared_value` decimal(12,2) NOT NULL COMMENT '申报价值',
  `declared_currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USD' COMMENT '申报币种',
  `declared_name_en` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '英文申报名',
  `hs_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'HS编码',
  `is_gift` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否礼品',
  `estimated_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '预估运费',
  `actual_fee` decimal(12,2) DEFAULT NULL COMMENT '实际运费',
  `fee_currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '运费币种',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `label_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面单地址',
  `label_format` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '面单格式',
  `create_waybill_time` datetime DEFAULT NULL COMMENT '创建运单时间',
  `pickup_time` datetime DEFAULT NULL COMMENT '揽收时间',
  `signed_time` datetime DEFAULT NULL COMMENT '签收时间',
  `exception_desc` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '异常描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_waybill_no` (`tenant_id`,`waybill_no`),
  UNIQUE KEY `uk_tenant_order_id` (`tenant_id`,`order_id`),
  UNIQUE KEY `uk_tracking_no` (`tracking_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物流运单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_address` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `receiver_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收件人',
  `phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '国家代码',
  `country_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `state` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '州省',
  `city` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `address_line1` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '地址1',
  `address_line2` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址2',
  `zip_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮编',
  `full_address` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '完整地址',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否验证',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `platform_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台SKU',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(12,4) NOT NULL COMMENT '单价',
  `discount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '优惠',
  `amount` decimal(12,2) NOT NULL COMMENT '小计',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `refunded_qty` int NOT NULL DEFAULT '0' COMMENT '已退款数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `from_status` tinyint DEFAULT NULL COMMENT '原状态',
  `to_status` tinyint NOT NULL COMMENT '新状态',
  `action` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动作',
  `operator_type` tinyint NOT NULL DEFAULT '1' COMMENT '操作人类型',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_main`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_main` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内部订单号',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台',
  `platform_order_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台订单号',
  `store_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `total_amount` decimal(12,2) NOT NULL COMMENT '总金额',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额',
  `shipping_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '运费',
  `payment_amount` decimal(12,2) NOT NULL COMMENT '实付金额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `exchange_rate` decimal(10,6) NOT NULL DEFAULT '1.000000' COMMENT '汇率',
  `cny_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '人民币金额',
  `platform_fee` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '平台手续费',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态',
  `cancel_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `is_abnormal` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否异常',
  `abnormal_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '异常原因',
  `warehouse_id` bigint DEFAULT NULL COMMENT '仓库ID',
  `logistics_channel` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流渠道',
  `waybill_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运单号',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `delivery_deadline` date DEFAULT NULL COMMENT '最晚发货日期',
  `signed_time` datetime DEFAULT NULL COMMENT '签收时间',
  `platform_order_time` datetime NOT NULL COMMENT '平台下单时间',
  `platform_pay_time` datetime DEFAULT NULL COMMENT '平台支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_order_no` (`tenant_id`,`order_no`),
  UNIQUE KEY `uk_platform_order_no` (`tenant_id`,`platform`,`platform_order_no`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_delivery_deadline` (`delivery_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_platform_raw`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_platform_raw` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台',
  `platform_order_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台订单号',
  `raw_data` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始JSON',
  `sync_time` datetime NOT NULL COMMENT '同步时间',
  `sync_type` tinyint NOT NULL DEFAULT '1' COMMENT '同步方式',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_platform_order_no` (`platform_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台订单原始数据表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `order_refund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_refund` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `refund_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款单号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `platform_refund_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台退款号',
  `refund_type` tinyint NOT NULL COMMENT '退款类型',
  `refund_reason` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原因',
  `reason_detail` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原因详情',
  `refund_amount` decimal(12,2) NOT NULL COMMENT '申请金额',
  `actual_refund_amount` decimal(12,2) DEFAULT NULL COMMENT '实际金额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `evidence_urls` json DEFAULT NULL COMMENT '凭证',
  `return_tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退货单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_refund_no` (`tenant_id`,`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `outbound_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbound_order` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `outbound_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出库单号',
  `outbound_type` tinyint NOT NULL COMMENT '出库类型',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `ref_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型',
  `ref_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `ref_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源单号',
  `plan_date` date DEFAULT NULL COMMENT '计划出库日期',
  `actual_date` date DEFAULT NULL COMMENT '实际出库日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待分配 1待拣货 2拣货中 3待复核 4已出库 5取消',
  `pick_user_id` bigint DEFAULT NULL COMMENT '拣货人ID',
  `pick_start_time` datetime DEFAULT NULL COMMENT '开始拣货时间',
  `pick_end_time` datetime DEFAULT NULL COMMENT '完成拣货时间',
  `total_qty` int NOT NULL DEFAULT '0' COMMENT '出库总数',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_outbound_no` (`tenant_id`,`outbound_no`),
  KEY `idx_warehouse_status` (`warehouse_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `outbound_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `outbound_order_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `quantity` int NOT NULL COMMENT '计划出库数量',
  `picked_qty` int NOT NULL DEFAULT '0' COMMENT '已拣数量',
  `location_id` bigint DEFAULT NULL COMMENT '拣货库位ID',
  `location_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拣货库位编码',
  `pick_status` tinyint NOT NULL DEFAULT '0' COMMENT '拣货状态',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_outbound_id` (`outbound_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_attr_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attr_template` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `attr_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '属性名称',
  `attr_name_en` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '属性英文名',
  `attr_type` tinyint NOT NULL DEFAULT '1' COMMENT '属性类型',
  `attr_options` json DEFAULT NULL COMMENT '选项JSON',
  `attr_unit` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单位',
  `is_sku_spec` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否SKU规格',
  `is_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必填',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类属性模板表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_category` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID，0表示顶级分类',
  `category_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_name_en` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '英文分类名称',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '层级深度',
  `path` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '/' COMMENT '分类路径',
  `icon_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类图标',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_parent` (`tenant_id`,`parent_id`),
  KEY `idx_path` (`path`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_i18n` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `ref_type` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联类型',
  `ref_id` bigint NOT NULL COMMENT '关联ID',
  `lang_code` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '语言代码',
  `title` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `subtitle` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '副标题',
  `bullet_points` json DEFAULT NULL COMMENT '卖点',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `keywords` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键词',
  `search_terms` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '搜索词',
  `is_ai_translated` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否AI翻译',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ref_lang` (`ref_type`,`ref_id`,`lang_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品多语言内容表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID',
  `image_type` tinyint NOT NULL COMMENT '图片类型',
  `image_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `thumb_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '缩略图',
  `image_width` int DEFAULT NULL COMMENT '宽度',
  `image_height` int DEFAULT NULL COMMENT '高度',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `alt_text` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ALT文案',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_sku` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `barcode` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '条形码',
  `fnsku` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '亚马逊仓储条码',
  `spec_values` json NOT NULL COMMENT '中文规格JSON',
  `spec_values_en` json DEFAULT NULL COMMENT '英文规格JSON',
  `net_weight_g` decimal(10,2) DEFAULT NULL COMMENT '净重克',
  `gross_weight_g` decimal(10,2) DEFAULT NULL COMMENT '毛重克',
  `length_mm` decimal(10,2) DEFAULT NULL COMMENT '长毫米',
  `width_mm` decimal(10,2) DEFAULT NULL COMMENT '宽毫米',
  `height_mm` decimal(10,2) DEFAULT NULL COMMENT '高毫米',
  `is_battery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否含电池',
  `is_liquid` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否液体',
  `is_powder` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否粉末',
  `cost_price` decimal(12,4) DEFAULT NULL COMMENT '成本价',
  `cost_currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '成本价币种',
  `abc_class` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ABC分类',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=草稿 1=已上架 2=已下架 3=已停售',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_sku_code` (`tenant_id`,`sku_code`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU最小库存单元表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_sku_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_sku_price` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `price_type` tinyint NOT NULL COMMENT '价格类型',
  `platform` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `price` decimal(12,4) NOT NULL COMMENT '价格',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `min_qty` int NOT NULL DEFAULT '1' COMMENT '最小数量',
  `max_qty` int DEFAULT NULL COMMENT '最大数量',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否生效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_sku_platform` (`sku_id`,`platform`,`country_code`,`price_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SKU多平台价格表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product_spu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_spu` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `spu_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SPU编码',
  `spu_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SPU名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `category_path` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类路径',
  `brand` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
  `hs_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'HS编码',
  `origin_country` char(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原产国',
  `material` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '材质',
  `certifications` json DEFAULT NULL COMMENT '认证列表',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=草稿 1=待审核 2=已上架 3=已下架 4=已停售',
  `publish_time` datetime DEFAULT NULL COMMENT '上架时间',
  `shelf_off_time` datetime DEFAULT NULL COMMENT '下架时间',
  `spu_desc` text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
  `package_desc` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '包装描述',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_spu_code` (`tenant_id`,`spu_code`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_tenant_category` (`tenant_id`,`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SPU商品标准单元表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_inquiry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_inquiry` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `inquiry_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '询价单编号',
  `req_id` bigint DEFAULT NULL COMMENT '采购申请单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0已发送 1已报价 2已选中 3未选中 4已过期',
  `send_time` datetime NOT NULL COMMENT '发送时间',
  `quote_deadline` datetime DEFAULT NULL COMMENT '报价截止时间',
  `quoted_time` datetime DEFAULT NULL COMMENT '回价时间',
  `response_hours` decimal(8,2) DEFAULT NULL COMMENT '响应小时数',
  `total_quote_amt` decimal(12,2) DEFAULT NULL COMMENT '总报价',
  `quote_valid_days` int DEFAULT NULL COMMENT '报价有效天数',
  `quote_expire_date` date DEFAULT NULL COMMENT '报价到期日',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '询价备注',
  `supplier_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '供应商备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_inquiry_no` (`tenant_id`,`inquiry_no`),
  KEY `idx_req_id` (`req_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='询价单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_inquiry_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_inquiry_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `inquiry_id` bigint NOT NULL COMMENT '询价单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `inquiry_qty` int NOT NULL COMMENT '询价数量',
  `quoted_price` decimal(10,4) DEFAULT NULL COMMENT '报价单价',
  `quoted_qty` int DEFAULT NULL COMMENT '可供数量',
  `delivery_days` int DEFAULT NULL COMMENT '交货天数',
  `min_order_qty` int DEFAULT NULL COMMENT '最小起订量',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_inquiry_id` (`inquiry_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='询价明细及供应商报价表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `po_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
  `req_id` bigint DEFAULT NULL COMMENT '来源申请单ID',
  `inquiry_id` bigint DEFAULT NULL COMMENT '来源询价单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `total_amount` decimal(12,2) NOT NULL COMMENT '采购总金额',
  `tax_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '税额',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `exchange_rate` decimal(10,6) NOT NULL DEFAULT '1.000000' COMMENT '汇率',
  `payment_type` tinyint NOT NULL DEFAULT '1' COMMENT '付款方式',
  `payment_days` int NOT NULL DEFAULT '0' COMMENT '账期天数',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已付款金额',
  `order_date` date NOT NULL COMMENT '下单日期',
  `expected_date` date DEFAULT NULL COMMENT '期望到货日期',
  `confirmed_date` date DEFAULT NULL COMMENT '承诺到货日期',
  `actual_delivery_date` date DEFAULT NULL COMMENT '实际发货日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1待确认 2已确认 3发货中 4部分到货 5全部到货 6已对账 7已结清 8已取消',
  `logistics_company` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流公司',
  `tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流单号',
  `contract_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同编号',
  `invoice_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发票编号',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_po_no` (`tenant_id`,`po_no`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_expected_date` (`expected_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `spec` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '件' COMMENT '单位',
  `quantity` int NOT NULL COMMENT '采购数量',
  `received_qty` int NOT NULL DEFAULT '0' COMMENT '已收货数量',
  `unit_price` decimal(10,4) NOT NULL COMMENT '单价',
  `amount` decimal(12,2) NOT NULL COMMENT '小计金额',
  `expect_date` date DEFAULT NULL COMMENT '期望到货日期',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_po_id` (`po_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_receipt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_receipt` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `receipt_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货单编号',
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `po_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `receive_date` date NOT NULL COMMENT '收货日期',
  `receiver_id` bigint NOT NULL COMMENT '收货人ID',
  `receiver_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待质检 1入库中 2部分入库 3全部入库 4拒收',
  `total_qty` int NOT NULL DEFAULT '0' COMMENT '收货总数',
  `pass_qty` int NOT NULL DEFAULT '0' COMMENT '合格数量',
  `reject_qty` int NOT NULL DEFAULT '0' COMMENT '拒收数量',
  `is_on_time` tinyint(1) DEFAULT NULL COMMENT '是否准时',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_receipt_no` (`tenant_id`,`receipt_no`),
  KEY `idx_po_id` (`po_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购收货单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_receipt_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_receipt_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `receipt_id` bigint NOT NULL COMMENT '收货单ID',
  `po_item_id` bigint NOT NULL COMMENT '采购明细ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `expected_qty` int NOT NULL COMMENT '应到数量',
  `actual_qty` int NOT NULL COMMENT '实收数量',
  `pass_qty` int NOT NULL DEFAULT '0' COMMENT '合格数量',
  `reject_qty` int NOT NULL DEFAULT '0' COMMENT '拒收数量',
  `reject_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒收原因',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待质检 1已入库 2已退货',
  PRIMARY KEY (`id`),
  KEY `idx_receipt_id` (`receipt_id`),
  KEY `idx_po_item_id` (`po_item_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_requisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_requisition` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `req_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请单编号',
  `req_source` tinyint NOT NULL DEFAULT '3' COMMENT '需求来源：1库存预警 2销售预测 3人工申请',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请标题',
  `warehouse_id` bigint NOT NULL COMMENT '目标收货仓库ID',
  `expect_date` date DEFAULT NULL COMMENT '期望到货日期',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '估算总金额',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级：1紧急 2普通 3低',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1待审批 2通过 3拒绝 4已转采购单 5已取消',
  `approval_level` tinyint NOT NULL DEFAULT '0' COMMENT '审批层级：0自动审批 1采购负责人 2财务负责人',
  `approval_role` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批角色：AUTO/PURCHASE_MANAGER/FINANCE_MANAGER',
  `apply_user_id` bigint NOT NULL COMMENT '申请人ID',
  `apply_user_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请人姓名',
  `apply_time` datetime NOT NULL COMMENT '提交时间',
  `audit_user_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审批时间',
  `audit_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_req_no` (`tenant_id`,`req_no`),
  KEY `idx_tenant_status` (`tenant_id`,`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购申请单主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_requisition_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_requisition_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `req_id` bigint NOT NULL COMMENT '申请单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `quantity` int NOT NULL COMMENT '采购数量',
  `current_stock` int NOT NULL DEFAULT '0' COMMENT '当前库存快照',
  `safety_stock` int NOT NULL DEFAULT '0' COMMENT '安全库存快照',
  `in_transit_qty` int NOT NULL DEFAULT '0' COMMENT '在途库存快照',
  `ref_price` decimal(10,4) DEFAULT NULL COMMENT '参考单价',
  `expect_date` date DEFAULT NULL COMMENT '期望到货日期',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_req_id` (`req_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购申请单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_return`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_return` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `return_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退货单编号',
  `po_id` bigint NOT NULL COMMENT '采购单ID',
  `po_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `return_reason` tinyint NOT NULL COMMENT '退货原因',
  `return_qty` int NOT NULL COMMENT '退货数量',
  `return_amount` decimal(12,2) NOT NULL COMMENT '退货金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1已提交 2供应商确认 3已出库 4已收货 5完成 6拒绝',
  `handle_type` tinyint DEFAULT NULL COMMENT '处理方式',
  `supplier_tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退货物流单号',
  `evidence_urls` json DEFAULT NULL COMMENT '证据附件',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_return_no` (`tenant_id`,`return_no`),
  KEY `idx_po_id` (`po_id`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购退货单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stocktake_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stocktake_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `location_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '库位编码',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `book_qty` int NOT NULL COMMENT '账面数量',
  `actual_qty` int DEFAULT NULL COMMENT '实盘数量',
  `diff_qty` int DEFAULT NULL COMMENT '差异数量',
  `diff_amount` decimal(12,2) DEFAULT NULL COMMENT '差异金额',
  `diff_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '差异原因',
  `is_adjusted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否调整',
  `adjust_time` datetime DEFAULT NULL COMMENT '调整时间',
  `picker_id` bigint DEFAULT NULL COMMENT '盘点人ID',
  `pick_time` datetime DEFAULT NULL COMMENT '盘点时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stocktake_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stocktake_task` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `task_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '盘点任务编号',
  `task_type` tinyint NOT NULL COMMENT '盘点类型',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `task_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `plan_date` date NOT NULL COMMENT '计划日期',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0待开始 1进行中 2待审核 3完成 4取消',
  `total_sku_count` int NOT NULL DEFAULT '0' COMMENT 'SKU种数',
  `diff_sku_count` int NOT NULL DEFAULT '0' COMMENT '差异SKU数',
  `profit_qty` int NOT NULL DEFAULT '0' COMMENT '盘盈数量',
  `loss_qty` int NOT NULL DEFAULT '0' COMMENT '盘亏数量',
  `profit_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '盘盈金额',
  `loss_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '盘亏金额',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_task_no` (`tenant_id`,`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存盘点任务主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL COMMENT '租户ID，多租户隔离核心字段',
  `supplier_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商编码，系统自动生成，格式：SUP-YYYYMMDD-XXXX',
  `supplier_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商公司名称',
  `supplier_type` tinyint NOT NULL COMMENT '供应商类型：1=工厂供应商 2=贸易商 3=物流服务商',
  `category_ids` json DEFAULT NULL COMMENT '供货品类ID列表',
  `province` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在省份',
  `city` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在城市',
  `address` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `website` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司官网URL',
  `company_size` tinyint DEFAULT NULL COMMENT '公司规模：1=50人以下 2=50-200人 3=200-500人 4=500人以上',
  `founded_year` smallint DEFAULT NULL COMMENT '成立年份',
  `contact_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主联系人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主联系人手机号',
  `contact_email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主联系人邮箱',
  `contact_wechat` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人微信号',
  `contact_whatsapp` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人WhatsApp账号',
  `bank_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户银行名称',
  `bank_account` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行账号，加密存储',
  `bank_account_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '银行开户名',
  `tax_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_type` tinyint DEFAULT NULL COMMENT '开票类型：1=专票 2=普票 3=收据',
  `moq` int DEFAULT NULL COMMENT '最小起订量',
  `lead_time_days` int DEFAULT NULL COMMENT '交货周期，单位天',
  `monthly_capacity` int DEFAULT NULL COMMENT '月最大供货量',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '结算货币',
  `payment_days` int NOT NULL DEFAULT '0' COMMENT '账期天数',
  `grade` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'C' COMMENT '综合评级：S/A/B/C',
  `score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '最新综合评分',
  `last_score_month` varchar(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后评分月份，格式YYYYMM',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=草稿 1=待审核 2=已通过 3=已拒绝 4=已停用',
  `audit_user_id` bigint DEFAULT NULL COMMENT '最后审核人用户ID',
  `audit_time` datetime DEFAULT NULL COMMENT '最后审核时间',
  `audit_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后审核意见',
  `portal_user_id` bigint DEFAULT NULL COMMENT '供应商Portal账号用户ID',
  `portal_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Portal是否开通：0=否 1=是',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内部备注',
  `tags` json DEFAULT NULL COMMENT '供应商标签列表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '最后修改人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_supplier_code` (`tenant_id`,`supplier_code`) COMMENT '同一租户内供应商编码唯一',
  UNIQUE KEY `uk_tenant_contact_email` (`tenant_id`,`contact_email`) COMMENT '同一租户内Portal登录邮箱唯一',
  UNIQUE KEY `uk_portal_user_id` (`portal_user_id`) COMMENT 'Portal用户ID唯一，防止并发重复开通',
  KEY `idx_tenant_status` (`tenant_id`,`status`) COMMENT '按租户和状态筛选',
  KEY `idx_tenant_grade` (`tenant_id`,`grade`) COMMENT '按租户和评级筛选',
  KEY `idx_tenant_type` (`tenant_id`,`supplier_type`) COMMENT '按租户和类型筛选',
  KEY `idx_create_time` (`create_time`) COMMENT '按创建时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_audit_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `from_status` tinyint DEFAULT NULL COMMENT '变更前状态',
  `to_status` tinyint NOT NULL COMMENT '变更后状态',
  `action` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作动作',
  `audit_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见或操作备注',
  `operator_id` bigint NOT NULL COMMENT '操作人用户ID',
  `operator_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询审核历史',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商审核操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_cert` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `cert_type` tinyint NOT NULL COMMENT '资质类型：1=营业执照 2=质检报告 3=产品认证 4=银行证明 5=其他',
  `cert_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资质名称',
  `file_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名称',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储URL',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小，单位字节',
  `file_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件MIME类型',
  `issue_date` date DEFAULT NULL COMMENT '颁发日期',
  `expire_date` date DEFAULT NULL COMMENT '有效期截止日期',
  `is_expired` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已过期：0=有效 1=已过期',
  `cert_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证书编号',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `create_by` bigint DEFAULT NULL COMMENT '上传人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询资质',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_expire_date` (`expire_date`) COMMENT '按到期日期查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商资质文件表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_contact` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `contact_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人姓名',
  `position` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职位',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `wechat` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信号',
  `whatsapp` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'WhatsApp账号',
  `department` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在部门',
  `is_primary` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否主联系人：1=是 0=否',
  `contact_type` tinyint NOT NULL DEFAULT '1' COMMENT '联系人类型：1=业务 2=财务 3=技术 4=紧急',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_purchase_arrival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_purchase_arrival` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `score_month` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分月份，格式YYYYMM',
  `purchase_order_id` bigint NOT NULL COMMENT '采购订单ID',
  `promised_arrival_date` date NOT NULL COMMENT '承诺到货日期',
  `actual_arrival_date` date NOT NULL COMMENT '实际到货日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_order_id` (`purchase_order_id`) COMMENT '采购订单到货记录唯一',
  KEY `idx_supplier_month` (`supplier_id`,`score_month`) COMMENT '按供应商和月份统计到货',
  KEY `idx_tenant_month` (`tenant_id`,`score_month`) COMMENT '按租户和月份统计到货'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商采购到货绩效事实表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_purchase_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_purchase_price` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `score_month` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分月份，格式YYYYMM',
  `category_id` bigint NOT NULL COMMENT '采购品类ID',
  `purchase_id` bigint NOT NULL COMMENT '采购明细ID',
  `unit_price` decimal(12,4) NOT NULL COMMENT '采购单价',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `purchase_time` datetime NOT NULL COMMENT '采购时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_id` (`purchase_id`) COMMENT '采购明细价格记录唯一',
  KEY `idx_supplier_month` (`supplier_id`,`score_month`) COMMENT '按供应商和月份统计价格',
  KEY `idx_tenant_month` (`tenant_id`,`score_month`) COMMENT '按租户和月份统计价格',
  KEY `idx_category_month` (`tenant_id`,`category_id`,`score_month`) COMMENT '按品类和月份统计市场均价'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商采购价格绩效事实表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_quality_inspection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_quality_inspection` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `score_month` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分月份，格式YYYYMM',
  `inspection_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '质检单号',
  `inspection_result` tinyint NOT NULL COMMENT '质检结果：1=合格 0=不合格',
  `inspection_time` datetime NOT NULL COMMENT '质检时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspection_no` (`inspection_no`) COMMENT '质检单号唯一',
  KEY `idx_supplier_month` (`supplier_id`,`score_month`) COMMENT '按供应商和月份统计质检',
  KEY `idx_tenant_month` (`tenant_id`,`score_month`) COMMENT '按租户和月份统计质检'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商入库质检绩效事实表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_quote_response`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_quote_response` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `score_month` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分月份，格式YYYYMM',
  `inquiry_id` bigint NOT NULL COMMENT '询价单ID',
  `inquiry_time` datetime NOT NULL COMMENT '发起询价时间',
  `quote_time` datetime DEFAULT NULL COMMENT '供应商回价时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inquiry_supplier` (`inquiry_id`,`supplier_id`) COMMENT '同一询价单供应商响应唯一',
  KEY `idx_supplier_month` (`supplier_id`,`score_month`) COMMENT '按供应商和月份统计响应',
  KEY `idx_tenant_month` (`tenant_id`,`score_month`) COMMENT '按租户和月份统计响应'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商询价响应绩效事实表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_risk_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_risk_event` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `category_id` bigint NOT NULL COMMENT '品类ID',
  `risk_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险类型：SUPPLIER_COUNT_LOW=供应商数量不足 ALL_SUPPLIER_GRADE_LOW=供应商评级偏低',
  `risk_level` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH',
  `risk_reason` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险原因',
  `system_suggestion` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '系统建议，仅供租户决策参考',
  `supplier_count` int NOT NULL DEFAULT '0' COMMENT '风险发生时可用供应商数量',
  `best_grade` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'C' COMMENT '风险发生时该品类最高供应商评级',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=处理中 2=已解除',
  `first_detected_date` date NOT NULL COMMENT '首次发现日期',
  `last_detected_date` date NOT NULL COMMENT '最近发现日期',
  `resolved_time` datetime DEFAULT NULL COMMENT '风险解除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_category_risk_status` (`tenant_id`,`category_id`,`risk_type`,`status`,`is_deleted`) COMMENT '按品类风险状态查询',
  KEY `idx_tenant_status` (`tenant_id`,`status`) COMMENT '按租户和状态查询风险',
  KEY `idx_category_id` (`category_id`) COMMENT '按品类查询风险'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商品类风险事件表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_score_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_score_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '关联供应商ID',
  `score_month` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分月份，格式YYYYMM',
  `total_orders` int NOT NULL DEFAULT '0' COMMENT '本月采购订单总数',
  `delivered_on_time` int NOT NULL DEFAULT '0' COMMENT '准时到货订单数',
  `quality_passed` int NOT NULL DEFAULT '0' COMMENT '质检合格批次数',
  `quality_total` int NOT NULL DEFAULT '0' COMMENT '质检总批次数',
  `response_hours_avg` decimal(8,2) DEFAULT NULL COMMENT '平均响应时长，单位小时',
  `price_comparison` decimal(8,4) DEFAULT NULL COMMENT '价格竞争力系数',
  `delivery_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '准时交货评分',
  `quality_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '质量合格评分',
  `response_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '响应速度评分',
  `price_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '价格竞争力评分',
  `total_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '综合评分',
  `grade` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '本月评级：S/A/B/C',
  `grade_changed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '评级是否变化：0=否 1=是',
  `prev_grade` char(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上月评级',
  `calc_remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '计算说明',
  `calc_time` datetime NOT NULL COMMENT '计算时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_month` (`supplier_id`,`score_month`) COMMENT '同一供应商同一月份唯一',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_score_month` (`score_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商月度评分记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_tenant_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_tenant_config` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `config_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `config_desc` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置说明',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_config_key` (`tenant_id`,`config_key`) COMMENT '同一租户配置键唯一',
  KEY `idx_config_key` (`config_key`) COMMENT '按配置键查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商租户级策略配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_watchlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_watchlist` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `current_grade` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '进入观察名单时的评级：S/A/B/C',
  `current_score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '进入观察名单时的评分',
  `watch_reason` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '观察原因',
  `system_suggestion` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '系统建议，仅供租户决策参考',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=观察中 2=已解除',
  `watch_time` datetime NOT NULL COMMENT '进入观察名单时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_watch_status` (`supplier_id`,`status`,`is_deleted`) COMMENT '按供应商和观察状态查询',
  KEY `idx_tenant_status` (`tenant_id`,`status`) COMMENT '按租户和状态查询观察名单',
  KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询观察记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商重点观察名单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_audit_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人用户名（冗余存储，防止用户删除后追溯失败）',
  `module` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块，如：供应商管理、采购管理',
  `action` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作动作，如：新增供应商、审核通过、删除采购单',
  `method` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方法（POST/PUT/DELETE）+ 接口路径',
  `request_params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数JSON（敏感字段脱敏后记录）',
  `response_code` int DEFAULT NULL COMMENT '响应业务码',
  `ip_address` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作来源IP地址',
  `user_agent` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器User-Agent信息',
  `duration_ms` int DEFAULT NULL COMMENT '接口耗时（毫秒）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '操作结果：1=成功 0=失败',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作失败时的错误信息',
  `operate_time` datetime NOT NULL COMMENT '操作发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_time` (`tenant_id`,`operate_time`) COMMENT '按租户+时间查询审计日志',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表（只增不改不删）';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `dict_type_id` bigint NOT NULL COMMENT '关联字典类型ID',
  `dict_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码（冗余，方便查询）',
  `item_value` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典项的值，如：1',
  `item_label` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典项的显示文字，如：工厂供应商',
  `item_label_en` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '英文显示文字（国际化）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序，数字越小越靠前',
  `css_class` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样式类名，用于前端标签着色，如 success/warning/danger',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_dict_code` (`dict_code`),
  KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `dict_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称，如：供应商类型',
  `dict_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典编码，如：supplier_type，代码中用此值查询',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_event_outbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_event_outbox` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID，平台事件为0',
  `event_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件全局唯一ID',
  `event_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型，如 SYSTEM_NOTIFICATION',
  `source_service` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件来源服务',
  `biz_type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务类型，如 SUPPLIER_RISK',
  `biz_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务ID',
  `idempotent_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务幂等键',
  `payload` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件载荷JSON',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0=待分发 1=已分发 2=失败 3=忽略',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `error_msg` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近一次错误信息',
  `occurred_time` datetime NOT NULL COMMENT '事件发生时间',
  `dispatch_time` datetime DEFAULT NULL COMMENT '分发成功时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_id` (`event_id`) COMMENT '事件ID唯一兜底',
  UNIQUE KEY `uk_tenant_idempotent` (`tenant_id`,`idempotent_key`) COMMENT '租户内业务幂等唯一兜底',
  KEY `idx_status_retry` (`status`,`retry_count`,`update_time`) COMMENT '按状态扫描待重试事件',
  KEY `idx_biz` (`biz_type`,`biz_id`) COMMENT '按业务定位事件'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统可靠事件发件箱表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID，0表示顶级菜单',
  `menu_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单/按钮名称',
  `menu_type` tinyint NOT NULL COMMENT '类型：1=目录 2=菜单页面 3=操作按钮',
  `permission` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识，如 srm:supplier:add，按钮级权限用此字段',
  `path` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端路由路径，如 /srm/supplier',
  `component` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端组件路径，如 srm/supplier/index',
  `icon` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单图标，使用Element Plus图标名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '同级菜单排序',
  `is_visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否在菜单中显示：1=显示 0=隐藏',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID，平台消息为0',
  `receiver_id` bigint NOT NULL DEFAULT '0' COMMENT '接收用户ID，按角色发送时为0',
  `receiver_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '接收类型：USER=用户 ROLE=角色 SYSTEM=系统广播',
  `receiver_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接收标识，角色消息存角色编码',
  `title` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `biz_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型，如 SUPPLIER_AUDIT',
  `biz_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务ID',
  `priority` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：NORMAL=普通 HIGH=重要 URGENT=紧急',
  `read_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '读取状态：0=未读 1=已读',
  `read_time` datetime DEFAULT NULL COMMENT '读取时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_receiver` (`tenant_id`,`receiver_type`,`receiver_id`,`read_status`) COMMENT '按租户与接收人查询消息',
  KEY `idx_receiver_key` (`tenant_id`,`receiver_type`,`receiver_key`,`read_status`) COMMENT '按租户与角色查询消息',
  KEY `idx_biz` (`biz_type`,`biz_id`) COMMENT '按业务定位消息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统站内信消息表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_plan_feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_plan_feature` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `plan_type` tinyint NOT NULL COMMENT '套餐类型：1=基础版 2=专业版 3=企业版',
  `feature_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能编码，与权限标识前缀对应',
  `feature_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '功能名称',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否开启：0=关闭 1=开启',
  `limit_value` int DEFAULT NULL COMMENT '数量限制，NULL表示无限制',
  `limit_unit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '限制单位',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_feature` (`plan_type`,`feature_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐功能开关配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID，0表示平台内置角色',
  `role_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称，如：采购专员',
  `role_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码，如：ROLE_PURCHASE，用于代码判断',
  `role_type` tinyint NOT NULL DEFAULT '1' COMMENT '角色类型：1=自定义角色 2=系统内置角色（不可删除）',
  `data_scope` tinyint NOT NULL DEFAULT '1' COMMENT '数据权限范围：1=全部数据 2=本部门数据 3=仅本人数据',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序序号，数字越小越靠前',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `remark` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_role_code` (`tenant_id`,`role_code`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单/按钮ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `create_by` bigint DEFAULT NULL COMMENT '分配人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) COMMENT '防止重复分配',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant` (
  `id` bigint unsigned NOT NULL COMMENT '租户ID，雪花算法生成',
  `tenant_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '租户编码，格式 TC-YYYYMMDD-XXXX，系统自动生成',
  `company_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公司名称',
  `contact_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '负责人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '负责人手机号',
  `contact_email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '负责人邮箱，同时作为登录账号',
  `plan_type` tinyint NOT NULL DEFAULT '1' COMMENT '套餐类型：1=基础版 2=专业版 3=企业版',
  `plan_start_time` datetime DEFAULT NULL COMMENT '套餐开始时间',
  `plan_end_time` datetime DEFAULT NULL COMMENT '套餐到期时间，NULL表示永久有效',
  `trial_end_time` datetime DEFAULT NULL COMMENT '试用期到期时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=正常 2=试用中 3=已到期 4=已注销',
  `max_supplier` int NOT NULL DEFAULT '20' COMMENT '套餐允许的最大供应商数量',
  `max_warehouse` int NOT NULL DEFAULT '1' COMMENT '套餐允许的最大仓库数量',
  `max_monthly_order` int NOT NULL DEFAULT '500' COMMENT '套餐允许的月最大订单量',
  `max_platform` int NOT NULL DEFAULT '1' COMMENT '套餐允许对接的最大平台数量',
  `admin_user_id` bigint DEFAULT NULL COMMENT '租户管理员的用户ID',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人（平台管理员ID）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已注销',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  UNIQUE KEY `uk_contact_email` (`contact_email`),
  KEY `idx_status` (`status`),
  KEY `idx_plan_end_time` (`plan_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID，雪花算法生成',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID，平台管理员为0',
  `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名，同一租户内唯一',
  `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码，BCrypt加密存储',
  `real_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像图片URL',
  `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱地址，用于通知和找回密码',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号，加密存储',
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型：1=普通用户 2=租户管理员 3=供应商用户 9=超级管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=正常 2=锁定',
  `login_fail_count` tinyint NOT NULL DEFAULT '0' COMMENT '连续登录失败次数，超过5次锁定账号',
  `lock_time` datetime DEFAULT NULL COMMENT '账号锁定时间',
  `lock_until_time` datetime DEFAULT NULL COMMENT '账号锁定截止时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人用户ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=正常 1=已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_username` (`tenant_id`,`username`) COMMENT '同一租户内用户名唯一',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `create_by` bigint DEFAULT NULL COMMENT '分配人（管理员ID）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) COMMENT '防止重复分配同一角色',
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tax_vat_rate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tax_vat_rate` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '国家代码',
  `country_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '国家名称',
  `vat_rate` decimal(6,4) NOT NULL COMMENT 'VAT税率',
  `threshold_value` decimal(12,2) DEFAULT NULL COMMENT '免税门槛',
  `currency` char(3) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_country_code` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VAT税率配置表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `transfer_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer_order` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `transfer_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调拨单编号',
  `from_warehouse_id` bigint NOT NULL COMMENT '调出仓库ID',
  `from_warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调出仓库名称',
  `to_warehouse_id` bigint NOT NULL COMMENT '调入仓库ID',
  `to_warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调入仓库名称',
  `transfer_reason` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '调拨原因',
  `logistics_company` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流公司',
  `tracking_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流单号',
  `plan_date` date DEFAULT NULL COMMENT '计划日期',
  `ship_date` date DEFAULT NULL COMMENT '发货日期',
  `arrive_date` date DEFAULT NULL COMMENT '到货日期',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0草稿 1已审核 2调出中 3已到达 4完成 5取消',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_transfer_no` (`tenant_id`,`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库调拨单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `transfer_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transfer_order_item` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `transfer_qty` int NOT NULL COMMENT '计划调拨数量',
  `shipped_qty` int NOT NULL DEFAULT '0' COMMENT '已发数量',
  `received_qty` int NOT NULL DEFAULT '0' COMMENT '已收数量',
  `from_location_id` bigint DEFAULT NULL COMMENT '调出库位ID',
  `to_location_id` bigint DEFAULT NULL COMMENT '调入库位ID',
  PRIMARY KEY (`id`),
  KEY `idx_transfer_id` (`transfer_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调拨单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `undo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undo_log` (
  `branch_id` bigint NOT NULL COMMENT '分支事务ID',
  `xid` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '全局事务ID',
  `context` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上下文',
  `rollback_info` longblob NOT NULL COMMENT '回滚镜像',
  `log_status` int NOT NULL COMMENT '状态：0正常 1全局已完成',
  `log_created` datetime(6) NOT NULL COMMENT '创建时间',
  `log_modified` datetime(6) NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seata AT undo log';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
  `warehouse_type` tinyint NOT NULL COMMENT '仓库类型：1国内备货仓 2FBA仓 3海外自营仓 4第三方仓 5保税仓',
  `country_code` char(2) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CN' COMMENT '国家ISO代码',
  `country_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家名称',
  `province` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省/州',
  `city` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `address` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `zip_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮政编码',
  `contact_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人姓名',
  `contact_phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人电话',
  `contact_email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人邮箱',
  `area_sqm` decimal(10,2) DEFAULT NULL COMMENT '仓库面积',
  `total_locations` int NOT NULL DEFAULT '0' COMMENT '总库位数',
  `used_locations` int NOT NULL DEFAULT '0' COMMENT '已使用库位数',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认仓',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1正常 2盘点中',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_warehouse_code` (`tenant_id`,`warehouse_code`),
  KEY `idx_tenant_status` (`tenant_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库主表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `warehouse_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_inventory` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '现有库存',
  `locked_quantity` int NOT NULL DEFAULT '0' COMMENT '锁定库存',
  `available_quantity` int GENERATED ALWAYS AS ((`quantity` - `locked_quantity`)) STORED COMMENT '可用库存',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_sku_location` (`tenant_id`,`warehouse_id`,`location_id`,`sku_id`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库库存表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `warehouse_inventory_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_inventory_log` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_id` bigint DEFAULT NULL COMMENT '库位ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU名称',
  `biz_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型：PURCHASE_IN/RETURN_OUT',
  `biz_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
  `change_qty` int NOT NULL COMMENT '库存变化数量',
  `before_qty` int NOT NULL COMMENT '变更前库存',
  `after_qty` int NOT NULL COMMENT '变更后库存',
  `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_biz_no` (`biz_no`),
  KEY `idx_sku_id` (`sku_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `warehouse_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_location` (
  `id` bigint unsigned NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `location_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '库位编码',
  `zone` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区域',
  `row_no` smallint NOT NULL COMMENT '排号',
  `column_no` smallint NOT NULL COMMENT '列号',
  `floor_no` smallint NOT NULL COMMENT '层号',
  `location_type` tinyint NOT NULL DEFAULT '1' COMMENT '库位类型',
  `max_weight_kg` decimal(8,2) DEFAULT NULL COMMENT '最大承重',
  `max_volume_l` decimal(10,2) DEFAULT NULL COMMENT '最大容积',
  `is_occupied` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否占用',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0停用 1正常 2锁定',
  `remark` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_location_code` (`warehouse_id`,`location_code`),
  KEY `idx_warehouse_zone` (`warehouse_id`,`zone`),
  KEY `idx_is_occupied` (`warehouse_id`,`is_occupied`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库库位表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

SET FOREIGN_KEY_CHECKS = 1;
