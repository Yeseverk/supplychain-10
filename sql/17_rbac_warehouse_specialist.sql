USE `supplychain_dev`;

SET @tenant_id = 2059984036520636418;
SET @role_id = 920000000000000401;
SET @user_id = 920000000000000402;
SET @admin_id = 2059984037695041538;

INSERT INTO `sys_role`
(`id`, `tenant_id`, `role_name`, `role_code`, `role_type`, `data_scope`, `sort`, `status`, `remark`, `create_by`, `update_by`)
VALUES
(@role_id, @tenant_id, CONVERT(0xE4BB93E582A8E4B893E59198 USING utf8mb4), 'ROLE_WAREHOUSE_SPECIALIST', 1, 3, 30, 1, CONVERT(0xE794A8E4BA8EE9AA8CE8AF81E4BB93E582A8E4BABAE591982052424143EFBC9AE4BB85E5BC80E694BEE4BB93E5BA93E38081E5BA93E5AD98E38081E585A5E5BA93E38081E587BAE5BA93E38081E79B98E782B9E79BB8E585B3E883BDE58A9BE38082 USING utf8mb4), @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `data_scope` = VALUES(`data_scope`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`),
    `update_by` = VALUES(`update_by`);

INSERT INTO `sys_user`
(`id`, `tenant_id`, `username`, `password`, `real_name`, `email`, `user_type`, `status`, `login_fail_count`, `create_by`, `update_by`)
VALUES
(@user_id, @tenant_id, 'warehouse@flexchain.local', '$2a$10$DQbKOiUaaJAiuPSPQkxTm.R5A2i6aiOI.UhJc1zzZn2YUnamKtHjW', CONVERT(0xE4BB93E582A8E4B893E59198 USING utf8mb4), 'warehouse@flexchain.local', 1, 1, 0, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE
    `password` = VALUES(`password`),
    `real_name` = VALUES(`real_name`),
    `email` = VALUES(`email`),
    `user_type` = VALUES(`user_type`),
    `status` = VALUES(`status`),
    `login_fail_count` = 0,
    `lock_time` = NULL,
    `lock_until_time` = NULL,
    `update_by` = VALUES(`update_by`);

INSERT INTO `sys_user_role`
(`id`, `tenant_id`, `user_id`, `role_id`, `create_by`)
VALUES
(920000000000000403, @tenant_id, @user_id, @role_id, @admin_id)
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `create_by` = VALUES(`create_by`);

DELETE FROM `sys_role_menu`
WHERE `tenant_id` = @tenant_id
  AND `role_id` = @role_id;

INSERT INTO `sys_role_menu`
(`id`, `tenant_id`, `role_id`, `menu_id`, `create_by`)
SELECT 920000000000100000 + m.id, @tenant_id, @role_id, m.id, @admin_id
FROM `sys_menu` m
WHERE m.id IN (103, 104)
   OR m.permission IN (
        'wms:warehouse:manage',
        'wms:inbound:manage',
        'wms:outbound:manage',
        'wms:inventory:list',
        'wms:inventory:adjust',
        'wms:stocktake:audit'
   )
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `create_by` = VALUES(`create_by`);
