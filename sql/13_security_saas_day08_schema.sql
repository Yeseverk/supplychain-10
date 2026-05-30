USE `supplychain_dev`;

-- ============================================================
-- Day08 权限安全系统 + SaaS 运营管理
-- ============================================================

-- 套餐功能开关表
CREATE TABLE IF NOT EXISTS `sys_plan_feature`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `plan_type`     TINYINT         NOT NULL COMMENT '套餐类型：1=基础版 2=专业版 3=企业版',
    `feature_code`  VARCHAR(64)     NOT NULL COMMENT '功能编码，与权限标识前缀对应',
    `feature_name`  VARCHAR(128)    NOT NULL COMMENT '功能名称',
    `is_enabled`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否开启：0=关闭 1=开启',
    `limit_value`   INT             NULL COMMENT '数量限制，NULL表示无限制',
    `limit_unit`    VARCHAR(32)     NULL COMMENT '限制单位',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_feature` (`plan_type`, `feature_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '套餐功能开关配置表';

-- 套餐功能初始化数据
INSERT INTO `sys_plan_feature`
(`id`, `plan_type`, `feature_code`, `feature_name`, `is_enabled`, `limit_value`, `limit_unit`)
VALUES
    (1, 1, 'supplier.max', '最大供应商数量', 1, 20, '家'),
    (2, 1, 'warehouse.max', '最大仓库数量', 1, 1, '个'),
    (3, 1, 'order.monthly', '月最大订单量', 1, 500, '单'),
    (4, 1, 'platform.max', '最大对接平台数', 1, 1, '个'),
    (5, 1, 'bi.ai', 'AI智能查询', 0, NULL, NULL),
    (6, 1, 'supplier.portal', '供应商Portal', 0, NULL, NULL),
    (7, 1, 'api.open', '开放API接口', 0, NULL, NULL),
    (8, 2, 'supplier.max', '最大供应商数量', 1, 100, '家'),
    (9, 2, 'warehouse.max', '最大仓库数量', 1, 5, '个'),
    (10, 2, 'order.monthly', '月最大订单量', 1, 5000, '单'),
    (11, 2, 'platform.max', '最大对接平台数', 1, 3, '个'),
    (12, 2, 'bi.ai', 'AI智能查询', 1, NULL, NULL),
    (13, 2, 'supplier.portal', '供应商Portal', 1, NULL, NULL),
    (14, 2, 'api.open', '开放API接口', 0, NULL, NULL),
    (15, 3, 'supplier.max', '最大供应商数量', 1, NULL, '无限制'),
    (16, 3, 'warehouse.max', '最大仓库数量', 1, NULL, '无限制'),
    (17, 3, 'order.monthly', '月最大订单量', 1, NULL, '无限制'),
    (18, 3, 'platform.max', '最大对接平台数', 1, NULL, '无限制'),
    (19, 3, 'bi.ai', 'AI智能查询完整能力', 1, NULL, NULL),
    (20, 3, 'supplier.portal', '供应商Portal', 1, NULL, NULL),
    (21, 3, 'api.open', '开放API接口', 1, NULL, NULL)
ON DUPLICATE KEY UPDATE
    `feature_name` = VALUES(`feature_name`),
    `is_enabled` = VALUES(`is_enabled`),
    `limit_value` = VALUES(`limit_value`),
    `limit_unit` = VALUES(`limit_unit`);

