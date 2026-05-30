USE `supplychain_dev`;

-- Day07 财务结算系统与 BI 分析模块表结构。
CREATE TABLE IF NOT EXISTS `finance_exchange_rate`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `rate_date` DATE NOT NULL COMMENT '汇率日期',
    `currency` CHAR(3) NOT NULL COMMENT '外币货币代码',
    `rate_to_cny` DECIMAL(12, 6) NOT NULL COMMENT '1单位外币兑换人民币汇率',
    `rate_source` VARCHAR(32) NOT NULL DEFAULT 'LOCAL_FALLBACK' COMMENT '汇率来源',
    `is_official` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否官方汇率',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_currency` (`rate_date`, `currency`),
    KEY `idx_currency_date` (`currency`, `rate_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '汇率表';

CREATE TABLE IF NOT EXISTS `finance_platform_bill`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `bill_no` VARCHAR(32) NOT NULL COMMENT '内部账单号',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台',
    `store_id` VARCHAR(64) NOT NULL COMMENT '店铺ID',
    `store_name` VARCHAR(128) NULL COMMENT '店铺名称',
    `platform_bill_id` VARCHAR(128) NULL COMMENT '平台账单ID',
    `settlement_start` DATE NOT NULL COMMENT '结算开始日期',
    `settlement_end` DATE NOT NULL COMMENT '结算结束日期',
    `currency` CHAR(3) NOT NULL COMMENT '账单货币',
    `total_sales` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '销售收入总额',
    `total_refund` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '退款总额',
    `referral_fee` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '平台佣金',
    `fba_fee` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT 'FBA费用',
    `storage_fee` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '仓储费',
    `advertising_fee` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '广告费',
    `other_fee` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '其他费用',
    `net_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '净到账金额',
    `cny_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '折合人民币净到账金额',
    `exchange_rate` DECIMAL(10, 6) NOT NULL DEFAULT 1 COMMENT '结算汇率',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待解析 1解析中 2已解析 3对账完成 4有差异',
    `source_file_url` VARCHAR(512) NULL COMMENT '原始账单文件地址',
    `import_time` DATETIME NULL COMMENT '导入时间',
    `import_user_id` BIGINT NULL COMMENT '导入操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_bill_no` (`tenant_id`, `bill_no`),
    UNIQUE KEY `uk_platform_bill_id` (`tenant_id`, `platform`, `store_id`, `platform_bill_id`),
    KEY `idx_tenant_platform_period` (`tenant_id`, `platform`, `settlement_start`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '平台结算账单主表';

CREATE TABLE IF NOT EXISTS `finance_bill_item`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `bill_id` BIGINT NOT NULL COMMENT '账单ID',
    `item_type` VARCHAR(64) NOT NULL COMMENT '明细类型',
    `order_no` VARCHAR(128) NULL COMMENT '平台订单号',
    `sku_id` BIGINT NULL COMMENT 'SKU ID',
    `platform_sku` VARCHAR(128) NULL COMMENT '平台SKU',
    `amount` DECIMAL(12, 4) NOT NULL COMMENT '金额',
    `currency` CHAR(3) NOT NULL COMMENT '货币',
    `description` VARCHAR(512) NULL COMMENT '说明',
    `transaction_date` DATE NULL COMMENT '交易日期',
    `is_matched` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匹配内部订单',
    `match_order_id` BIGINT NULL COMMENT '匹配订单ID',
    PRIMARY KEY (`id`),
    KEY `idx_bill_id` (`bill_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_item_type` (`item_type`),
    KEY `idx_is_matched` (`is_matched`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单明细表';

CREATE TABLE IF NOT EXISTS `finance_profit_snapshot`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `snapshot_type` TINYINT NOT NULL COMMENT '快照类型：1日 2月',
    `snapshot_date` DATE NOT NULL COMMENT '快照日期',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `platform` VARCHAR(32) NOT NULL COMMENT '平台',
    `store_id` VARCHAR(64) NOT NULL COMMENT '店铺ID',
    `country_code` CHAR(2) NULL COMMENT '国家代码',
    `currency` CHAR(3) NOT NULL COMMENT '原始货币',
    `exchange_rate` DECIMAL(10, 6) NOT NULL COMMENT '汇率',
    `order_count` INT NOT NULL DEFAULT 0 COMMENT '订单数',
    `sales_qty` INT NOT NULL DEFAULT 0 COMMENT '销售数量',
    `gross_revenue` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '原币收入',
    `gross_revenue_cny` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '人民币收入',
    `purchase_cost` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '采购成本',
    `logistics_fee` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '物流费',
    `platform_fee` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '平台费',
    `fba_storage_fee` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '仓储费',
    `advertising_fee` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '广告费',
    `refund_loss` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '退款损失',
    `vat_fee` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT 'VAT费用',
    `other_cost` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '其他成本',
    `total_cost` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '总成本',
    `gross_profit` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '毛利润',
    `net_profit` DECIMAL(14, 4) NOT NULL DEFAULT 0 COMMENT '净利润',
    `gross_margin` DECIMAL(8, 4) NOT NULL DEFAULT 0 COMMENT '毛利率',
    `net_margin` DECIMAL(8, 4) NOT NULL DEFAULT 0 COMMENT '净利率',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_snapshot` (`tenant_id`, `snapshot_type`, `snapshot_date`, `sku_id`, `platform`, `store_id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_snapshot_date` (`snapshot_date`),
    KEY `idx_platform_date` (`platform`, `snapshot_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SKU利润快照表';

CREATE TABLE IF NOT EXISTS `finance_vat_record`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `country_code` CHAR(2) NOT NULL COMMENT '申报国家',
    `vat_no` VARCHAR(32) NULL COMMENT 'VAT登记号',
    `period` VARCHAR(7) NOT NULL COMMENT '申报期',
    `taxable_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '应税销售额',
    `vat_rate` DECIMAL(6, 4) NOT NULL COMMENT 'VAT税率',
    `vat_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '应缴VAT',
    `local_currency` CHAR(3) NOT NULL COMMENT '当地货币',
    `cny_amount` DECIMAL(14, 2) NOT NULL DEFAULT 0 COMMENT '折合人民币',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待申报 1已申报 2已缴纳 3异常',
    `file_url` VARCHAR(512) NULL COMMENT '申报文件地址',
    `declare_time` DATETIME NULL COMMENT '申报时间',
    `pay_time` DATETIME NULL COMMENT '缴纳时间',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_country_period` (`tenant_id`, `country_code`, `period`),
    KEY `idx_status` (`status`),
    KEY `idx_period` (`period`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'VAT申报记录表';

CREATE TABLE IF NOT EXISTS `finance_cash_flow`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `flow_date` DATE NOT NULL COMMENT '流水日期',
    `flow_type` TINYINT NOT NULL COMMENT '类型：1收入 2支出',
    `source_type` VARCHAR(32) NOT NULL COMMENT '来源类型',
    `source_id` BIGINT NULL COMMENT '来源单据ID',
    `source_no` VARCHAR(64) NULL COMMENT '来源单据编号',
    `amount_cny` DECIMAL(14, 2) NOT NULL COMMENT '人民币金额',
    `amount_origin` DECIMAL(14, 2) NULL COMMENT '原币金额',
    `currency` CHAR(3) NULL COMMENT '原币货币',
    `exchange_rate` DECIMAL(10, 6) NULL COMMENT '汇率',
    `remark` VARCHAR(256) NULL COMMENT '说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_source` (`tenant_id`, `source_type`, `source_id`),
    KEY `idx_tenant_date` (`tenant_id`, `flow_date`),
    KEY `idx_source_type` (`source_type`),
    KEY `idx_flow_type` (`flow_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资金流水记录表';

CREATE TABLE IF NOT EXISTS `bi_kpi_threshold`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `kpi_code` VARCHAR(64) NOT NULL COMMENT 'KPI编码',
    `kpi_name` VARCHAR(128) NOT NULL COMMENT 'KPI名称',
    `warning_value` DECIMAL(12, 4) NOT NULL COMMENT '预警阈值',
    `danger_value` DECIMAL(12, 4) NOT NULL COMMENT '危险阈值',
    `compare_type` TINYINT NOT NULL COMMENT '比较类型：1小于触发 2大于触发',
    `notify_roles` JSON NULL COMMENT '通知角色列表',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_kpi` (`tenant_id`, `kpi_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'KPI预警阈值配置表';
