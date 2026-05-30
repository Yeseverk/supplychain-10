USE `supplychain_dev`;

-- Day03 仓储模块库存表结构。
CREATE TABLE IF NOT EXISTS `warehouse_inventory`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NULL COMMENT '库位ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '现有库存',
    `locked_quantity` INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    `available_quantity` INT GENERATED ALWAYS AS (`quantity` - `locked_quantity`) STORED COMMENT '可用库存',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inventory_sku_location` (`tenant_id`, `warehouse_id`, `location_id`, `sku_id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库库存表';

CREATE TABLE IF NOT EXISTS `warehouse_inventory_log`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT NULL COMMENT '库位ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `sku_name` VARCHAR(256) NOT NULL COMMENT 'SKU名称',
    `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型：PURCHASE_IN/RETURN_OUT',
    `biz_no` VARCHAR(64) NOT NULL COMMENT '业务单号',
    `change_qty` INT NOT NULL COMMENT '库存变化数量',
    `before_qty` INT NOT NULL COMMENT '变更前库存',
    `after_qty` INT NOT NULL COMMENT '变更后库存',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_biz_no` (`biz_no`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存流水表';
