USE `supplychain_dev`;

-- Seata AT 模式 undo_log，每个参与分布式事务的业务库都需要创建。
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
    `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    `context` VARCHAR(128) NOT NULL COMMENT '上下文',
    `rollback_info` LONGBLOB NOT NULL COMMENT '回滚镜像',
    `log_status` INT NOT NULL COMMENT '状态：0正常 1全局已完成',
    `log_created` DATETIME(6) NOT NULL COMMENT '创建时间',
    `log_modified` DATETIME(6) NOT NULL COMMENT '修改时间',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Seata AT undo log';