-- 补充 Day08 菜单和按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `component`, `icon`, `sort`, `is_visible`, `status`)
VALUES
    (906, 900, '租户运营', 2, NULL, '/system/tenant-operation', 'system/tenant-operation/index', NULL, 6, 1, 1),
    (907, 900, '套餐功能', 2, NULL, '/system/plan-feature', 'system/plan-feature/index', NULL, 7, 1, 1),
    (908, 900, '消息中心', 2, NULL, '/system/messages', 'system/messages/index', NULL, 8, 1, 1),
    (90101, 901, '查看用户', 3, 'sys:user:list', NULL, NULL, NULL, 1, 0, 1),
    (90102, 901, '新增用户', 3, 'sys:user:add', NULL, NULL, NULL, 2, 0, 1),
    (90103, 901, '编辑用户', 3, 'sys:user:edit', NULL, NULL, NULL, 3, 0, 1),
    (90104, 901, '删除用户', 3, 'sys:user:delete', NULL, NULL, NULL, 4, 0, 1),
    (90201, 902, '管理角色', 3, 'sys:role:manage', NULL, NULL, NULL, 1, 0, 1),
    (90301, 903, '管理菜单', 3, 'sys:menu:manage', NULL, NULL, NULL, 1, 0, 1),
    (90501, 905, '查看审计日志', 3, 'sys:audit:list', NULL, NULL, NULL, 1, 0, 1),
    (90601, 906, '管理租户', 3, 'saas:tenant:manage', NULL, NULL, NULL, 1, 0, 1),
    (90701, 907, '管理套餐功能', 3, 'saas:plan:manage', NULL, NULL, NULL, 1, 0, 1),
    (90801, 908, '管理消息', 3, 'sys:message:manage', NULL, NULL, NULL, 1, 0, 1),
    (10201, 102, '查看采购单', 3, 'pms:order:list', NULL, NULL, NULL, 1, 0, 1),
    (10202, 102, '管理采购单', 3, 'pms:order:manage', NULL, NULL, NULL, 2, 0, 1),
    (10203, 102, '确认采购入库', 3, 'pms:receipt:confirm', NULL, NULL, NULL, 3, 0, 1),
    (10301, 103, '管理仓库', 3, 'wms:warehouse:manage', NULL, NULL, NULL, 1, 0, 1),
    (10302, 103, '管理入库单', 3, 'wms:inbound:manage', NULL, NULL, NULL, 2, 0, 1),
    (10303, 103, '管理出库单', 3, 'wms:outbound:manage', NULL, NULL, NULL, 3, 0, 1),
    (10401, 104, '查看库存', 3, 'wms:inventory:list', NULL, NULL, NULL, 1, 0, 1),
    (10402, 104, '调整库存', 3, 'wms:inventory:adjust', NULL, NULL, NULL, 2, 0, 1),
    (10403, 104, '审核库存盘点', 3, 'wms:stocktake:audit', NULL, NULL, NULL, 3, 0, 1),
    (20001, 200, '管理商品', 3, 'pim:product:manage', NULL, NULL, NULL, 1, 0, 1),
    (30001, 300, '查看订单', 3, 'oms:order:list', NULL, NULL, NULL, 1, 0, 1),
    (30002, 300, '管理订单', 3, 'oms:order:manage', NULL, NULL, NULL, 2, 0, 1),
    (40001, 400, '新增运单', 3, 'tms:waybill:add', NULL, NULL, NULL, 1, 0, 1),
    (40002, 400, '管理物流', 3, 'tms:logistics:manage', NULL, NULL, NULL, 2, 0, 1),
    (50001, 500, '导入账单', 3, 'fms:bill:import', NULL, NULL, NULL, 1, 0, 1),
    (50002, 500, '查看利润', 3, 'fms:profit:view', NULL, NULL, NULL, 2, 0, 1),
    (60001, 600, '查看BI看板', 3, 'bi:dashboard:view', NULL, NULL, NULL, 1, 0, 1)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `permission` = VALUES(`permission`),
    `status` = VALUES(`status`);

-- 超级管理员拥有全部系统权限
INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 130000 + m.id, 0, 1, m.id
FROM `sys_menu` m
WHERE m.id BETWEEN 100 AND 99999;

-- 租户管理员拥有租户内业务权限
INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 230000 + m.id, 0, 2, m.id
FROM `sys_menu` m
WHERE m.permission IS NULL
   OR m.permission IN (
        'srm:supplier:list', 'srm:supplier:add', 'srm:supplier:edit', 'srm:supplier:delete', 'srm:supplier:audit',
        'pms:order:list', 'pms:order:manage', 'pms:receipt:confirm',
        'wms:warehouse:manage', 'wms:inbound:manage', 'wms:outbound:manage',
        'wms:inventory:list', 'wms:inventory:adjust', 'wms:stocktake:audit',
        'pim:product:manage', 'oms:order:list', 'oms:order:manage',
        'tms:waybill:add', 'tms:logistics:manage',
        'fms:bill:import', 'fms:profit:view', 'bi:dashboard:view',
        'sys:user:list', 'sys:user:add', 'sys:user:edit', 'sys:user:delete',
        'sys:role:manage', 'sys:message:manage'
   );

-- 采购专员权限
INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 330000 + m.id, 0, 3, m.id
FROM `sys_menu` m
WHERE m.permission IN ('srm:supplier:list', 'srm:supplier:add', 'srm:supplier:edit', 'pms:order:list', 'pms:order:manage', 'bi:dashboard:view');

-- 仓储管理员权限
INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 430000 + m.id, 0, 4, m.id
FROM `sys_menu` m
WHERE m.permission IN (
    'wms:warehouse:manage', 'wms:inbound:manage', 'wms:outbound:manage',
    'wms:inventory:list', 'wms:inventory:adjust', 'wms:stocktake:audit',
    'pms:receipt:confirm', 'bi:dashboard:view'
);

-- 运营、物流、财务权限
INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 530000 + m.id, 0, 5, m.id
FROM `sys_menu` m
WHERE m.permission IN ('pim:product:manage', 'oms:order:list', 'oms:order:manage', 'wms:inventory:list', 'bi:dashboard:view');

INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 630000 + m.id, 0, 6, m.id
FROM `sys_menu` m
WHERE m.permission IN ('tms:waybill:add', 'tms:logistics:manage', 'bi:dashboard:view');

INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 730000 + m.id, 0, 7, m.id
FROM `sys_menu` m
WHERE m.permission IN ('pms:order:list', 'fms:bill:import', 'fms:profit:view', 'bi:dashboard:view');
