USE `supplychain_dev`;

-- 供应商主表
CREATE TABLE IF NOT EXISTS `supplier`
(
    `id`                 BIGINT UNSIGNED NOT NULL COMMENT '主键ID，雪花算法生成',
    `tenant_id`          BIGINT          NOT NULL COMMENT '租户ID，多租户隔离核心字段',
    `supplier_code`      VARCHAR(32)     NOT NULL COMMENT '供应商编码，系统自动生成，格式：SUP-YYYYMMDD-XXXX',
    `supplier_name`      VARCHAR(128)    NOT NULL COMMENT '供应商公司名称',
    `supplier_type`      TINYINT         NOT NULL COMMENT '供应商类型：1=工厂供应商 2=贸易商 3=物流服务商',
    `category_ids`       JSON            NULL COMMENT '供货品类ID列表',
    `province`           VARCHAR(32)     NULL COMMENT '所在省份',
    `city`               VARCHAR(32)     NULL COMMENT '所在城市',
    `address`            VARCHAR(256)    NULL COMMENT '详细地址',
    `website`            VARCHAR(256)    NULL COMMENT '公司官网URL',
    `company_size`       TINYINT         NULL COMMENT '公司规模：1=50人以下 2=50-200人 3=200-500人 4=500人以上',
    `founded_year`       SMALLINT        NULL COMMENT '成立年份',
    `contact_name`       VARCHAR(64)     NOT NULL COMMENT '主联系人姓名',
    `contact_phone`      VARCHAR(20)     NOT NULL COMMENT '主联系人手机号',
    `contact_email`      VARCHAR(128)    NOT NULL COMMENT '主联系人邮箱',
    `contact_wechat`     VARCHAR(64)     NULL COMMENT '联系人微信号',
    `contact_whatsapp`   VARCHAR(64)     NULL COMMENT '联系人WhatsApp账号',
    `bank_name`          VARCHAR(64)     NULL COMMENT '开户银行名称',
    `bank_account`       VARCHAR(64)     NULL COMMENT '银行账号，加密存储',
    `bank_account_name`  VARCHAR(64)     NULL COMMENT '银行开户名',
    `tax_no`             VARCHAR(32)     NULL COMMENT '纳税人识别号',
    `invoice_type`       TINYINT         NULL COMMENT '开票类型：1=专票 2=普票 3=收据',
    `moq`                INT             NULL COMMENT '最小起订量',
    `lead_time_days`     INT             NULL COMMENT '交货周期，单位天',
    `monthly_capacity`   INT             NULL COMMENT '月最大供货量',
    `currency`           CHAR(3)         NOT NULL DEFAULT 'CNY' COMMENT '结算货币',
    `payment_days`       INT             NOT NULL DEFAULT 0 COMMENT '账期天数',
    `grade`              CHAR(1)         NOT NULL DEFAULT 'C' COMMENT '综合评级：S/A/B/C',
    `score`              DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '最新综合评分',
    `last_score_month`   VARCHAR(6)      NULL COMMENT '最后评分月份，格式YYYYMM',
    `status`             TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=待审核 2=已通过 3=已拒绝 4=已停用',
    `audit_user_id`      BIGINT          NULL COMMENT '最后审核人用户ID',
    `audit_time`         DATETIME        NULL COMMENT '最后审核时间',
    `audit_remark`       VARCHAR(512)    NULL COMMENT '最后审核意见',
    `portal_user_id`     BIGINT          NULL COMMENT '供应商Portal账号用户ID',
    `portal_enabled`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Portal是否开通：0=否 1=是',
    `remark`             VARCHAR(512)    NULL COMMENT '内部备注',
    `tags`               JSON            NULL COMMENT '供应商标签列表',
    `create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_by`          BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`          BIGINT          NULL COMMENT '最后修改人用户ID',
    `is_deleted`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`            INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_supplier_code` (`tenant_id`, `supplier_code`) COMMENT '同一租户内供应商编码唯一',
    UNIQUE KEY `uk_portal_user_id` (`portal_user_id`) COMMENT 'Portal用户ID唯一，防止并发重复开通',
    UNIQUE KEY `uk_tenant_contact_email` (`tenant_id`, `contact_email`) COMMENT '同一租户内Portal登录邮箱唯一',
    KEY `idx_tenant_status` (`tenant_id`, `status`) COMMENT '按租户和状态筛选',
    KEY `idx_tenant_grade` (`tenant_id`, `grade`) COMMENT '按租户和评级筛选',
    KEY `idx_tenant_type` (`tenant_id`, `supplier_type`) COMMENT '按租户和类型筛选',
    KEY `idx_create_time` (`create_time`) COMMENT '按创建时间排序'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商主表';

-- 供应商资质文件表
CREATE TABLE IF NOT EXISTS `supplier_cert`
(
    `id`          BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`   BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id` BIGINT          NOT NULL COMMENT '关联供应商ID',
    `cert_type`   TINYINT         NOT NULL COMMENT '资质类型：1=营业执照 2=质检报告 3=产品认证 4=银行证明 5=其他',
    `cert_name`   VARCHAR(128)    NOT NULL COMMENT '资质名称',
    `file_name`   VARCHAR(256)    NOT NULL COMMENT '原始文件名称',
    `file_url`    VARCHAR(512)    NOT NULL COMMENT '文件存储URL',
    `file_size`   BIGINT          NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    `file_type`   VARCHAR(32)     NOT NULL COMMENT '文件MIME类型',
    `issue_date`  DATE            NULL COMMENT '颁发日期',
    `expire_date` DATE            NULL COMMENT '有效期截止日期',
    `is_expired`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否已过期：0=有效 1=已过期',
    `cert_no`     VARCHAR(64)     NULL COMMENT '证书编号',
    `remark`      VARCHAR(256)    NULL COMMENT '备注',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_by`   BIGINT          NULL COMMENT '上传人用户ID',
    `is_deleted`  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询资质',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_expire_date` (`expire_date`) COMMENT '按到期日期查询'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商资质文件表';

-- 供应商联系人表
CREATE TABLE IF NOT EXISTS `supplier_contact`
(
    `id`           BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`    BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`  BIGINT          NOT NULL COMMENT '关联供应商ID',
    `contact_name` VARCHAR(64)     NOT NULL COMMENT '联系人姓名',
    `position`     VARCHAR(64)     NULL COMMENT '职位',
    `phone`        VARCHAR(20)     NULL COMMENT '手机号',
    `email`        VARCHAR(128)    NULL COMMENT '邮箱',
    `wechat`       VARCHAR(64)     NULL COMMENT '微信号',
    `whatsapp`     VARCHAR(64)     NULL COMMENT 'WhatsApp账号',
    `department`   VARCHAR(64)     NULL COMMENT '所在部门',
    `is_primary`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否主联系人：1=是 0=否',
    `contact_type` TINYINT         NOT NULL DEFAULT 1 COMMENT '联系人类型：1=业务 2=财务 3=技术 4=紧急',
    `remark`       VARCHAR(256)    NULL COMMENT '备注',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    BIGINT          NULL COMMENT '创建人用户ID',
    `is_deleted`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`),
    KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商联系人表';

-- 供应商月度评分记录表
CREATE TABLE IF NOT EXISTS `supplier_score_log`
(
    `id`                 BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`          BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`        BIGINT          NOT NULL COMMENT '关联供应商ID',
    `score_month`        VARCHAR(6)      NOT NULL COMMENT '评分月份，格式YYYYMM',
    `total_orders`       INT             NOT NULL DEFAULT 0 COMMENT '本月采购订单总数',
    `delivered_on_time`  INT             NOT NULL DEFAULT 0 COMMENT '准时到货订单数',
    `quality_passed`     INT             NOT NULL DEFAULT 0 COMMENT '质检合格批次数',
    `quality_total`      INT             NOT NULL DEFAULT 0 COMMENT '质检总批次数',
    `response_hours_avg` DECIMAL(8, 2)   NULL COMMENT '平均响应时长，单位小时',
    `price_comparison`   DECIMAL(8, 4)   NULL COMMENT '价格竞争力系数',
    `delivery_score`     DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '准时交货评分',
    `quality_score`      DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '质量合格评分',
    `response_score`     DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '响应速度评分',
    `price_score`        DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '价格竞争力评分',
    `total_score`        DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '综合评分',
    `grade`              CHAR(1)         NOT NULL COMMENT '本月评级：S/A/B/C',
    `grade_changed`      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '评级是否变化：0=否 1=是',
    `prev_grade`         CHAR(1)         NULL COMMENT '上月评级',
    `calc_remark`        VARCHAR(512)    NULL COMMENT '计算说明',
    `calc_time`          DATETIME        NOT NULL COMMENT '计算时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_month` (`supplier_id`, `score_month`) COMMENT '同一供应商同一月份唯一',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_score_month` (`score_month`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商月度评分记录表';

-- 供应商重点观察名单表
CREATE TABLE IF NOT EXISTS `supplier_watchlist`
(
    `id`                BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`         BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`       BIGINT          NOT NULL COMMENT '供应商ID',
    `current_grade`     CHAR(1)         NOT NULL COMMENT '进入观察名单时的评级：S/A/B/C',
    `current_score`     DECIMAL(5, 2)   NOT NULL DEFAULT 0.00 COMMENT '进入观察名单时的评分',
    `watch_reason`      VARCHAR(256)    NOT NULL COMMENT '观察原因',
    `system_suggestion` VARCHAR(512)    NOT NULL COMMENT '系统建议，仅供租户决策参考',
    `status`            TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=观察中 2=已解除',
    `watch_time`        DATETIME        NOT NULL COMMENT '进入观察名单时间',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`         BIGINT          NULL COMMENT '更新人用户ID',
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`           INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_watch_status` (`supplier_id`, `status`, `is_deleted`) COMMENT '按供应商和观察状态查询',
    KEY `idx_tenant_status` (`tenant_id`, `status`) COMMENT '按租户和状态查询观察名单',
    KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询观察记录'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商重点观察名单表';

-- 供应商租户级策略配置表
CREATE TABLE IF NOT EXISTS `supplier_tenant_config`
(
    `id`           BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`    BIGINT          NOT NULL COMMENT '租户ID',
    `config_key`   VARCHAR(128)    NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(512)    NOT NULL COMMENT '配置值',
    `config_desc`  VARCHAR(256)    NULL COMMENT '配置说明',
    `enabled`      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用 1=启用',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`    BIGINT          NULL COMMENT '更新人用户ID',
    `is_deleted`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`      INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_config_key` (`tenant_id`, `config_key`) COMMENT '同一租户配置键唯一',
    KEY `idx_config_key` (`config_key`) COMMENT '按配置键查询'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商租户级策略配置表';

-- 供应商品类风险事件表
CREATE TABLE IF NOT EXISTS `supplier_risk_event`
(
    `id`                  BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`           BIGINT          NOT NULL COMMENT '租户ID',
    `category_id`         BIGINT          NOT NULL COMMENT '品类ID',
    `risk_type`           VARCHAR(64)     NOT NULL COMMENT '风险类型：SUPPLIER_COUNT_LOW=供应商数量不足 ALL_SUPPLIER_GRADE_LOW=供应商评级偏低',
    `risk_level`          VARCHAR(16)     NOT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH',
    `risk_reason`         VARCHAR(512)    NOT NULL COMMENT '风险原因',
    `system_suggestion`   VARCHAR(512)    NOT NULL COMMENT '系统建议，仅供租户决策参考',
    `supplier_count`      INT             NOT NULL DEFAULT 0 COMMENT '风险发生时可用供应商数量',
    `best_grade`          CHAR(1)         NOT NULL DEFAULT 'C' COMMENT '风险发生时该品类最高供应商评级',
    `status`              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：1=处理中 2=已解除',
    `first_detected_date` DATE            NOT NULL COMMENT '首次发现日期',
    `last_detected_date`  DATE            NOT NULL COMMENT '最近发现日期',
    `resolved_time`       DATETIME        NULL COMMENT '风险解除时间',
    `create_time`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`           BIGINT          NULL COMMENT '创建人用户ID',
    `update_by`           BIGINT          NULL COMMENT '更新人用户ID',
    `is_deleted`          TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    `version`             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_category_risk_status` (`tenant_id`, `category_id`, `risk_type`, `status`, `is_deleted`) COMMENT '按品类风险状态查询',
    KEY `idx_tenant_status` (`tenant_id`, `status`) COMMENT '按租户和状态查询风险',
    KEY `idx_category_id` (`category_id`) COMMENT '按品类查询风险'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商品类风险事件表';

-- 供应商采购到货绩效事实表
CREATE TABLE IF NOT EXISTS `supplier_purchase_arrival`
(
    `id`                    BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`             BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`           BIGINT          NOT NULL COMMENT '供应商ID',
    `score_month`           VARCHAR(6)      NOT NULL COMMENT '评分月份，格式YYYYMM',
    `purchase_order_id`     BIGINT          NOT NULL COMMENT '采购订单ID',
    `promised_arrival_date` DATE            NOT NULL COMMENT '承诺到货日期',
    `actual_arrival_date`   DATE            NOT NULL COMMENT '实际到货日期',
    `create_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`            TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_order_id` (`purchase_order_id`) COMMENT '采购订单到货记录唯一',
    KEY `idx_supplier_month` (`supplier_id`, `score_month`) COMMENT '按供应商和月份统计到货',
    KEY `idx_tenant_month` (`tenant_id`, `score_month`) COMMENT '按租户和月份统计到货'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商采购到货绩效事实表';

-- 供应商入库质检绩效事实表
CREATE TABLE IF NOT EXISTS `supplier_quality_inspection`
(
    `id`                BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`         BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`       BIGINT          NOT NULL COMMENT '供应商ID',
    `score_month`       VARCHAR(6)      NOT NULL COMMENT '评分月份，格式YYYYMM',
    `inspection_no`     VARCHAR(64)     NOT NULL COMMENT '质检单号',
    `inspection_result` TINYINT         NOT NULL COMMENT '质检结果：1=合格 0=不合格',
    `inspection_time`   DATETIME        NOT NULL COMMENT '质检时间',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inspection_no` (`inspection_no`) COMMENT '质检单号唯一',
    KEY `idx_supplier_month` (`supplier_id`, `score_month`) COMMENT '按供应商和月份统计质检',
    KEY `idx_tenant_month` (`tenant_id`, `score_month`) COMMENT '按租户和月份统计质检'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商入库质检绩效事实表';

-- 供应商询价响应绩效事实表
CREATE TABLE IF NOT EXISTS `supplier_quote_response`
(
    `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`      BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`    BIGINT          NOT NULL COMMENT '供应商ID',
    `score_month`    VARCHAR(6)      NOT NULL COMMENT '评分月份，格式YYYYMM',
    `inquiry_id`     BIGINT          NOT NULL COMMENT '询价单ID',
    `inquiry_time`   DATETIME        NOT NULL COMMENT '发起询价时间',
    `quote_time`     DATETIME        NULL COMMENT '供应商回价时间',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inquiry_supplier` (`inquiry_id`, `supplier_id`) COMMENT '同一询价单供应商响应唯一',
    KEY `idx_supplier_month` (`supplier_id`, `score_month`) COMMENT '按供应商和月份统计响应',
    KEY `idx_tenant_month` (`tenant_id`, `score_month`) COMMENT '按租户和月份统计响应'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商询价响应绩效事实表';

-- 供应商采购价格绩效事实表
CREATE TABLE IF NOT EXISTS `supplier_purchase_price`
(
    `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`      BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`    BIGINT          NOT NULL COMMENT '供应商ID',
    `score_month`    VARCHAR(6)      NOT NULL COMMENT '评分月份，格式YYYYMM',
    `category_id`    BIGINT          NOT NULL COMMENT '采购品类ID',
    `purchase_id`    BIGINT          NOT NULL COMMENT '采购明细ID',
    `unit_price`     DECIMAL(12, 4)  NOT NULL COMMENT '采购单价',
    `currency`       CHAR(3)         NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `purchase_time`  DATETIME        NOT NULL COMMENT '采购时间',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_id` (`purchase_id`) COMMENT '采购明细价格记录唯一',
    KEY `idx_supplier_month` (`supplier_id`, `score_month`) COMMENT '按供应商和月份统计价格',
    KEY `idx_tenant_month` (`tenant_id`, `score_month`) COMMENT '按租户和月份统计价格',
    KEY `idx_category_month` (`tenant_id`, `category_id`, `score_month`) COMMENT '按品类和月份统计市场均价'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商采购价格绩效事实表';

-- 供应商审核操作日志表
CREATE TABLE IF NOT EXISTS `supplier_audit_log`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`     BIGINT          NOT NULL COMMENT '租户ID',
    `supplier_id`   BIGINT          NOT NULL COMMENT '关联供应商ID',
    `from_status`   TINYINT         NULL COMMENT '变更前状态',
    `to_status`     TINYINT         NOT NULL COMMENT '变更后状态',
    `action`        VARCHAR(64)     NOT NULL COMMENT '操作动作',
    `audit_remark`  VARCHAR(512)    NULL COMMENT '审核意见或操作备注',
    `operator_id`   BIGINT          NOT NULL COMMENT '操作人用户ID',
    `operator_name` VARCHAR(64)     NOT NULL COMMENT '操作人姓名',
    `operate_time`  DATETIME        NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_supplier_id` (`supplier_id`) COMMENT '按供应商查询审核历史',
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '供应商审核操作日志表';
