-- 创建开发环境数据库
CREATE DATABASE IF NOT EXISTS `supplychain_dev`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `supplychain_dev`;

-- 系统用户表
CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`               BIGINT UNSIGNED NOT NULL COMMENT '主键ID，雪花算法生成',
    `tenant_id`        BIGINT          NOT NULL DEFAULT 0 COMMENT '租户ID，平台管理员为0',
    `username`         VARCHAR(64)     NOT NULL COMMENT '登录用户名，同一租户内唯一',
    `password`         VARCHAR(128)    NOT NULL COMMENT '密码，BCrypt加密存储',
    `real_name`        VARCHAR(64)     NULL COMMENT '真实姓名',
    `avatar`           VARCHAR(512)    NULL COMMENT '头像图片URL',
    `email`            VARCHAR(128)    NULL COMMENT '邮箱地址，用于通知和找回密码',
    `phone`            VARCHAR(20)     NULL COMMENT '手机号，加密存储',
    `user_type`        TINYINT         NOT NULL DEFAULT 1 COMMENT '用户类型：1=普通用户 2=租户管理员 3=供应商用户 9=超级管理员',
    `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=正常 2=锁定',
    `login_fail_count` TINYINT         NOT NULL DEFAULT 0 COMMENT '连续登录失败次数，超过5次锁定账号',
    `lock_time`        DATETIME        NULL COMMENT '账号锁定时间',
    `lock_until_time`  DATETIME        NULL COMMENT '账号锁定截止时间',
    `last_login_time`  DATETIME        NULL COMMENT '最后登录时间',
    `last_login_ip`    VARCHAR(64)     NULL COMMENT '最后登录IP地址',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`        BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`        BIGINT          NULL COMMENT '最后操作人用户ID',
    `is_deleted`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`          INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_username` (`tenant_id`, `username`) COMMENT '同一租户内用户名唯一',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表';

