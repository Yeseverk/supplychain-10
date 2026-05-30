USE `supplychain_dev`;

-- 初始化菜单数据
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `component`, `icon`, `sort`, `is_visible`, `status`)
VALUES
(100, 0, '供应链管理', 1, NULL, '/supply', NULL, 'Box', 1, 1, 1),
(200, 0, '商品中心', 1, NULL, '/product', NULL, 'Goods', 2, 1, 1),
(300, 0, '订单中心', 1, NULL, '/order', NULL, 'List', 3, 1, 1),
(400, 0, '物流管理', 1, NULL, '/logistics', NULL, 'Van', 4, 1, 1),
(500, 0, '财务中心', 1, NULL, '/finance', NULL, 'Money', 5, 1, 1),
(600, 0, '数据分析', 1, NULL, '/bi', NULL, 'DataAnalysis', 6, 1, 1),
(900, 0, '系统设置', 1, NULL, '/system', NULL, 'Setting', 9, 1, 1),
(101, 100, '供应商管理', 2, NULL, '/supply/supplier', 'srm/supplier/index', NULL, 1, 1, 1),
(102, 100, '采购管理', 2, NULL, '/supply/purchase', 'pms/purchase/index', NULL, 2, 1, 1),
(103, 100, '仓库管理', 2, NULL, '/supply/warehouse', 'wms/warehouse/index', NULL, 3, 1, 1),
(104, 100, '库存管理', 2, NULL, '/supply/inventory', 'wms/inventory/index', NULL, 4, 1, 1),
(10101, 101, '查看供应商', 3, 'srm:supplier:list', NULL, NULL, NULL, 1, 0, 1),
(10102, 101, '新增供应商', 3, 'srm:supplier:add', NULL, NULL, NULL, 2, 0, 1),
(10103, 101, '编辑供应商', 3, 'srm:supplier:edit', NULL, NULL, NULL, 3, 0, 1),
(10104, 101, '删除供应商', 3, 'srm:supplier:delete', NULL, NULL, NULL, 4, 0, 1),
(10105, 101, '审核供应商', 3, 'srm:supplier:audit', NULL, NULL, NULL, 5, 0, 1),
(901, 900, '用户管理', 2, NULL, '/system/user', 'system/user/index', NULL, 1, 1, 1),
(902, 900, '角色管理', 2, NULL, '/system/role', 'system/role/index', NULL, 2, 1, 1),
(903, 900, '菜单管理', 2, NULL, '/system/menu', 'system/menu/index', NULL, 3, 1, 1),
(904, 900, '数据字典', 2, NULL, '/system/dict', 'system/dict/index', NULL, 4, 1, 1),
(905, 900, '审计日志', 2, NULL, '/system/audit', 'system/audit/index', NULL, 5, 1, 1)
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);

-- 初始化角色数据
INSERT INTO `sys_role` (`id`, `tenant_id`, `role_name`, `role_code`, `role_type`, `data_scope`, `sort`, `status`, `remark`)
VALUES
(1, 0, '超级管理员', 'ROLE_SUPER_ADMIN', 2, 1, 0, 1, '平台超级管理员，拥有全部权限，不属于任何租户'),
(2, 0, '租户管理员', 'ROLE_TENANT_ADMIN', 2, 1, 1, 1, '租户内最高权限管理员，可管理本租户所有功能'),
(3, 0, '采购专员', 'ROLE_PURCHASE', 2, 3, 2, 1, '负责供应商管理和采购下单'),
(4, 0, '仓储管理员', 'ROLE_WAREHOUSE', 2, 1, 3, 1, '负责仓库日常运营，出入库操作'),
(5, 0, '运营专员', 'ROLE_OPERATION', 2, 3, 4, 1, '负责商品上架和订单处理'),
(6, 0, '物流专员', 'ROLE_LOGISTICS', 2, 3, 5, 1, '负责运单创建和物流跟踪'),
(7, 0, '财务专员', 'ROLE_FINANCE', 2, 1, 6, 1, '负责对账和利润核算'),
(8, 0, '供应商', 'ROLE_SUPPLIER', 2, 3, 7, 1, '供应商Portal专属角色，权限受严格限制')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 初始化数据字典类型
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_code`, `status`, `remark`)
VALUES
(1, '供应商类型', 'supplier_type', 1, '供应商的业务类型分类'),
(2, '供应商状态', 'supplier_status', 1, '供应商的审核和合作状态'),
(3, '供应商评级', 'supplier_grade', 1, '供应商的综合绩效评级'),
(4, '采购单状态', 'purchase_order_status', 1, '采购订单的全状态定义'),
(5, '仓库类型', 'warehouse_type', 1, '仓库的业务类型'),
(6, '库存流水类型', 'inventory_log_type', 1, '库存变动的操作类型'),
(7, '订单状态', 'order_status', 1, '销售订单的全状态定义'),
(8, '物流渠道类型', 'logistics_type', 1, '物流服务的类型分类'),
(9, '币种', 'currency', 1, '系统支持的结算货币')
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

-- 初始化数据字典明细
INSERT INTO `sys_dict_item` (`id`, `dict_type_id`, `dict_code`, `item_value`, `item_label`, `item_label_en`, `sort`, `css_class`, `status`)
VALUES
(101, 1, 'supplier_type', '1', '工厂供应商', 'Factory', 1, 'success', 1),
(102, 1, 'supplier_type', '2', '贸易商', 'Trader', 2, 'primary', 1),
(103, 1, 'supplier_type', '3', '物流服务商', 'Logistics Provider', 3, 'info', 1),
(201, 2, 'supplier_status', '0', '草稿', 'Draft', 1, 'info', 1),
(202, 2, 'supplier_status', '1', '待审核', 'Pending', 2, 'warning', 1),
(203, 2, 'supplier_status', '2', '已通过', 'Approved', 3, 'success', 1),
(204, 2, 'supplier_status', '3', '已拒绝', 'Rejected', 4, 'danger', 1),
(205, 2, 'supplier_status', '4', '已停用', 'Disabled', 5, '', 1),
(301, 3, 'supplier_grade', 'S', 'S级-优质', 'Premium', 1, 'success', 1),
(302, 3, 'supplier_grade', 'A', 'A级-良好', 'Good', 2, 'primary', 1),
(303, 3, 'supplier_grade', 'B', 'B级-一般', 'Fair', 3, 'warning', 1),
(304, 3, 'supplier_grade', 'C', 'C级-预警', 'Alert', 4, 'danger', 1),
(901, 9, 'currency', 'CNY', '人民币', 'Chinese Yuan', 1, '', 1),
(902, 9, 'currency', 'USD', '美元', 'US Dollar', 2, '', 1),
(903, 9, 'currency', 'EUR', '欧元', 'Euro', 3, '', 1),
(904, 9, 'currency', 'GBP', '英镑', 'British Pound', 4, '', 1),
(905, 9, 'currency', 'JPY', '日元', 'Japanese Yen', 5, '', 1)
ON DUPLICATE KEY UPDATE `item_label` = VALUES(`item_label`);
