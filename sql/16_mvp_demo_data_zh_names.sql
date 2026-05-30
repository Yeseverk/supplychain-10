USE `supplychain_dev`;
SET NAMES utf8mb4;

SET @tenant_id := 2059984036520636418;

SET @sup_elec := 910000000000000101;
SET @sup_pack := 910000000000000102;
SET @sup_outdoor := 910000000000000103;
SET @cat_pet := 910000000000000201;
SET @cat_pack := 910000000000000202;
SET @cat_outdoor := 910000000000000203;
SET @spu_pet_dryer := 910000000000000301;
SET @spu_pack_box := 910000000000000302;
SET @spu_camp_light := 910000000000000303;
SET @sku_pet_white := 910000000000000401;
SET @sku_pet_gray := 910000000000000402;
SET @sku_pack_box := 910000000000000403;
SET @sku_camp_light := 910000000000000404;
SET @wh_la := 910000000000000501;
SET @wh_nj := 910000000000000502;
SET @wh_de := 910000000000000503;

UPDATE supplier
SET supplier_name='深圳华景电子有限公司',
    province='广东省',
    city='深圳市',
    address='宝安区航城街道智造园 8 栋',
    contact_name='陈敏',
    bank_account_name='深圳华景电子有限公司',
    audit_remark='资质完整，交付稳定',
    remark='核心电子类供应商'
WHERE tenant_id=@tenant_id AND id=@sup_elec;

UPDATE supplier
SET supplier_name='宁波远航包装厂',
    province='浙江省',
    city='宁波市',
    address='北仑区春晓工业园 12 号',
    contact_name='周航',
    bank_account_name='宁波远航包装厂',
    audit_remark='价格稳定，旺季需提前锁定产能',
    remark='包装耗材长期合作供应商'
WHERE tenant_id=@tenant_id AND id=@sup_pack;

UPDATE supplier
SET supplier_name='厦门森海户外用品有限公司',
    province='福建省',
    city='厦门市',
    address='集美区杏林湾商务运营中心',
    contact_name='林启',
    bank_account_name='厦门森海户外用品有限公司',
    audit_remark='欧洲订单交期偏长，需要重点跟进',
    remark='户外用品供应商'
WHERE tenant_id=@tenant_id AND id=@sup_outdoor;

UPDATE supplier_contact SET contact_name='陈敏', position='销售总监', department='销售部', remark='主联系人' WHERE tenant_id=@tenant_id AND supplier_id=@sup_elec AND contact_type=1;
UPDATE supplier_contact SET contact_name='周航', position='厂长', department='业务部', remark='包装订单联系人' WHERE tenant_id=@tenant_id AND supplier_id=@sup_pack;
UPDATE supplier_contact SET contact_name='林启', position='外贸经理', department='外贸部', remark='欧洲订单联系人' WHERE tenant_id=@tenant_id AND supplier_id=@sup_outdoor;

UPDATE product_category SET category_name='宠物电器', category_name_en='Pet Appliance' WHERE tenant_id=@tenant_id AND id=@cat_pet;
UPDATE product_category SET category_name='包装耗材', category_name_en='Packaging Consumables' WHERE tenant_id=@tenant_id AND id=@cat_pack;
UPDATE product_category SET category_name='户外装备', category_name_en='Outdoor Gear' WHERE tenant_id=@tenant_id AND id=@cat_outdoor;

UPDATE product_spu
SET spu_name='AX9 多规格宠物烘干箱',
    material='ABS、钢化玻璃、电子元件',
    spu_desc='面向跨境平台销售的多规格宠物烘干箱',
    package_desc='泡棉防护外箱',
    remark='美国 TikTok 热销 SKU'
WHERE tenant_id=@tenant_id AND id=@spu_pet_dryer;

UPDATE product_spu
SET spu_name='40x30x28 抗压快递纸箱',
    material='五层瓦楞纸',
    spu_desc='仓库出库使用的包装耗材',
    package_desc='扁平打包成捆',
    remark='仓储耗材'
WHERE tenant_id=@tenant_id AND id=@spu_pack_box;

UPDATE product_spu
SET spu_name='折叠式户外储能灯',
    material='铝合金、锂电池',
    spu_desc='可充电户外露营灯',
    package_desc='零售彩盒',
    remark='欧洲季节性 SKU'
WHERE tenant_id=@tenant_id AND id=@spu_camp_light;

UPDATE product_sku SET sku_name='AX9 宠物烘干箱 白色 110V', remark='美国热销款' WHERE tenant_id=@tenant_id AND id=@sku_pet_white;
UPDATE product_sku SET sku_name='AX9 宠物烘干箱 灰色 110V', remark='美国备选颜色款' WHERE tenant_id=@tenant_id AND id=@sku_pet_gray;
UPDATE product_sku SET sku_name='抗压快递纸箱 40x30x28cm', remark='包装耗材' WHERE tenant_id=@tenant_id AND id=@sku_pack_box;
UPDATE product_sku SET sku_name='折叠式户外储能灯 欧规黑色', remark='欧洲含电 SKU' WHERE tenant_id=@tenant_id AND id=@sku_camp_light;

