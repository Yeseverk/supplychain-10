USE `supplychain_dev`;

-- FlexChain seller workspace MVP permissions used by the React frontend.
INSERT INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `component`, `icon`, `sort`, `is_visible`, `status`)
VALUES
    (10302, 103, '管理入库单', 3, 'wms:inbound:manage', NULL, NULL, NULL, 2, 0, 1),
    (10303, 103, '管理出库单', 3, 'wms:outbound:manage', NULL, NULL, NULL, 3, 0, 1),
    (10403, 104, '审核库存盘点', 3, 'wms:stocktake:audit', NULL, NULL, NULL, 3, 0, 1),
    (90101, 901, '查看用户', 3, 'sys:user:list', NULL, NULL, NULL, 1, 0, 1),
    (90102, 901, '新增用户', 3, 'sys:user:add', NULL, NULL, NULL, 2, 0, 1),
    (90103, 901, '编辑用户', 3, 'sys:user:edit', NULL, NULL, NULL, 3, 0, 1),
    (90104, 901, '删除用户', 3, 'sys:user:delete', NULL, NULL, NULL, 4, 0, 1),
    (90201, 902, '管理角色', 3, 'sys:role:manage', NULL, NULL, NULL, 1, 0, 1),
    (90801, 908, '管理消息', 3, 'sys:message:manage', NULL, NULL, NULL, 1, 0, 1)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `permission` = VALUES(`permission`),
    `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 1000000 + m.id, 0, 1, m.id
FROM `sys_menu` m
WHERE m.id IN (10302, 10303, 10403, 90101, 90102, 90103, 90104, 90201, 90801);

INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 2000000 + m.id, 0, 2, m.id
FROM `sys_menu` m
WHERE m.id IN (10302, 10303, 10403, 90101, 90102, 90103, 90104, 90201, 90801);

INSERT IGNORE INTO `sys_role_menu` (`id`, `tenant_id`, `role_id`, `menu_id`)
SELECT 4000000 + m.id, 0, 4, m.id
FROM `sys_menu` m
WHERE m.id IN (10302, 10303, 10403);
