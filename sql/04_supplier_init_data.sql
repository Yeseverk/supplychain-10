USE `supplychain_dev`;

-- 供应商相关字典类型
INSERT IGNORE INTO `sys_dict_type` (`id`, `dict_name`, `dict_code`, `status`, `remark`)
VALUES
(10, '供应商资质类型', 'supplier_cert_type', 1, '供应商可上传的资质文件种类'),
(11, '供应商联系人类型', 'supplier_contact_type', 1, '联系人的业务职能分类'),
(12, '公司规模', 'company_size', 1, '供应商公司的员工人数规模');

-- 供应商相关字典明细
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_type_id`, `dict_code`, `item_value`, `item_label`, `item_label_en`, `sort`, `css_class`, `status`)
VALUES
(1001, 10, 'supplier_cert_type', '1', '营业执照', 'Business License', 1, 'primary', 1),
(1002, 10, 'supplier_cert_type', '2', '质检报告', 'Quality Report', 2, 'success', 1),
(1003, 10, 'supplier_cert_type', '3', '产品认证', 'Product Certification', 3, 'warning', 1),
(1004, 10, 'supplier_cert_type', '4', '银行账户证明', 'Bank Certificate', 4, 'info', 1),
(1005, 10, 'supplier_cert_type', '5', '其他文件', 'Other', 5, '', 1),
(1101, 11, 'supplier_contact_type', '1', '业务对接', 'Business', 1, 'primary', 1),
(1102, 11, 'supplier_contact_type', '2', '财务对账', 'Finance', 2, 'success', 1),
(1103, 11, 'supplier_contact_type', '3', '技术支持', 'Technical', 3, 'info', 1),
(1104, 11, 'supplier_contact_type', '4', '紧急联系', 'Emergency', 4, 'danger', 1),
(1201, 12, 'company_size', '1', '50人以下', '<50', 1, '', 1),
(1202, 12, 'company_size', '2', '50-200人', '50-200', 2, '', 1),
(1203, 12, 'company_size', '3', '200-500人', '200-500', 3, '', 1),
(1204, 12, 'company_size', '4', '500人以上', '>500', 4, '', 1);

-- 供应商租户级策略默认配置示例，租户可按自身采购策略调整
-- 给租户去配置的策略 每个租户可自行配置最低等级和最低供应商数量
INSERT IGNORE INTO `supplier_tenant_config` (`id`, `tenant_id`, `config_key`, `config_value`, `config_desc`, `enabled`)
VALUES
(2001, 101, 'supplier.layering.watchlist.grades', 'B,C', '进入重点观察名单的供应商评级，系统建议值为B,C', 1),
(2002, 101, 'supplier.multi.min_count', '2', '同一品类建议维护的最少可用供应商数量', 1),
(2003, 101, 'supplier.multi.min_healthy_grade', 'B', '同一品类供应商建议达到的最低健康评级', 1);