UPDATE warehouse
SET warehouse_name='洛杉矶海外仓',
    country_name='美国',
    province='加利福尼亚州',
    city='洛杉矶',
    address='1688 Supply Chain Ave',
    contact_name='Mike Lee',
    remark='美国主力发货仓'
WHERE tenant_id=@tenant_id AND id=@wh_la;

UPDATE warehouse
SET warehouse_name='宁波国内仓',
    country_name='中国',
    province='浙江省',
    city='宁波市',
    address='北仑保税区 6 号',
    contact_name='王鑫',
    remark='国内集货中转仓'
WHERE tenant_id=@tenant_id AND id=@wh_nj;

UPDATE warehouse
SET warehouse_name='汉堡欧洲仓',
    country_name='德国',
    province='汉堡',
    city='汉堡',
    address='HafenCity 18',
    contact_name='Anna Becker',
    remark='欧洲区域仓'
WHERE tenant_id=@tenant_id AND id=@wh_de;

UPDATE warehouse_location SET remark='快进快出库区' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_la AND location_code='A-02-06';
UPDATE warehouse_location SET remark='冻结库存库区' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_la AND location_code='B-01-03';
UPDATE warehouse_location SET remark='包装耗材库区' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_nj AND location_code='P-04-11';
UPDATE warehouse_location SET remark='欧洲灯具库区' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_de AND location_code='E-03-02';

UPDATE inventory SET sku_name='AX9 宠物烘干箱 白色 110V' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE inventory SET sku_name='AX9 宠物烘干箱 灰色 110V' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_gray;
UPDATE inventory SET sku_name='抗压快递纸箱 40x30x28cm' WHERE tenant_id=@tenant_id AND sku_id=@sku_pack_box;
UPDATE inventory SET sku_name='折叠式户外储能灯 欧规黑色' WHERE tenant_id=@tenant_id AND sku_id=@sku_camp_light;

UPDATE inventory_log SET sku_name='AX9 宠物烘干箱 白色 110V', operator_name='管理员', remark='采购入库' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white AND log_type=1;
UPDATE inventory_log SET sku_name='AX9 宠物烘干箱 白色 110V', operator_name='管理员', remark='销售出库' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white AND log_type=2;
UPDATE inventory_log SET sku_name='抗压快递纸箱 40x30x28cm', operator_name='管理员', remark='包装耗材入库' WHERE tenant_id=@tenant_id AND sku_id=@sku_pack_box;
UPDATE inventory_log SET sku_name='折叠式户外储能灯 欧规黑色', operator_name='管理员', remark='欧洲订单清空库存' WHERE tenant_id=@tenant_id AND sku_id=@sku_camp_light;

UPDATE purchase_requisition SET title='美国宠物烘干箱补货申请', remark='由低库存预警触发', audit_remark='为降低断货风险，审批通过' WHERE tenant_id=@tenant_id;
UPDATE purchase_requisition_item SET sku_name='AX9 宠物烘干箱 白色 110V', remark='维持 30 天库存覆盖' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE purchase_requisition_item SET sku_name='AX9 宠物烘干箱 灰色 110V', remark='低库存颜色款' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_gray;

UPDATE purchase_inquiry SET supplier_name='深圳华景电子有限公司', remark='六月补货询价对比', supplier_remark='可分两批发货' WHERE tenant_id=@tenant_id AND supplier_id=@sup_elec;
UPDATE purchase_inquiry_item SET sku_name='AX9 宠物烘干箱 白色 110V', remark='工厂现货可排产' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE purchase_inquiry_item SET sku_name='AX9 宠物烘干箱 灰色 110V', remark='需确认灰色外壳库存' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_gray;

UPDATE purchase_order SET supplier_name='深圳华景电子有限公司', warehouse_name='洛杉矶海外仓', remark='首批部分到货' WHERE tenant_id=@tenant_id AND supplier_id=@sup_elec;
UPDATE purchase_order SET supplier_name='宁波远航包装厂', warehouse_name='宁波国内仓', remark='包装耗材已到货，待结算' WHERE tenant_id=@tenant_id AND supplier_id=@sup_pack;
UPDATE purchase_order SET supplier_name='厦门森海户外用品有限公司', warehouse_name='汉堡欧洲仓', remark='欧洲含电 SKU 生产中' WHERE tenant_id=@tenant_id AND supplier_id=@sup_outdoor;

UPDATE purchase_order_item SET sku_name='AX9 宠物烘干箱 白色 110V', spec='白色 / 110V', unit='件', remark='首批到货 120 件' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE purchase_order_item SET sku_name='AX9 宠物烘干箱 灰色 110V', spec='灰色 / 110V', unit='件', remark='待到货' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_gray;
UPDATE purchase_order_item SET sku_name='抗压快递纸箱 40x30x28cm', spec='五层瓦楞纸箱', unit='个', remark='部分入库' WHERE tenant_id=@tenant_id AND sku_id=@sku_pack_box;
UPDATE purchase_order_item SET sku_name='折叠式户外储能灯 欧规黑色', spec='黑色 / 欧规插头', unit='件', remark='生产中' WHERE tenant_id=@tenant_id AND sku_id=@sku_camp_light;

