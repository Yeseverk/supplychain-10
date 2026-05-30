USE `supplychain_dev`;

-- Day03 财务模块表结构。
CREATE TABLE IF NOT EXISTS `finance_payable`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `payable_no` VARCHAR(32) NOT NULL COMMENT '应付账款单号',
    `source_type` VARCHAR(32) NULL COMMENT '来源类型：PURCHASE_ORDER采购单 TMS_LOGISTICS_BILL物流账单',
    `source_biz_no` VARCHAR(64) NULL COMMENT '来源业务单号',
    `po_id` BIGINT NULL COMMENT '采购单ID',
    `po_no` VARCHAR(32) NULL COMMENT '采购单编号',
    `supplier_id` BIGINT NULL COMMENT '供应商ID',
    `supplier_name` VARCHAR(128) NULL COMMENT '供应商名称',
    `invoice_no` VARCHAR(64) NULL COMMENT '发票编号',
    `invoice_date` DATE NULL COMMENT '发票日期',
    `payable_amount` DECIMAL(12, 2) NOT NULL COMMENT '应付金额',
    `paid_amount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT '已付金额',
    `remaining_amount` DECIMAL(12, 2) GENERATED ALWAYS AS (`payable_amount` - `paid_amount`) STORED COMMENT '剩余金额',
    `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `payment_days` INT NOT NULL DEFAULT 0 COMMENT '账期天数',
    `due_date` DATE NOT NULL COMMENT '到期日',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待付 1部分已付 2已结清 3作废',
    `overdue_days` INT NOT NULL DEFAULT 0 COMMENT '逾期天数',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NULL COMMENT '创建人',
    `update_by` BIGINT NULL COMMENT '更新人',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_payable_no` (`tenant_id`, `payable_no`),
    UNIQUE KEY `uk_po_id` (`po_id`),
    UNIQUE KEY `uk_tenant_source_biz` (`tenant_id`, `source_type`, `source_biz_no`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '应付账款表';

CREATE TABLE IF NOT EXISTS `finance_payment_record`
(
    `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `payable_id` BIGINT NOT NULL COMMENT '应付账款ID',
    `payment_amount` DECIMAL(12, 2) NOT NULL COMMENT '付款金额',
    `payment_date` DATE NOT NULL COMMENT '付款日期',
    `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '付款方式',
    `voucher_no` VARCHAR(64) NOT NULL COMMENT '凭证号',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(256) NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_payable_id` (`payable_id`),
    KEY `idx_payment_date` (`payment_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '付款记录表';
