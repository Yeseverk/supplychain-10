USE `supplychain_dev`;

-- Day04 WMS 完整表结构：仓库、库位、库存、流水、入库、出库、盘点、调拨与预警事件。
CREATE TABLE IF NOT EXISTS `warehouse`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_code` VARCHAR(32) NOT NULL COMMENT '仓库编码',
    `warehouse_name` VARCHAR(64) NOT NULL COMMENT '仓库名称',
    `warehouse_type` TINYINT NOT NULL COMMENT '仓库类型：1国内备货仓 2FBA仓 3海外自营仓 4第三方仓 5保税仓',
    `country_code` CHAR(2) NOT NULL DEFAULT 'CN' COMMENT '国家ISO代码',
    `country_name` VARCHAR(32) NULL COMMENT '国家名称',
    `province` VARCHAR(32) NULL COMMENT '省/州',
    `city` VARCHAR(32) NULL COMMENT '城市',
    `address` VARCHAR(256) NULL COMMENT '详细地址',
    `zip_code` VARCHAR(16) NULL COMMENT '邮政编码',
    `contact_name` VARCHAR(64) NULL COMMENT '负责人姓名',
    `contact_phone` VARCHAR(32) NULL COMMENT '负责人电话',
    `contact_email` VARCHAR(128) NULL COMMENT '负责人邮箱',
    `area_sqm` DECIMAL(10, 2) NULL COMMENT '仓库面积',
    `total_locations` INT NOT NULL DEFAULT 0 COMMENT '总库位数',
    `used_locations` INT NOT NULL DEFAULT 0 COMMENT '已使用库位数',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认仓',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1正常 2盘点中',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_warehouse_code` (`tenant_id`, `warehouse_code`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库主表';

CREATE TABLE IF NOT EXISTS `warehouse_location`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_code` VARCHAR(32) NOT NULL COMMENT '库位编码',
    `zone` VARCHAR(8) NOT NULL COMMENT '区域',
    `row_no` SMALLINT NOT NULL COMMENT '排号',
    `column_no` SMALLINT NOT NULL COMMENT '列号',
    `floor_no` SMALLINT NOT NULL COMMENT '层号',
    `location_type` TINYINT NOT NULL DEFAULT 1 COMMENT '库位类型',
    `max_weight_kg` DECIMAL(8, 2) NULL COMMENT '最大承重',
    `max_volume_l` DECIMAL(10, 2) NULL COMMENT '最大容积',
    `is_occupied` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否占用',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0停用 1正常 2锁定',
    `remark` VARCHAR(128) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_location_code` (`warehouse_id`, `location_code`),
    KEY `idx_warehouse_zone` (`warehouse_id`, `zone`),
    KEY `idx_is_occupied` (`warehouse_id`, `is_occupied`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库库位表';

CREATE TABLE IF NOT EXISTS `inventory`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NULL COMMENT '库位ID，NULL代表仓库汇总',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NULL COMMENT 'SKU名称',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '实物库存',
    `frozen_qty` INT NOT NULL DEFAULT 0 COMMENT '冻结库存',
    `in_transit_qty` INT NOT NULL DEFAULT 0 COMMENT '在途库存',
    `defective_qty` INT NOT NULL DEFAULT 0 COMMENT '不良品库存',
    `reserved_qty` INT NOT NULL DEFAULT 0 COMMENT '预留库存',
    `safety_stock` INT NOT NULL DEFAULT 0 COMMENT '安全库存',
    `max_stock` INT NULL COMMENT '最大库存',
    `reorder_point` INT NULL COMMENT '补货点',
    `avg_cost` DECIMAL(12, 4) NOT NULL DEFAULT 0 COMMENT '加权平均成本',
    `total_cost` DECIMAL(16, 4) NOT NULL DEFAULT 0 COMMENT '总成本',
    `last_inbound_time` DATETIME NULL COMMENT '最后入库时间',
    `last_outbound_time` DATETIME NULL COMMENT '最后出库时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_location_sku` (`warehouse_id`, `location_id`, `sku_id`),
    KEY `idx_tenant_sku` (`tenant_id`, `sku_id`),
    KEY `idx_warehouse_sku` (`warehouse_id`, `sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存主表';

CREATE TABLE IF NOT EXISTS `inventory_log`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `log_type` TINYINT NOT NULL COMMENT '流水类型：1采购入库 2销售出库 3调拨入库 4调拨出库 5盘盈 6盘亏 7退货入库 8报损 11冻结 12解冻',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NULL COMMENT '库位ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NULL COMMENT 'SKU名称',
    `change_qty` INT NOT NULL COMMENT '变动数量',
    `before_qty` INT NOT NULL COMMENT '变动前数量',
    `after_qty` INT NOT NULL COMMENT '变动后数量',
    `ref_type` VARCHAR(32) NULL COMMENT '关联单据类型',
    `ref_no` VARCHAR(64) NULL COMMENT '关联单号',
    `ref_id` BIGINT NULL COMMENT '关联单据ID',
    `batch_no` VARCHAR(64) NULL COMMENT '批次号',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
    `operate_time` DATETIME NOT NULL COMMENT '操作时间',
    `remark` VARCHAR(256) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_sku_time` (`tenant_id`, `sku_id`, `operate_time`),
    KEY `idx_ref_no` (`ref_no`),
    KEY `idx_log_type` (`log_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存流水记录表';

CREATE TABLE IF NOT EXISTS `inbound_order`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `inbound_no` VARCHAR(32) NOT NULL COMMENT '入库单号',
    `inbound_type` TINYINT NOT NULL COMMENT '入库类型',
    `warehouse_id` BIGINT NOT NULL COMMENT '目标仓库ID',
    `warehouse_name` VARCHAR(64) NOT NULL COMMENT '仓库名称',
    `ref_type` VARCHAR(32) NULL COMMENT '来源类型',
    `ref_id` BIGINT NULL COMMENT '来源单据ID',
    `ref_no` VARCHAR(32) NULL COMMENT '来源单号',
    `expected_date` DATE NULL COMMENT '预计到货日期',
    `actual_date` DATE NULL COMMENT '实际入库日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待入库 1入库中 2完成 3取消',
    `total_sku_count` INT NOT NULL DEFAULT 0 COMMENT 'SKU种数',
    `total_qty` INT NOT NULL DEFAULT 0 COMMENT '计划数量',
    `actual_qty` INT NOT NULL DEFAULT 0 COMMENT '实际数量',
    `operator_id` BIGINT NULL COMMENT '操作人ID',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_inbound_no` (`tenant_id`, `inbound_no`),
    KEY `idx_warehouse_status` (`warehouse_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库单主表';

CREATE TABLE IF NOT EXISTS `inbound_order_item`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `inbound_id` BIGINT NOT NULL COMMENT '入库单ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `expected_qty` INT NOT NULL COMMENT '计划入库数量',
    `actual_qty` INT NOT NULL DEFAULT 0 COMMENT '实际入库数量',
    `defective_qty` INT NOT NULL DEFAULT 0 COMMENT '不良数量',
    `location_id` BIGINT NULL COMMENT '上架库位ID',
    `location_code` VARCHAR(32) NULL COMMENT '上架库位编码',
    `unit_cost` DECIMAL(12, 4) NULL COMMENT '入库成本单价',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待入库 1已入库 2部分入库',
    `remark` VARCHAR(256) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_inbound_id` (`inbound_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '入库单明细表';

CREATE TABLE IF NOT EXISTS `outbound_order`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `outbound_no` VARCHAR(32) NOT NULL COMMENT '出库单号',
    `outbound_type` TINYINT NOT NULL COMMENT '出库类型',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `warehouse_name` VARCHAR(64) NOT NULL COMMENT '仓库名称',
    `ref_type` VARCHAR(32) NULL COMMENT '来源类型',
    `ref_id` BIGINT NULL COMMENT '来源单据ID',
    `ref_no` VARCHAR(32) NULL COMMENT '来源单号',
    `plan_date` DATE NULL COMMENT '计划出库日期',
    `actual_date` DATE NULL COMMENT '实际出库日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待分配 1待拣货 2拣货中 3待复核 4已出库 5取消',
    `pick_user_id` BIGINT NULL COMMENT '拣货人ID',
    `pick_start_time` DATETIME NULL COMMENT '开始拣货时间',
    `pick_end_time` DATETIME NULL COMMENT '完成拣货时间',
    `total_qty` INT NOT NULL DEFAULT 0 COMMENT '出库总数',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_outbound_no` (`tenant_id`, `outbound_no`),
    KEY `idx_warehouse_status` (`warehouse_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库单主表';

CREATE TABLE IF NOT EXISTS `outbound_order_item`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `outbound_id` BIGINT NOT NULL COMMENT '出库单ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `quantity` INT NOT NULL COMMENT '计划出库数量',
    `picked_qty` INT NOT NULL DEFAULT 0 COMMENT '已拣数量',
    `location_id` BIGINT NULL COMMENT '拣货库位ID',
    `location_code` VARCHAR(32) NULL COMMENT '拣货库位编码',
    `pick_status` TINYINT NOT NULL DEFAULT 0 COMMENT '拣货状态',
    `remark` VARCHAR(256) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_outbound_id` (`outbound_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出库单明细表';

CREATE TABLE IF NOT EXISTS `stocktake_task`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `task_no` VARCHAR(32) NOT NULL COMMENT '盘点任务编号',
    `task_type` TINYINT NOT NULL COMMENT '盘点类型',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `task_name` VARCHAR(128) NOT NULL COMMENT '任务名称',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `start_time` DATETIME NULL COMMENT '开始时间',
    `end_time` DATETIME NULL COMMENT '结束时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待开始 1进行中 2待审核 3完成 4取消',
    `total_sku_count` INT NOT NULL DEFAULT 0 COMMENT 'SKU种数',
    `diff_sku_count` INT NOT NULL DEFAULT 0 COMMENT '差异SKU数',
    `profit_qty` INT NOT NULL DEFAULT 0 COMMENT '盘盈数量',
    `loss_qty` INT NOT NULL DEFAULT 0 COMMENT '盘亏数量',
    `profit_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '盘盈金额',
    `loss_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '盘亏金额',
    `auditor_id` BIGINT NULL COMMENT '审核人ID',
    `audit_time` DATETIME NULL COMMENT '审核时间',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_task_no` (`tenant_id`, `task_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存盘点任务主表';

CREATE TABLE IF NOT EXISTS `stocktake_item`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NULL COMMENT '库位ID',
    `location_code` VARCHAR(32) NULL COMMENT '库位编码',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `book_qty` INT NOT NULL COMMENT '账面数量',
    `actual_qty` INT NULL COMMENT '实盘数量',
    `diff_qty` INT NULL COMMENT '差异数量',
    `diff_amount` DECIMAL(12, 2) NULL COMMENT '差异金额',
    `diff_reason` VARCHAR(256) NULL COMMENT '差异原因',
    `is_adjusted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否调整',
    `adjust_time` DATETIME NULL COMMENT '调整时间',
    `picker_id` BIGINT NULL COMMENT '盘点人ID',
    `pick_time` DATETIME NULL COMMENT '盘点时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '盘点明细表';

CREATE TABLE IF NOT EXISTS `transfer_order`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `transfer_no` VARCHAR(32) NOT NULL COMMENT '调拨单编号',
    `from_warehouse_id` BIGINT NOT NULL COMMENT '调出仓库ID',
    `from_warehouse_name` VARCHAR(64) NOT NULL COMMENT '调出仓库名称',
    `to_warehouse_id` BIGINT NOT NULL COMMENT '调入仓库ID',
    `to_warehouse_name` VARCHAR(64) NOT NULL COMMENT '调入仓库名称',
    `transfer_reason` VARCHAR(256) NULL COMMENT '调拨原因',
    `logistics_company` VARCHAR(64) NULL COMMENT '物流公司',
    `tracking_no` VARCHAR(128) NULL COMMENT '物流单号',
    `plan_date` DATE NULL COMMENT '计划日期',
    `ship_date` DATE NULL COMMENT '发货日期',
    `arrive_date` DATE NULL COMMENT '到货日期',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1已审核 2调出中 3已到达 4完成 5取消',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_transfer_no` (`tenant_id`, `transfer_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库调拨单表';

CREATE TABLE IF NOT EXISTS `transfer_order_item`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `transfer_id` BIGINT NOT NULL COMMENT '调拨单ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `transfer_qty` INT NOT NULL COMMENT '计划调拨数量',
    `shipped_qty` INT NOT NULL DEFAULT 0 COMMENT '已发数量',
    `received_qty` INT NOT NULL DEFAULT 0 COMMENT '已收数量',
    `from_location_id` BIGINT NULL COMMENT '调出库位ID',
    `to_location_id` BIGINT NULL COMMENT '调入库位ID',
    PRIMARY KEY (`id`),
    KEY `idx_transfer_id` (`transfer_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调拨单明细表';

CREATE TABLE IF NOT EXISTS `inventory_warning_event`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `warning_level` TINYINT NOT NULL COMMENT '预警级别：1紧张 2不足 3零库存 4积压',
    `available_qty` INT NOT NULL COMMENT '可售库存',
    `safety_stock` INT NOT NULL COMMENT '安全库存',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1生效 2解除',
    `first_detected_time` DATETIME NOT NULL COMMENT '首次发现时间',
    `last_detected_time` DATETIME NOT NULL COMMENT '最近发现时间',
    `resolved_time` DATETIME NULL COMMENT '解除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warning_active` (`tenant_id`, `warehouse_id`, `sku_id`, `warning_level`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存预警事件表';

CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚镜像',
    `log_status` INT NOT NULL COMMENT '状态',
    `log_created` DATETIME(6) NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Seata AT undo log';