-- 租户表
CREATE TABLE IF NOT EXISTS `sys_tenant`
(
    `id`                BIGINT UNSIGNED NOT NULL COMMENT '租户ID，雪花算法生成',
    `tenant_code`       VARCHAR(32)     NOT NULL COMMENT '租户编码，格式 TC-YYYYMMDD-XXXX，系统自动生成',
    `company_name`      VARCHAR(128)    NOT NULL COMMENT '公司名称',
    `contact_name`      VARCHAR(64)     NOT NULL COMMENT '负责人姓名',
    `contact_phone`     VARCHAR(20)     NOT NULL COMMENT '负责人手机号',
    `contact_email`     VARCHAR(128)    NOT NULL COMMENT '负责人邮箱，同时作为登录账号',
    `plan_type`         TINYINT         NOT NULL DEFAULT 1 COMMENT '套餐类型：1=基础版 2=专业版 3=企业版',
    `plan_start_time`   DATETIME        NULL COMMENT '套餐开始时间',
    `plan_end_time`     DATETIME        NULL COMMENT '套餐到期时间，NULL表示永久有效',
    `trial_end_time`    DATETIME        NULL COMMENT '试用期到期时间',
    `status`            TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=正常 2=试用中 3=已到期 4=已注销',
    `max_supplier`      INT             NOT NULL DEFAULT 20 COMMENT '套餐允许的最大供应商数量',
    `max_warehouse`     INT             NOT NULL DEFAULT 1 COMMENT '套餐允许的最大仓库数量',
    `max_monthly_order` INT             NOT NULL DEFAULT 500 COMMENT '套餐允许的月最大订单量',
    `max_platform`      INT             NOT NULL DEFAULT 1 COMMENT '套餐允许对接的最大平台数量',
    `admin_user_id`     BIGINT          NULL COMMENT '租户管理员的用户ID',
    `remark`            VARCHAR(512)    NULL COMMENT '备注信息',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`         BIGINT          NULL COMMENT '创建人（平台管理员ID）',
    `update_by`         BIGINT          NULL COMMENT '最后操作人',
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已注销',
    `version`           INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_code` (`tenant_code`),
    UNIQUE KEY `uk_contact_email` (`contact_email`),
    KEY `idx_status` (`status`),
    KEY `idx_plan_end_time` (`plan_end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '租户信息表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`   BIGINT          NOT NULL COMMENT '租户ID，0表示平台内置角色',
    `role_name`   VARCHAR(64)     NOT NULL COMMENT '角色名称，如：采购专员',
    `role_code`   VARCHAR(64)     NOT NULL COMMENT '角色编码，如：ROLE_PURCHASE，用于代码判断',
    `role_type`   TINYINT         NOT NULL DEFAULT 1 COMMENT '角色类型：1=自定义角色 2=系统内置角色（不可删除）',
    `data_scope`  TINYINT         NOT NULL DEFAULT 1 COMMENT '数据权限范围：1=全部数据 2=本部门数据 3=仅本人数据',
    `sort`        INT             NOT NULL DEFAULT 0 COMMENT '排序序号，数字越小越靠前',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `remark`      VARCHAR(256)    NULL COMMENT '角色说明',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`   BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`   BIGINT          NULL COMMENT '最后操作人用户ID',
    `is_deleted`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`     INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_role_code` (`tenant_id`, `role_code`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色表';

-- 菜单权限表
CREATE TABLE IF NOT EXISTS `sys_menu`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `parent_id`   BIGINT          NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
    `menu_name`   VARCHAR(64)     NOT NULL COMMENT '菜单/按钮名称',
    `menu_type`   TINYINT         NOT NULL COMMENT '类型：1=目录 2=菜单页面 3=操作按钮',
    `permission`  VARCHAR(128)    NULL COMMENT '权限标识，如 srm:supplier:add，按钮级权限用此字段',
    `path`        VARCHAR(256)    NULL COMMENT '前端路由路径，如 /srm/supplier',
    `component`   VARCHAR(256)    NULL COMMENT '前端组件路径，如 srm/supplier/index',
    `icon`        VARCHAR(64)     NULL COMMENT '菜单图标，使用Element Plus图标名称',
    `sort`        INT             NOT NULL DEFAULT 0 COMMENT '同级菜单排序',
    `is_visible`  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否在菜单中显示：1=显示 0=隐藏',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`   BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`   BIGINT          NULL COMMENT '最后操作人用户ID',
    `is_deleted`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_menu_type` (`menu_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表';

-- 用户与角色的关联表
CREATE TABLE IF NOT EXISTS `sys_user_role`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`   BIGINT          NOT NULL COMMENT '租户ID',
    `user_id`     BIGINT          NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT          NOT NULL COMMENT '角色ID',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    `create_by`   BIGINT          NULL COMMENT '分配人（管理员ID）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '防止重复分配同一角色',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表';

-- 角色与菜单权限的关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`   BIGINT          NOT NULL COMMENT '租户ID',
    `role_id`     BIGINT          NOT NULL COMMENT '角色ID',
    `menu_id`     BIGINT          NOT NULL COMMENT '菜单/按钮ID',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    `create_by`   BIGINT          NULL COMMENT '分配人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`) COMMENT '防止重复分配',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单权限关联表';

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS `sys_audit_log`
(
    `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`      BIGINT          NOT NULL DEFAULT 0 COMMENT '租户ID',
    `user_id`        BIGINT          NOT NULL COMMENT '操作人用户ID',
    `username`       VARCHAR(64)     NOT NULL COMMENT '操作人用户名（冗余存储，防止用户删除后追溯失败）',
    `module`         VARCHAR(64)     NOT NULL COMMENT '操作模块，如：供应商管理、采购管理',
    `action`         VARCHAR(64)     NOT NULL COMMENT '操作动作，如：新增供应商、审核通过、删除采购单',
    `method`         VARCHAR(256)    NOT NULL COMMENT '请求方法（POST/PUT/DELETE）+ 接口路径',
    `request_params` TEXT            NULL COMMENT '请求参数JSON（敏感字段脱敏后记录）',
    `response_code`  INT             NULL COMMENT '响应业务码',
    `ip_address`     VARCHAR(64)     NOT NULL COMMENT '操作来源IP地址',
    `user_agent`     VARCHAR(512)    NULL COMMENT '浏览器User-Agent信息',
    `duration_ms`    INT             NULL COMMENT '接口耗时（毫秒）',
    `status`         TINYINT         NOT NULL DEFAULT 1 COMMENT '操作结果：1=成功 0=失败',
    `error_msg`      VARCHAR(512)    NULL COMMENT '操作失败时的错误信息',
    `operate_time`   DATETIME        NOT NULL COMMENT '操作发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_time` (`tenant_id`, `operate_time`) COMMENT '按租户+时间查询审计日志',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '操作审计日志表（只增不改不删）';

-- 系统站内信消息表
CREATE TABLE IF NOT EXISTS `sys_message`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID，雪花算法生成',
    `tenant_id`     BIGINT          NOT NULL DEFAULT 0 COMMENT '租户ID，平台消息为0',
    `receiver_id`   BIGINT          NOT NULL DEFAULT 0 COMMENT '接收用户ID，按角色发送时为0',
    `receiver_type` VARCHAR(32)     NOT NULL DEFAULT 'USER' COMMENT '接收类型：USER=用户 ROLE=角色 SYSTEM=系统广播',
    `receiver_key`  VARCHAR(128)    NULL COMMENT '接收标识，角色消息存角色编码',
    `title`         VARCHAR(128)    NOT NULL COMMENT '消息标题',
    `content`       VARCHAR(1024)   NOT NULL COMMENT '消息内容',
    `biz_type`      VARCHAR(64)     NOT NULL COMMENT '业务类型，如 SUPPLIER_AUDIT',
    `biz_id`        VARCHAR(128)    NULL COMMENT '业务ID',
    `priority`      VARCHAR(16)     NOT NULL DEFAULT 'NORMAL' COMMENT '优先级：NORMAL=普通 HIGH=重要 URGENT=紧急',
    `read_status`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '读取状态：0=未读 1=已读',
    `read_time`     DATETIME        NULL COMMENT '读取时间',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `is_deleted`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_receiver` (`tenant_id`, `receiver_type`, `receiver_id`, `read_status`) COMMENT '按租户与接收人查询消息',
    KEY `idx_receiver_key` (`tenant_id`, `receiver_type`, `receiver_key`, `read_status`) COMMENT '按租户与角色查询消息',
    KEY `idx_biz` (`biz_type`, `biz_id`) COMMENT '按业务定位消息'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统站内信消息表';

-- 系统可靠事件发件箱表
CREATE TABLE IF NOT EXISTS `sys_event_outbox`
(
    `id`              BIGINT UNSIGNED NOT NULL COMMENT '主键ID，雪花算法生成',
    `tenant_id`       BIGINT          NOT NULL DEFAULT 0 COMMENT '租户ID，平台事件为0',
    `event_id`        VARCHAR(64)     NOT NULL COMMENT '事件全局唯一ID',
    `event_type`      VARCHAR(64)     NOT NULL COMMENT '事件类型，如 SYSTEM_NOTIFICATION',
    `source_service`  VARCHAR(64)     NULL COMMENT '事件来源服务',
    `biz_type`        VARCHAR(64)     NULL COMMENT '业务类型，如 SUPPLIER_RISK',
    `biz_id`          VARCHAR(128)    NULL COMMENT '业务ID',
    `idempotent_key`  VARCHAR(128)    NOT NULL COMMENT '业务幂等键',
    `payload`         TEXT            NOT NULL COMMENT '事件载荷JSON',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '处理状态：0=待分发 1=已分发 2=失败 3=忽略',
    `retry_count`     INT             NOT NULL DEFAULT 0 COMMENT '重试次数',
    `error_msg`       VARCHAR(512)    NULL COMMENT '最近一次错误信息',
    `occurred_time`   DATETIME        NOT NULL COMMENT '事件发生时间',
    `dispatch_time`   DATETIME        NULL COMMENT '分发成功时间',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `is_deleted`      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_id` (`event_id`) COMMENT '事件ID唯一兜底',
    UNIQUE KEY `uk_tenant_idempotent` (`tenant_id`, `idempotent_key`) COMMENT '租户内业务幂等唯一兜底',
    KEY `idx_status_retry` (`status`, `retry_count`, `update_time`) COMMENT '按状态扫描待重试事件',
    KEY `idx_biz` (`biz_type`, `biz_id`) COMMENT '按业务定位事件'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统可靠事件发件箱表';

-- 数据字典主表
CREATE TABLE IF NOT EXISTS `sys_dict_type`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `dict_name`   VARCHAR(64)     NOT NULL COMMENT '字典名称，如：供应商类型',
    `dict_code`   VARCHAR(64)     NOT NULL COMMENT '字典编码，如：supplier_type，代码中用此值查询',
    `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `remark`      VARCHAR(256)    NULL COMMENT '备注说明',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`   BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`   BIGINT          NULL COMMENT '最后操作人用户ID',
    `is_deleted`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '数据字典类型表';

-- 数据字典明细表
CREATE TABLE IF NOT EXISTS `sys_dict_item`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `dict_type_id`  BIGINT          NOT NULL COMMENT '关联字典类型ID',
    `dict_code`     VARCHAR(64)     NOT NULL COMMENT '字典编码（冗余，方便查询）',
    `item_value`    VARCHAR(64)     NOT NULL COMMENT '字典项的值，如：1',
    `item_label`    VARCHAR(128)    NOT NULL COMMENT '字典项的显示文字，如：工厂供应商',
    `item_label_en` VARCHAR(128)    NULL COMMENT '英文显示文字（国际化）',
    `sort`          INT             NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
    `css_class`     VARCHAR(32)     NULL COMMENT '样式类名，用于前端标签着色，如 success/warning/danger',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `remark`        VARCHAR(256)    NULL COMMENT '备注',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `is_deleted`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_dict_code` (`dict_code`),
    KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '数据字典明细表';