UPDATE purchase_receipt SET receiver_name='管理员', remark='首批到达洛杉矶海外仓' WHERE tenant_id=@tenant_id AND supplier_id=@sup_elec;
UPDATE purchase_receipt SET receiver_name='管理员', remark='包装耗材批次到货' WHERE tenant_id=@tenant_id AND supplier_id=@sup_pack;
UPDATE purchase_receipt_item SET sku_name='AX9 宠物烘干箱 白色 110V', reject_reason='外箱破损' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE purchase_receipt_item SET sku_name='抗压快递纸箱 40x30x28cm', reject_reason='边角受潮' WHERE tenant_id=@tenant_id AND sku_id=@sku_pack_box;

UPDATE inbound_order SET warehouse_name='洛杉矶海外仓', remark='采购单首批部分上架' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_la;
UPDATE inbound_order SET warehouse_name='宁波国内仓', remark='包装耗材批次入库' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_nj;
UPDATE inbound_order_item SET sku_name='AX9 宠物烘干箱 白色 110V', remark='首批已上架' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE inbound_order_item SET sku_name='AX9 宠物烘干箱 灰色 110V', remark='待到货' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_gray;
UPDATE inbound_order_item SET sku_name='抗压快递纸箱 40x30x28cm', remark='首批纸箱已入库' WHERE tenant_id=@tenant_id AND sku_id=@sku_pack_box;

UPDATE stocktake_task SET task_name='汉堡仓月度循环盘点', remark='进行中，重点核对含电 SKU' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_de;
UPDATE stocktake_task SET task_name='洛杉矶仓动销 SKU 差异复核', remark='等待仓储主管审核调整' WHERE tenant_id=@tenant_id AND warehouse_id=@wh_la;
UPDATE stocktake_item SET sku_name='折叠式户外储能灯 欧规黑色', diff_reason='账实一致' WHERE tenant_id=@tenant_id AND sku_id=@sku_camp_light;
UPDATE stocktake_item SET sku_name='AX9 宠物烘干箱 白色 110V', diff_reason='销售出库回写延迟' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;

UPDATE order_item SET sku_name='AX9 宠物烘干箱 白色 110V' WHERE tenant_id=@tenant_id AND sku_id=@sku_pet_white;
UPDATE order_item SET sku_name='折叠式户外储能灯 欧规黑色' WHERE tenant_id=@tenant_id AND sku_id=@sku_camp_light;

UPDATE logistics_waybill SET receiver_name='Mia Johnson', exception_desc=NULL WHERE tenant_id=@tenant_id AND order_no='SO-202605-1001';
UPDATE logistics_waybill SET receiver_name='Lukas Weber', exception_desc='超过 7 天无轨迹更新' WHERE tenant_id=@tenant_id AND order_no='SO-202605-1002';
UPDATE logistics_track SET status_desc='包裹已被物流商揽收' WHERE tenant_id=@tenant_id AND track_code='PICKUP';
UPDATE logistics_track SET status_desc='离开分拨中心' WHERE tenant_id=@tenant_id AND track_code='TRANSIT' AND tracking_no='YTN202605280001';
UPDATE logistics_track SET status_desc='包裹中心已处理' WHERE tenant_id=@tenant_id AND track_code='TRANSIT' AND tracking_no='DHL202605210002';
UPDATE logistics_track SET status_desc='超过 7 天无轨迹更新', exception_desc='轨迹停滞' WHERE tenant_id=@tenant_id AND track_code='NO_UPDATE';

UPDATE finance_payable SET supplier_name='深圳华景电子有限公司', remark='全量到货确认后付款' WHERE tenant_id=@tenant_id AND supplier_id=@sup_elec;
UPDATE finance_payable SET supplier_name='宁波远航包装厂', remark='已完成部分付款' WHERE tenant_id=@tenant_id AND supplier_id=@sup_pack;
UPDATE finance_payable SET supplier_name='厦门森海户外用品有限公司', remark='待供应商开票' WHERE tenant_id=@tenant_id AND supplier_id=@sup_outdoor;

UPDATE sys_message SET title='库存预警', content='SKU-US-AX902 可售库存低于安全库存，请尽快安排补货。' WHERE tenant_id=@tenant_id AND biz_id='SKU-US-AX902';
UPDATE sys_message SET title='采购单部分入库', content='PO-202605-2388 首批货物已在洛杉矶海外仓上架。' WHERE tenant_id=@tenant_id AND biz_id='PO-202605-2388';
UPDATE sys_message SET title='物流异常', content='WB-202605-9002 超过 7 天无轨迹更新，请跟进物流商。' WHERE tenant_id=@tenant_id AND biz_id='WB-202605-9002';

SELECT 'zh_demo_names_done' AS result,
       (SELECT COUNT(*) FROM purchase_order WHERE tenant_id=@tenant_id) AS purchase_orders,
       (SELECT COUNT(*) FROM inventory WHERE tenant_id=@tenant_id) AS inventory_rows,
       (SELECT COUNT(*) FROM sys_message WHERE tenant_id=@tenant_id AND read_status=0) AS unread_messages;
