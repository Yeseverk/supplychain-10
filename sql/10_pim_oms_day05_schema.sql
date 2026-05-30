-- ============================================================
-- Day05 PIM + OMS 建表脚本
-- ============================================================

CREATE TABLE IF NOT EXISTS `product_category`
(
    `id`               BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`        BIGINT          NOT NULL COMMENT '租户ID',
    `parent_id`        BIGINT          NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `category_name`    VARCHAR(64)     NOT NULL COMMENT '分类名称',
    `category_name_en` VARCHAR(64)     NULL COMMENT '英文分类名称',
    `level`            TINYINT         NOT NULL DEFAULT 1 COMMENT '层级深度',
    `path`             VARCHAR(256)    NOT NULL DEFAULT '/' COMMENT '分类路径',
    `icon_url`         VARCHAR(512)    NULL COMMENT '分类图标',
    `sort_order`       INT             NOT NULL DEFAULT 0 COMMENT '排序',
    `status`           TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`        BIGINT          NULL,
    `update_by`        BIGINT          NULL,
    `is_deleted`       TINYINT(1)      NOT NULL DEFAULT 0,
    `version`          INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_tenant_parent` (`tenant_id`, `parent_id`),
    KEY `idx_path` (`path`(128))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表';

CREATE TABLE IF NOT EXISTS `product_spu`
(
    `id`              BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`       BIGINT          NOT NULL COMMENT '租户ID',
    `spu_code`        VARCHAR(32)     NOT NULL COMMENT 'SPU编码',
    `spu_name`        VARCHAR(256)    NOT NULL COMMENT 'SPU名称',
    `category_id`     BIGINT          NOT NULL COMMENT '分类ID',
    `category_path`   VARCHAR(256)    NOT NULL COMMENT '分类路径',
    `brand`           VARCHAR(64)     NULL COMMENT '品牌',
    `hs_code`         VARCHAR(16)     NULL COMMENT 'HS编码',
    `origin_country`  CHAR(2)         NULL COMMENT '原产国',
    `material`        VARCHAR(128)    NULL COMMENT '材质',
    `certifications`  JSON            NULL COMMENT '认证列表',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=待审核 2=已上架 3=已下架 4=已停售',
    `publish_time`    DATETIME        NULL COMMENT '上架时间',
    `shelf_off_time`  DATETIME        NULL COMMENT '下架时间',
    `spu_desc`        TEXT            NULL COMMENT '商品描述',
    `package_desc`    VARCHAR(512)    NULL COMMENT '包装描述',
    `remark`          VARCHAR(512)    NULL COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT          NULL,
    `update_by`       BIGINT          NULL,
    `is_deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
    `version`         INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_spu_code` (`tenant_id`, `spu_code`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_tenant_category` (`tenant_id`, `category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SPU商品标准单元表';

CREATE TABLE IF NOT EXISTS `product_sku`
(
    `id`              BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`       BIGINT          NOT NULL COMMENT '租户ID',
    `spu_id`          BIGINT          NOT NULL COMMENT 'SPU ID',
    `sku_code`        VARCHAR(64)     NOT NULL COMMENT 'SKU编码',
    `sku_name`        VARCHAR(256)    NOT NULL COMMENT 'SKU名称',
    `barcode`         VARCHAR(64)     NULL COMMENT '条形码',
    `fnsku`           VARCHAR(64)     NULL COMMENT '亚马逊仓储条码',
    `spec_values`     JSON            NOT NULL COMMENT '中文规格JSON',
    `spec_values_en`  JSON            NULL COMMENT '英文规格JSON',
    `net_weight_g`    DECIMAL(10, 2)  NULL COMMENT '净重克',
    `gross_weight_g`  DECIMAL(10, 2)  NULL COMMENT '毛重克',
    `length_mm`       DECIMAL(10, 2)  NULL COMMENT '长毫米',
    `width_mm`        DECIMAL(10, 2)  NULL COMMENT '宽毫米',
    `height_mm`       DECIMAL(10, 2)  NULL COMMENT '高毫米',
    `is_battery`      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否含电池',
    `is_liquid`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否液体',
    `is_powder`       TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否粉末',
    `cost_price`      DECIMAL(12, 4)  NULL COMMENT '成本价',
    `cost_currency`   CHAR(3)         NOT NULL DEFAULT 'CNY' COMMENT '成本价币种',
    `abc_class`       CHAR(1)         NULL COMMENT 'ABC分类',
    `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已上架 2=已下架 3=已停售',
    `remark`          VARCHAR(256)    NULL COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT          NULL,
    `update_by`       BIGINT          NULL,
    `is_deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
    `version`         INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_sku_code` (`tenant_id`, `sku_code`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SKU最小库存单元表';

CREATE TABLE IF NOT EXISTS `product_attr_template`
(
    `id`              BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`       BIGINT          NOT NULL COMMENT '租户ID',
    `category_id`     BIGINT          NOT NULL COMMENT '分类ID',
    `attr_name`       VARCHAR(64)     NOT NULL COMMENT '属性名称',
    `attr_name_en`    VARCHAR(64)     NULL COMMENT '属性英文名',
    `attr_type`       TINYINT         NOT NULL DEFAULT 1 COMMENT '属性类型',
    `attr_options`    JSON            NULL COMMENT '选项JSON',
    `attr_unit`       VARCHAR(16)     NULL COMMENT '单位',
    `is_sku_spec`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否SKU规格',
    `is_required`     TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否必填',
    `sort_order`      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    `status`          TINYINT         NOT NULL DEFAULT 1 COMMENT '状态',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       BIGINT          NULL,
    `update_by`       BIGINT          NULL,
    `is_deleted`      TINYINT(1)      NOT NULL DEFAULT 0,
    `version`         INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分类属性模板表';

CREATE TABLE IF NOT EXISTS `product_sku_price`
(
    `id`             BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`      BIGINT          NOT NULL COMMENT '租户ID',
    `sku_id`         BIGINT          NOT NULL COMMENT 'SKU ID',
    `price_type`     TINYINT         NOT NULL COMMENT '价格类型',
    `platform`       VARCHAR(32)     NULL COMMENT '平台',
    `country_code`   CHAR(2)         NULL COMMENT '国家',
    `price`          DECIMAL(12, 4)  NOT NULL COMMENT '价格',
    `currency`       CHAR(3)         NOT NULL COMMENT '币种',
    `min_qty`        INT             NOT NULL DEFAULT 1 COMMENT '最小数量',
    `max_qty`        INT             NULL COMMENT '最大数量',
    `effective_time` DATETIME        NULL COMMENT '生效时间',
    `expire_time`    DATETIME        NULL COMMENT '失效时间',
    `is_active`      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否生效',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      BIGINT          NULL,
    `update_by`      BIGINT          NULL,
    `is_deleted`     TINYINT(1)      NOT NULL DEFAULT 0,
    `version`        INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sku_platform` (`sku_id`, `platform`, `country_code`, `price_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SKU多平台价格表';

CREATE TABLE IF NOT EXISTS `product_i18n`
(
    `id`               BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`        BIGINT          NOT NULL COMMENT '租户ID',
    `ref_type`         VARCHAR(16)     NOT NULL COMMENT '关联类型',
    `ref_id`           BIGINT          NOT NULL COMMENT '关联ID',
    `lang_code`        VARCHAR(8)      NOT NULL COMMENT '语言代码',
    `title`            VARCHAR(512)    NULL COMMENT '标题',
    `subtitle`         VARCHAR(256)    NULL COMMENT '副标题',
    `bullet_points`    JSON            NULL COMMENT '卖点',
    `description`      TEXT            NULL COMMENT '描述',
    `keywords`         VARCHAR(1024)   NULL COMMENT '关键词',
    `search_terms`     VARCHAR(1024)   NULL COMMENT '搜索词',
    `is_ai_translated` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否AI翻译',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`        BIGINT          NULL,
    `update_by`        BIGINT          NULL,
    `is_deleted`       TINYINT(1)      NOT NULL DEFAULT 0,
    `version`          INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ref_lang` (`ref_type`, `ref_id`, `lang_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品多语言内容表';

CREATE TABLE IF NOT EXISTS `product_image`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`     BIGINT          NOT NULL COMMENT '租户ID',
    `spu_id`        BIGINT          NOT NULL COMMENT 'SPU ID',
    `sku_id`        BIGINT          NULL COMMENT 'SKU ID',
    `image_type`    TINYINT         NOT NULL COMMENT '图片类型',
    `image_url`     VARCHAR(512)    NOT NULL COMMENT '图片URL',
    `thumb_url`     VARCHAR(512)    NULL COMMENT '缩略图',
    `image_width`   INT             NULL COMMENT '宽度',
    `image_height`  INT             NULL COMMENT '高度',
    `file_size`     BIGINT          NULL COMMENT '文件大小',
    `sort_order`    INT             NOT NULL DEFAULT 0 COMMENT '排序',
    `alt_text`      VARCHAR(128)    NULL COMMENT 'ALT文案',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     BIGINT          NULL,
    `update_by`     BIGINT          NULL,
    `is_deleted`    TINYINT(1)      NOT NULL DEFAULT 0,
    `version`       INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品图片表';

CREATE TABLE IF NOT EXISTS `order_main`
(
    `id`                  BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`           BIGINT          NOT NULL COMMENT '租户ID',
    `order_no`            VARCHAR(32)     NOT NULL COMMENT '内部订单号',
    `platform`            VARCHAR(32)     NOT NULL COMMENT '平台',
    `platform_order_no`   VARCHAR(128)    NOT NULL COMMENT '平台订单号',
    `store_id`            BIGINT          NULL COMMENT '店铺ID',
    `total_amount`        DECIMAL(12, 2)  NOT NULL COMMENT '总金额',
    `discount_amount`     DECIMAL(12, 2)  NOT NULL DEFAULT 0 COMMENT '优惠金额',
    `shipping_fee`        DECIMAL(12, 2)  NOT NULL DEFAULT 0 COMMENT '运费',
    `payment_amount`      DECIMAL(12, 2)  NOT NULL COMMENT '实付金额',
    `currency`            CHAR(3)         NOT NULL COMMENT '币种',
    `exchange_rate`       DECIMAL(10, 6)  NOT NULL DEFAULT 1 COMMENT '汇率',
    `cny_amount`          DECIMAL(12, 2)  NOT NULL DEFAULT 0 COMMENT '人民币金额',
    `platform_fee`        DECIMAL(12, 2)  NOT NULL DEFAULT 0 COMMENT '平台手续费',
    `status`              TINYINT         NOT NULL DEFAULT 0 COMMENT '订单状态',
    `cancel_reason`       VARCHAR(256)    NULL COMMENT '取消原因',
    `is_abnormal`         TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否异常',
    `abnormal_reason`     VARCHAR(256)    NULL COMMENT '异常原因',
    `warehouse_id`        BIGINT          NULL COMMENT '仓库ID',
    `logistics_channel`   VARCHAR(64)     NULL COMMENT '物流渠道',
    `waybill_no`          VARCHAR(128)    NULL COMMENT '运单号',
    `ship_time`           DATETIME        NULL COMMENT '发货时间',
    `delivery_deadline`   DATE            NULL COMMENT '最晚发货日期',
    `signed_time`         DATETIME        NULL COMMENT '签收时间',
    `platform_order_time` DATETIME        NOT NULL COMMENT '平台下单时间',
    `platform_pay_time`   DATETIME        NULL COMMENT '平台支付时间',
    `create_time`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`           BIGINT          NULL,
    `update_by`           BIGINT          NULL,
    `is_deleted`          TINYINT(1)      NOT NULL DEFAULT 0,
    `version`             INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_order_no` (`tenant_id`, `order_no`),
    UNIQUE KEY `uk_platform_order_no` (`tenant_id`, `platform`, `platform_order_no`),
    KEY `idx_tenant_status` (`tenant_id`, `status`),
    KEY `idx_delivery_deadline` (`delivery_deadline`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单主表';

CREATE TABLE IF NOT EXISTS `order_item`
(
    `id`              BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`       BIGINT          NOT NULL COMMENT '租户ID',
    `order_id`        BIGINT          NOT NULL COMMENT '订单ID',
    `order_no`        VARCHAR(32)     NOT NULL COMMENT '订单号',
    `sku_id`          BIGINT          NOT NULL COMMENT 'SKU ID',
    `sku_code`        VARCHAR(64)     NOT NULL COMMENT 'SKU编码',
    `sku_name`        VARCHAR(256)    NOT NULL COMMENT 'SKU名称',
    `platform_sku_id` VARCHAR(128)    NULL COMMENT '平台SKU',
    `quantity`        INT             NOT NULL COMMENT '数量',
    `unit_price`      DECIMAL(12, 4)  NOT NULL COMMENT '单价',
    `discount`        DECIMAL(12, 2)  NOT NULL DEFAULT 0 COMMENT '优惠',
    `amount`          DECIMAL(12, 2)  NOT NULL COMMENT '小计',
    `currency`        CHAR(3)         NOT NULL COMMENT '币种',
    `refunded_qty`    INT             NOT NULL DEFAULT 0 COMMENT '已退款数量',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单明细表';

CREATE TABLE IF NOT EXISTS `order_address`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`     BIGINT          NOT NULL COMMENT '租户ID',
    `order_id`      BIGINT          NOT NULL COMMENT '订单ID',
    `receiver_name` VARCHAR(128)    NOT NULL COMMENT '收件人',
    `phone`         VARCHAR(32)     NULL COMMENT '电话',
    `email`         VARCHAR(128)    NULL COMMENT '邮箱',
    `country_code`  CHAR(2)         NOT NULL COMMENT '国家代码',
    `country_name`  VARCHAR(64)     NULL COMMENT '国家',
    `state`         VARCHAR(64)     NULL COMMENT '州省',
    `city`          VARCHAR(64)     NULL COMMENT '城市',
    `address_line1` VARCHAR(256)    NOT NULL COMMENT '地址1',
    `address_line2` VARCHAR(256)    NULL COMMENT '地址2',
    `zip_code`      VARCHAR(16)     NOT NULL COMMENT '邮编',
    `full_address`  VARCHAR(512)    NOT NULL COMMENT '完整地址',
    `is_verified`   TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否验证',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单收货地址表';

CREATE TABLE IF NOT EXISTS `order_log`
(
    `id`            BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`     BIGINT          NOT NULL COMMENT '租户ID',
    `order_id`      BIGINT          NOT NULL COMMENT '订单ID',
    `order_no`      VARCHAR(32)     NOT NULL COMMENT '订单号',
    `from_status`   TINYINT         NULL COMMENT '原状态',
    `to_status`     TINYINT         NOT NULL COMMENT '新状态',
    `action`        VARCHAR(128)    NOT NULL COMMENT '动作',
    `operator_type` TINYINT         NOT NULL DEFAULT 1 COMMENT '操作人类型',
    `operator_id`   BIGINT          NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64)     NULL COMMENT '操作人',
    `remark`        VARCHAR(512)    NULL COMMENT '备注',
    `operate_time`  DATETIME        NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单操作日志表';

CREATE TABLE IF NOT EXISTS `order_refund`
(
    `id`                   BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`            BIGINT          NOT NULL COMMENT '租户ID',
    `refund_no`            VARCHAR(32)     NOT NULL COMMENT '退款单号',
    `order_id`             BIGINT          NOT NULL COMMENT '订单ID',
    `order_no`             VARCHAR(32)     NOT NULL COMMENT '订单号',
    `platform_refund_no`   VARCHAR(128)    NULL COMMENT '平台退款号',
    `refund_type`          TINYINT         NOT NULL COMMENT '退款类型',
    `refund_reason`        VARCHAR(32)     NOT NULL COMMENT '原因',
    `reason_detail`        VARCHAR(512)    NULL COMMENT '原因详情',
    `refund_amount`        DECIMAL(12, 2)  NOT NULL COMMENT '申请金额',
    `actual_refund_amount` DECIMAL(12, 2)  NULL COMMENT '实际金额',
    `currency`             CHAR(3)         NOT NULL COMMENT '币种',
    `status`               TINYINT         NOT NULL DEFAULT 0 COMMENT '状态',
    `apply_time`           DATETIME        NOT NULL COMMENT '申请时间',
    `audit_time`           DATETIME        NULL COMMENT '审核时间',
    `complete_time`        DATETIME        NULL COMMENT '完成时间',
    `evidence_urls`        JSON            NULL COMMENT '凭证',
    `return_tracking_no`   VARCHAR(128)    NULL COMMENT '退货单号',
    `create_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`            BIGINT          NULL,
    `update_by`            BIGINT          NULL,
    `is_deleted`           TINYINT(1)      NOT NULL DEFAULT 0,
    `version`              INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_refund_no` (`tenant_id`, `refund_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退款单表';

CREATE TABLE IF NOT EXISTS `order_platform_raw`
(
    `id`                BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`         BIGINT          NOT NULL COMMENT '租户ID',
    `order_id`          BIGINT          NULL COMMENT '订单ID',
    `platform`          VARCHAR(32)     NOT NULL COMMENT '平台',
    `platform_order_no` VARCHAR(128)    NOT NULL COMMENT '平台订单号',
    `raw_data`          MEDIUMTEXT      NOT NULL COMMENT '原始JSON',
    `sync_time`         DATETIME        NOT NULL COMMENT '同步时间',
    `sync_type`         TINYINT         NOT NULL DEFAULT 1 COMMENT '同步方式',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_platform_order_no` (`platform_order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '平台订单原始数据表';

CREATE TABLE IF NOT EXISTS `inventory_platform_allocation`
(
    `id`                BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
    `tenant_id`         BIGINT          NOT NULL COMMENT '租户ID',
    `sku_id`            BIGINT          NOT NULL COMMENT 'SKU ID',
    `sku_code`          VARCHAR(64)     NOT NULL COMMENT 'SKU编码',
    `platform`          VARCHAR(32)     NOT NULL COMMENT '平台编码：AMAZON/SHOPIFY/EBAY/TIKTOK等',
    `store_id`          BIGINT          NOT NULL DEFAULT 0 COMMENT '店铺ID，0表示平台通用配额',
    `allocated_qty`     INT             NOT NULL DEFAULT 0 COMMENT '分配库存数量',
    `frozen_qty`        INT             NOT NULL DEFAULT 0 COMMENT '已冻结数量，平台订单创建后冻结',
    `available_qty`     INT             NOT NULL DEFAULT 0 COMMENT '平台可售数量',
    `sold_qty`          INT             NOT NULL DEFAULT 0 COMMENT '已销售数量，订单出库后确认',
    `allocation_ratio`  INT             NULL COMMENT '分配比例，0-100',
    `status`            TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
    `last_sync_status`  TINYINT         NOT NULL DEFAULT 0 COMMENT '最近同步状态：0=未同步 1=成功 2=失败',
    `last_sync_time`    DATETIME        NULL COMMENT '最近同步时间',
    `last_sync_message` VARCHAR(512)    NULL COMMENT '最近同步结果',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         BIGINT          NULL,
    `update_by`         BIGINT          NULL,
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
    `version`           INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_sku_platform_store` (`tenant_id`, `sku_id`, `platform`, `store_id`),
    KEY `idx_tenant_platform` (`tenant_id`, `platform`),
    KEY `idx_tenant_sku` (`tenant_id`, `sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '平台库存分配表';
