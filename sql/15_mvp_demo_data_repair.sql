USE `supplychain_dev`;
SET NAMES utf8mb4;
SET @tenant_id := 2059984036520636418;
SET @admin_id := 2059984037695041538;
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
SET @loc_la_a := 910000000000000511;
SET @loc_la_b := 910000000000000512;
SET @loc_nj_a := 910000000000000513;
SET @loc_de_a := 910000000000000514;
SET @req1 := 910000000000000601;
SET @inq1 := 910000000000000621;
SET @po1 := 910000000000000701;
SET @po2 := 910000000000000702;
SET @po3 := 910000000000000703;
SET @in1 := 910000000000000801;
SET @in2 := 910000000000000802;
SET @st1 := 910000000000000901;
SET @st2 := 910000000000000902;
SET @order1 := 910000000000001001;
SET @order2 := 910000000000001002;
SET @carrier1 := 910000000000001101;
SET @carrier2 := 910000000000001102;
SET @channel1 := 910000000000001111;
SET @channel2 := 910000000000001112;
SET @waybill1 := 910000000000001201;
SET @waybill2 := 910000000000001202;
SET @bill1 := 910000000000001301;

UPDATE supplier SET supplier_name='Shenzhen Huajing Electronics Co Ltd',province='Guangdong',city='Shenzhen',address='Baoan Smart Manufacturing Park B8',contact_name='Chen Min',contact_phone='13800138001',contact_email='chenmin@huajing.example.com',bank_name='CMB Shenzhen Baoan Branch',bank_account_name='Shenzhen Huajing Electronics Co Ltd',audit_remark='Approved with complete qualification files',remark='Core electronics supplier',tags=JSON_ARRAY('core','invoice','sample'),grade='S',score=94.50,status=2 WHERE id=@sup_elec;
UPDATE supplier SET supplier_name='Ningbo Yuanhang Packaging Factory',province='Zhejiang',city='Ningbo',address='Beilun Spring Industrial Park 12',contact_name='Zhou Hang',contact_phone='13800138002',contact_email='zhouhang@yuanhang.example.com',bank_name='Bank of Ningbo Beilun Branch',bank_account_name='Ningbo Yuanhang Packaging Factory',audit_remark='Stable price, reserve capacity before peak season',remark='Long-term packaging supplier',tags=JSON_ARRAY('packaging','15-day terms'),grade='A',score=88.20,status=2 WHERE id=@sup_pack;
UPDATE supplier SET supplier_name='Xiamen Senhai Outdoor Products Co Ltd',province='Fujian',city='Xiamen',address='Jimei Business Operation Center',contact_name='Lin Qi',contact_phone='13800138003',contact_email='linqi@senhai.example.com',bank_name='CIB Xiamen Branch',bank_account_name='Xiamen Senhai Outdoor Products Co Ltd',audit_remark='EU orders need delivery follow-up',remark='Outdoor products supplier',tags=JSON_ARRAY('EU','follow-up'),grade='B',score=78.60,status=2 WHERE id=@sup_outdoor;

UPDATE supplier_contact SET contact_name='Chen Min',position='Sales Director',department='Sales',remark='Primary contact' WHERE id=910000000000000111;
UPDATE supplier_contact SET contact_name='Huang Jie',position='Finance Manager',department='Finance',remark='Invoice and reconciliation' WHERE id=910000000000000112;
UPDATE supplier_contact SET contact_name='Zhou Hang',position='Factory Manager',department='Business',remark='Packaging order contact' WHERE id=910000000000000113;
UPDATE supplier_contact SET contact_name='Lin Qi',position='Trade Manager',department='Export',remark='EU order contact' WHERE id=910000000000000114;
UPDATE supplier_cert SET cert_name='Business License',remark='Approved' WHERE id=910000000000000121;
UPDATE supplier_cert SET cert_name='CE Certificate',remark='Required for EU listing' WHERE id=910000000000000122;
UPDATE supplier_cert SET cert_name='Carton BCT Test Report',remark='Review within 90 days' WHERE id=910000000000000123;
UPDATE supplier_cert SET cert_name='RoHS Certificate',remark='Expiring soon warning' WHERE id=910000000000000124;
UPDATE supplier_score_log SET calc_remark='Stable top supplier' WHERE id=910000000000000131;
UPDATE supplier_score_log SET calc_remark='Good for packaging replenishment' WHERE id=910000000000000132;
UPDATE supplier_score_log SET calc_remark='Delivery and response need follow-up' WHERE id=910000000000000133;

UPDATE product_category SET category_name='Pet Appliance',category_name_en='Pet Appliance',path='/pet' WHERE id=@cat_pet;
UPDATE product_category SET category_name='Packaging Consumables',category_name_en='Packaging Consumables',path='/packaging' WHERE id=@cat_pack;
UPDATE product_category SET category_name='Outdoor Gear',category_name_en='Outdoor Gear',path='/outdoor' WHERE id=@cat_outdoor;
UPDATE product_spu SET spu_name='AX9 Multi-size Pet Dryer Box',category_path='/pet',brand='FlexPet',spu_desc='Multi-size pet dryer box for cross-border platforms',package_desc='Carton with foam protection',remark='Hot SKU for US TikTok shop' WHERE id=@spu_pet_dryer;
UPDATE product_spu SET spu_name='Heavy Duty Express Carton 40x30x28',category_path='/packaging',brand='Yuanhang',spu_desc='Packaging consumable for outbound orders',package_desc='Flat packed carton bundle',remark='Warehouse consumable' WHERE id=@spu_pack_box;
UPDATE product_spu SET spu_name='Foldable Outdoor Power Light',category_path='/outdoor',brand='Senhai',spu_desc='Rechargeable outdoor camping light',package_desc='Retail color box',remark='EU seasonal SKU' WHERE id=@spu_camp_light;
UPDATE product_sku SET sku_name='AX9 Pet Dryer Box White 110V',spec_values=JSON_OBJECT('color','white','voltage','110V'),spec_values_en=JSON_OBJECT('color','White','voltage','110V'),remark='US hot SKU' WHERE id=@sku_pet_white;
UPDATE product_sku SET sku_name='AX9 Pet Dryer Box Gray 110V',spec_values=JSON_OBJECT('color','gray','voltage','110V'),spec_values_en=JSON_OBJECT('color','Gray','voltage','110V'),remark='US alternate color' WHERE id=@sku_pet_gray;
UPDATE product_sku SET sku_name='Heavy Duty Carton 40x30x28cm',spec_values=JSON_OBJECT('size','40*30*28cm','layer','5-ply'),spec_values_en=JSON_OBJECT('size','40*30*28cm','layer','5-ply'),remark='Packaging consumable' WHERE id=@sku_pack_box;
UPDATE product_sku SET sku_name='Foldable Outdoor Power Light EU Black',spec_values=JSON_OBJECT('color','black','plug','EU'),spec_values_en=JSON_OBJECT('color','Black','plug','EU'),remark='EU battery SKU' WHERE id=@sku_camp_light;

UPDATE warehouse SET warehouse_name='Los Angeles Overseas Warehouse',country_name='United States',province='CA',city='Los Angeles',address='1688 Supply Chain Ave',contact_name='Mike Lee',remark='Main US outbound warehouse' WHERE id=@wh_la;
UPDATE warehouse SET warehouse_name='Ningbo Domestic Warehouse',country_name='China',province='Zhejiang',city='Ningbo',address='Beilun Bonded Area 6',contact_name='Wang Xin',remark='Domestic consolidation warehouse' WHERE id=@wh_nj;
UPDATE warehouse SET warehouse_name='Hamburg EU Warehouse',country_name='Germany',province='Hamburg',city='Hamburg',address='HafenCity 18',contact_name='Anna Becker',remark='EU regional warehouse' WHERE id=@wh_de;
UPDATE warehouse_location SET remark='Fast moving zone' WHERE id=@loc_la_a;
UPDATE warehouse_location SET remark='Frozen stock area' WHERE id=@loc_la_b;
UPDATE warehouse_location SET remark='Packaging area' WHERE id=@loc_nj_a;
UPDATE warehouse_location SET remark='EU lighting zone' WHERE id=@loc_de_a;

UPDATE inventory SET sku_name='AX9 Pet Dryer Box White 110V',quantity=351,frozen_qty=18,in_transit_qty=600,defective_qty=4,reserved_qty=42,safety_stock=120,total_cost=298350.0000 WHERE id=910000000000000521;
UPDATE inventory SET sku_name='AX9 Pet Dryer Box Gray 110V',quantity=48,frozen_qty=6,in_transit_qty=240,defective_qty=0,reserved_qty=16,safety_stock=80,total_cost=43800.0000 WHERE id=910000000000000522;
UPDATE inventory SET sku_name='Heavy Duty Carton 40x30x28cm',quantity=2200,frozen_qty=0,in_transit_qty=5000,defective_qty=22,reserved_qty=180,safety_stock=1500,total_cost=28160.0000 WHERE id=910000000000000523;
UPDATE inventory SET sku_name='Foldable Outdoor Power Light EU Black',quantity=0,frozen_qty=0,in_transit_qty=360,defective_qty=0,reserved_qty=0,safety_stock=90,total_cost=0.0000 WHERE id=910000000000000524;
UPDATE inventory_log SET sku_name='AX9 Pet Dryer Box White 110V',operator_name='Admin',remark='Purchase inbound' WHERE id=910000000000000531;
UPDATE inventory_log SET sku_name='AX9 Pet Dryer Box White 110V',operator_name='Admin',remark='Sales outbound' WHERE id=910000000000000532;
UPDATE inventory_log SET sku_name='Heavy Duty Carton 40x30x28cm',operator_name='Admin',remark='Packaging inbound' WHERE id=910000000000000533;
UPDATE inventory_log SET sku_name='Foldable Outdoor Power Light EU Black',operator_name='Admin',remark='EU orders cleared stock' WHERE id=910000000000000534;
UPDATE inventory_warning_event SET available_qty=26,safety_stock=80,status=1 WHERE id=910000000000000541;
UPDATE inventory_warning_event SET available_qty=0,safety_stock=90,status=1 WHERE id=910000000000000542;

UPDATE purchase_requisition SET title='US pet dryer box replenishment',remark='Triggered by low-stock warning',audit_remark='Approved for stock risk' WHERE id=@req1;
UPDATE purchase_requisition_item SET sku_name='AX9 Pet Dryer Box White 110V',remark='Maintain 30 days cover' WHERE id=910000000000000611;
UPDATE purchase_requisition_item SET sku_name='AX9 Pet Dryer Box Gray 110V',remark='Low stock color variant' WHERE id=910000000000000612;
UPDATE purchase_inquiry SET supplier_name='Shenzhen Huajing Electronics Co Ltd',remark='Compare for June replenishment',supplier_remark='Can ship in two batches' WHERE id=@inq1;
UPDATE purchase_inquiry_item SET sku_name='AX9 Pet Dryer Box White 110V',remark='Factory stock available' WHERE id=910000000000000631;
UPDATE purchase_inquiry_item SET sku_name='AX9 Pet Dryer Box Gray 110V',remark='Needs color shell confirmation' WHERE id=910000000000000632;
UPDATE purchase_order SET supplier_name='Shenzhen Huajing Electronics Co Ltd',warehouse_name='Los Angeles Overseas Warehouse',remark='First batch partially inbound' WHERE id=@po1;
UPDATE purchase_order SET supplier_name='Ningbo Yuanhang Packaging Factory',warehouse_name='Ningbo Domestic Warehouse',remark='Packaging received and pending settlement' WHERE id=@po2;
UPDATE purchase_order SET supplier_name='Xiamen Senhai Outdoor Products Co Ltd',warehouse_name='Hamburg EU Warehouse',remark='EU battery SKU in production' WHERE id=@po3;
UPDATE purchase_order_item SET sku_name='AX9 Pet Dryer Box White 110V',spec='white / 110V',unit='pcs',remark='First batch 120 received' WHERE id=910000000000000711;
UPDATE purchase_order_item SET sku_name='AX9 Pet Dryer Box Gray 110V',spec='gray / 110V',unit='pcs',remark='Pending arrival' WHERE id=910000000000000712;
UPDATE purchase_order_item SET sku_name='Heavy Duty Carton 40x30x28cm',spec='5-ply carton',unit='pcs',remark='Partial inbound' WHERE id=910000000000000713;
UPDATE purchase_order_item SET sku_name='Foldable Outdoor Power Light EU Black',spec='black / EU plug',unit='pcs',remark='In production' WHERE id=910000000000000714;
UPDATE purchase_receipt SET receiver_name='Admin',remark='First batch received in LA warehouse' WHERE id=910000000000000751;
UPDATE purchase_receipt SET receiver_name='Admin',remark='Packaging batch received' WHERE id=910000000000000752;
UPDATE purchase_receipt_item SET sku_name='AX9 Pet Dryer Box White 110V',reject_reason='Outer carton damaged' WHERE id=910000000000000761;
UPDATE purchase_receipt_item SET sku_name='Heavy Duty Carton 40x30x28cm',reject_reason='Moisture mark on edge' WHERE id=910000000000000762;
UPDATE inbound_order SET warehouse_name='Los Angeles Overseas Warehouse',remark='PO first batch partially shelved' WHERE id=@in1;
UPDATE inbound_order SET warehouse_name='Ningbo Domestic Warehouse',remark='Packaging batch received' WHERE id=@in2;
UPDATE inbound_order_item SET sku_name='AX9 Pet Dryer Box White 110V',remark='First batch shelved' WHERE id=910000000000000811;
UPDATE inbound_order_item SET sku_name='AX9 Pet Dryer Box Gray 110V',remark='Pending arrival' WHERE id=910000000000000812;
UPDATE inbound_order_item SET sku_name='Heavy Duty Carton 40x30x28cm',remark='First carton batch inbound' WHERE id=910000000000000813;
UPDATE stocktake_task SET task_name='Hamburg monthly cycle count',remark='In progress, focus on battery SKU' WHERE id=@st1;
UPDATE stocktake_task SET task_name='LA slow-moving SKU variance review',remark='Waiting for warehouse supervisor audit' WHERE id=@st2;
INSERT INTO stocktake_item (id,tenant_id,task_id,warehouse_id,location_id,location_code,sku_id,sku_code,sku_name,book_qty,actual_qty,diff_qty,diff_amount,diff_reason,is_adjusted,adjust_time,picker_id,pick_time) VALUES
(910000000000000911,@tenant_id,@st1,@wh_de,@loc_de_a,'E-03-02',@sku_camp_light,'SKU-EU-CAMP01','Foldable Outdoor Power Light EU Black',0,0,0,0.00,'Book and actual quantities match',0,NULL,@admin_id,'2026-05-27 11:20:00'),
(910000000000000912,@tenant_id,@st2,@wh_la,@loc_la_a,'A-02-06',@sku_pet_white,'SKU-US-AX901','AX9 Pet Dryer Box White 110V',351,320,-31,26350.00,'Sales outbound callback delayed',0,NULL,@admin_id,'2026-05-26 16:30:00')
ON DUPLICATE KEY UPDATE sku_name=VALUES(sku_name),actual_qty=VALUES(actual_qty),diff_qty=VALUES(diff_qty),diff_reason=VALUES(diff_reason);

INSERT INTO order_main
(id, tenant_id, order_no, platform, platform_order_no, store_id, total_amount, discount_amount, shipping_fee, payment_amount, currency, exchange_rate, cny_amount, platform_fee, status, cancel_reason, is_abnormal, abnormal_reason, warehouse_id, logistics_channel, waybill_no, ship_time, delivery_deadline, signed_time, platform_order_time, platform_pay_time, create_by, update_by)
VALUES
(@order1,@tenant_id,'SO-202605-1001','TikTok','TT-US-202605280001',1001,199.99,10.00,0.00,189.99,'USD',7.200000,1367.93,18.20,6,NULL,0,NULL,@wh_la,'YunExpress US Standard','WB-202605-9001','2026-05-28 13:20:00','2026-05-29',NULL,'2026-05-28 09:15:00','2026-05-28 09:16:00',@admin_id,@admin_id),
(@order2,@tenant_id,'SO-202605-1002','Amazon','AMZ-DE-303-778899',2001,49.99,0.00,4.99,54.98,'EUR',7.800000,428.84,7.60,7,NULL,1,'Tracking has not updated for more than 7 days',@wh_de,'DHL Paket DE','WB-202605-9002','2026-05-21 11:30:00','2026-05-22',NULL,'2026-05-20 20:05:00','2026-05-20 20:07:00',@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status),is_abnormal=VALUES(is_abnormal),abnormal_reason=VALUES(abnormal_reason),update_by=@admin_id;
INSERT INTO order_item (id,tenant_id,order_id,order_no,sku_id,sku_code,sku_name,platform_sku_id,quantity,unit_price,discount,amount,currency,refunded_qty) VALUES
(910000000000001011,@tenant_id,@order1,'SO-202605-1001',@sku_pet_white,'SKU-US-AX901','AX9 Pet Dryer Box White 110V','TT-AX901-WH',1,199.9900,10.00,189.99,'USD',0),
(910000000000001012,@tenant_id,@order2,'SO-202605-1002',@sku_camp_light,'SKU-EU-CAMP01','Foldable Outdoor Power Light EU Black','AMZ-CAMP01-EU',1,49.9900,0.00,49.99,'EUR',0)
ON DUPLICATE KEY UPDATE sku_name=VALUES(sku_name),quantity=VALUES(quantity),amount=VALUES(amount);
INSERT INTO order_address (id,tenant_id,order_id,receiver_name,phone,email,country_code,country_name,state,city,address_line1,address_line2,zip_code,full_address,is_verified) VALUES
(910000000000001021,@tenant_id,@order1,'Mia Johnson','+1 213 555 0101','mia@example.com','US','United States','CA','Los Angeles','900 Flower Street',NULL,'90015','900 Flower Street, Los Angeles, CA 90015, US',1),
(910000000000001022,@tenant_id,@order2,'Lukas Weber','+49 30 000 0102','lukas@example.de','DE','Germany','Berlin','Berlin','Alexanderplatz 1',NULL,'10178','Alexanderplatz 1, 10178 Berlin, DE',1)
ON DUPLICATE KEY UPDATE receiver_name=VALUES(receiver_name),full_address=VALUES(full_address);

INSERT INTO logistics_carrier (id,tenant_id,carrier_code,carrier_name,carrier_name_en,carrier_type,logo_url,api_base_url,api_key,api_secret,api_account,api_version,track_api_url,support_label,support_track,status,remark,create_by,update_by) VALUES
(@carrier1,@tenant_id,'YUNEXPRESS','YunExpress','YunExpress',1,'https://www.yunexpress.com/favicon.ico','https://api.yunexpress.example.com','demo-key','demo-secret','flexchain-demo','v1','https://track.yunexpress.example.com',1,1,1,'US standard shipping',@admin_id,@admin_id),
(@carrier2,@tenant_id,'DHLDE','DHL Germany','DHL Germany',1,'https://www.dhl.de/favicon.ico','https://api.dhl.example.com','demo-key','demo-secret','flexchain-demo','v2','https://track.dhl.example.com',1,1,1,'EU parcel channel',@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE carrier_name=VALUES(carrier_name),remark=VALUES(remark),update_by=@admin_id;
INSERT INTO logistics_channel (id,tenant_id,carrier_id,channel_code,channel_name,channel_type,country_codes,min_weight_g,max_weight_g,max_length_mm,max_girth_mm,allow_battery,allow_liquid,allow_powder,allow_food,min_days,max_days,volume_factor,declared_value_limit,status,sort_order,remark,create_by,update_by) VALUES
(@channel1,@tenant_id,@carrier1,'YUN-US-STD','YunExpress US Standard',1,JSON_ARRAY('US'),0,30000,1200,3000,0,0,0,1,5.0,9.0,5000,800.00,1,10,'Balanced cost and delivery time for US',@admin_id,@admin_id),
(@channel2,@tenant_id,@carrier2,'DHL-DE-PARCEL','DHL Paket DE',1,JSON_ARRAY('DE','FR','NL'),0,20000,1000,2500,1,0,0,1,3.0,6.0,5000,600.00,1,20,'EU channel accepts battery SKU',@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE channel_name=VALUES(channel_name),country_codes=VALUES(country_codes),remark=VALUES(remark),update_by=@admin_id;
INSERT INTO logistics_waybill (id,tenant_id,waybill_no,tracking_no,carrier_id,channel_id,order_id,order_no,warehouse_id,receiver_name,receiver_phone,country_code,state,city,address_line1,address_line2,zip_code,actual_weight_g,volume_weight_g,charge_weight_g,length_mm,width_mm,height_mm,package_count,declared_value,declared_currency,declared_name_en,hs_code,is_gift,estimated_fee,actual_fee,fee_currency,status,label_url,label_format,create_waybill_time,pickup_time,signed_time,exception_desc,create_by,update_by) VALUES
(@waybill1,@tenant_id,'WB-202605-9001','YTN202605280001',@carrier1,@channel1,@order1,'SO-202605-1001',@wh_la,'Mia Johnson','+1 213 555 0101','US','CA','Los Angeles','900 Flower Street',NULL,'90015',9600,38688,38688,620,520,540,1,189.99,'USD','Pet Dryer Box','85167990',0,148.00,151.20,'CNY',2,'oss://demo/label/WB-202605-9001.pdf','PDF','2026-05-28 12:30:00','2026-05-28 18:30:00',NULL,NULL,@admin_id,@admin_id),
(@waybill2,@tenant_id,'WB-202605-9002','DHL202605210002',@carrier2,@channel2,@order2,'SO-202605-1002',@wh_de,'Lukas Weber','+49 30 000 0102','DE','Berlin','Berlin','Alexanderplatz 1',NULL,'10178',820,439,820,210,110,95,1,49.99,'EUR','Outdoor Camping Light','94054290',0,42.00,45.60,'CNY',6,'oss://demo/label/WB-202605-9002.pdf','PDF','2026-05-21 09:00:00','2026-05-21 11:30:00',NULL,'No tracking update for 7 days',@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status),actual_fee=VALUES(actual_fee),exception_desc=VALUES(exception_desc),update_by=@admin_id;
INSERT INTO logistics_track (id,tenant_id,waybill_id,tracking_no,track_code,track_stage,raw_status,status_desc,location,location_country,track_time,fetch_time,is_exception,exception_type,exception_desc) VALUES
(910000000000001211,@tenant_id,@waybill1,'YTN202605280001','PICKUP',2,'Picked up','Package picked up by carrier','Los Angeles, CA','US','2026-05-28 18:30:00','2026-05-28 19:00:00',0,NULL,NULL),
(910000000000001212,@tenant_id,@waybill1,'YTN202605280001','TRANSIT',3,'In transit','Departed sorting center','Los Angeles, CA','US','2026-05-28 23:20:00','2026-05-29 00:00:00',0,NULL,NULL),
(910000000000001213,@tenant_id,@waybill2,'DHL202605210002','TRANSIT',3,'In transit','Shipment processed at parcel center','Hamburg','DE','2026-05-21 22:10:00','2026-05-22 00:00:00',0,NULL,NULL),
(910000000000001214,@tenant_id,@waybill2,'DHL202605210002','NO_UPDATE',6,'Exception','No tracking update for more than 7 days','Hamburg','DE','2026-05-28 08:00:00','2026-05-28 08:10:00',1,2,'Tracking stagnation')
ON DUPLICATE KEY UPDATE status_desc=VALUES(status_desc),is_exception=VALUES(is_exception),exception_desc=VALUES(exception_desc);
INSERT INTO logistics_fee_record (id,tenant_id,waybill_id,waybill_no,base_fee,fuel_surcharge,peak_surcharge,remote_fee,oversize_fee,insurance_fee,other_fee,estimated_total,actual_total,currency,billing_weight_g,rate_id) VALUES
(910000000000001221,@tenant_id,@waybill1,'WB-202605-9001',120.0000,18.0000,5.0000,0,0,5.0000,3.2000,148.00,151.20,'CNY',38688,NULL),
(910000000000001222,@tenant_id,@waybill2,'WB-202605-9002',35.0000,4.5000,0,0,0,2.5000,3.6000,42.00,45.60,'CNY',820,NULL)
ON DUPLICATE KEY UPDATE actual_total=VALUES(actual_total),billing_weight_g=VALUES(billing_weight_g);

INSERT INTO finance_payable (id,tenant_id,payable_no,source_type,source_biz_no,po_id,po_no,supplier_id,supplier_name,invoice_no,invoice_date,payable_amount,paid_amount,currency,payment_days,due_date,status,overdue_days,remark,create_by,update_by) VALUES
(910000000000001301,@tenant_id,'AP-202605-1001','PURCHASE_ORDER','PO-202605-1001',@po1,'PO-202605-1001',@sup_elec,'Shenzhen Huajing Electronics Co Ltd','INV-HJ-2026051001','2026-05-28',729000.00,0.00,'CNY',30,'2026-06-27',0,0,'Pay after full delivery confirmation',@admin_id,@admin_id),
(910000000000001302,@tenant_id,'AP-202605-1002','PURCHASE_ORDER','PO-202605-1002',@po2,'PO-202605-1002',@sup_pack,'Ningbo Yuanhang Packaging Factory','INV-YH-2026051002','2026-05-27',64000.00,20000.00,'CNY',15,'2026-06-11',1,0,'Partial payment completed',@admin_id,@admin_id),
(910000000000001303,@tenant_id,'AP-202605-1003','PURCHASE_ORDER','PO-202605-1003',@po3,'PO-202605-1003',@sup_outdoor,'Xiamen Senhai Outdoor Products Co Ltd',NULL,NULL,45360.00,0.00,'CNY',45,'2026-07-08',0,0,'Invoice pending',@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE supplier_name=VALUES(supplier_name),payable_amount=VALUES(payable_amount),paid_amount=VALUES(paid_amount),status=VALUES(status),remark=VALUES(remark),update_by=@admin_id;
INSERT INTO finance_platform_bill (id,tenant_id,bill_no,platform,store_id,store_name,platform_bill_id,settlement_start,settlement_end,currency,total_sales,total_refund,referral_fee,fba_fee,storage_fee,advertising_fee,other_fee,net_amount,cny_amount,exchange_rate,status,source_file_url,import_time,import_user_id,create_by,update_by) VALUES
(@bill1,@tenant_id,'BILL-AMZ-202605-01','Amazon','AMZ-DE-01','FlexChain DE Store','AMZ-SETTLE-202605A','2026-05-01','2026-05-15','EUR',38620.00,1280.00,4248.20,1880.00,236.00,3200.00,180.00,27895.80,217587.24,7.800000,2,'oss://demo/bill/amazon-20260501.csv','2026-05-28 09:20:00',@admin_id,@admin_id,@admin_id)
ON DUPLICATE KEY UPDATE total_sales=VALUES(total_sales),net_amount=VALUES(net_amount),status=VALUES(status),update_by=@admin_id;
INSERT INTO finance_bill_item (id,tenant_id,bill_id,item_type,order_no,sku_id,platform_sku,amount,currency,description,transaction_date,is_matched,match_order_id) VALUES
(910000000000001311,@tenant_id,@bill1,'SALE','AMZ-DE-303-778899',@sku_camp_light,'AMZ-CAMP01-EU',49.9900,'EUR','Order sale amount','2026-05-20',1,@order2),
(910000000000001312,@tenant_id,@bill1,'REFERRAL_FEE','AMZ-DE-303-778899',@sku_camp_light,'AMZ-CAMP01-EU',-7.6000,'EUR','Amazon referral fee','2026-05-20',1,@order2),
(910000000000001313,@tenant_id,@bill1,'UNMATCHED_FEE',NULL,NULL,NULL,-12.3000,'EUR','Unmatched service adjustment','2026-05-22',0,NULL)
ON DUPLICATE KEY UPDATE amount=VALUES(amount),is_matched=VALUES(is_matched),description=VALUES(description);
INSERT INTO finance_profit_snapshot (id,tenant_id,snapshot_type,snapshot_date,sku_id,sku_code,platform,store_id,country_code,currency,exchange_rate,order_count,sales_qty,gross_revenue,gross_revenue_cny,purchase_cost,logistics_fee,platform_fee,fba_storage_fee,advertising_fee,refund_loss,vat_fee,other_cost,total_cost,gross_profit,net_profit,gross_margin,net_margin) VALUES
(910000000000001321,@tenant_id,1,'2026-05-28',@sku_pet_white,'SKU-US-AX901','TikTok','TT-US-01','US','USD',7.200000,42,42,8399.58,60476.98,35700.00,6349.20,764.40,0.00,1800.00,0.00,0.00,320.00,44933.60,24776.98,15543.38,0.4097,0.2570),
(910000000000001322,@tenant_id,1,'2026-05-28',@sku_camp_light,'SKU-EU-CAMP01','Amazon','AMZ-DE-01','DE','EUR',7.800000,18,18,899.82,7018.60,2268.00,820.80,136.80,42.00,420.00,0.00,132.00,60.00,3879.60,4750.60,3139.00,0.6770,0.4473)
ON DUPLICATE KEY UPDATE order_count=VALUES(order_count),sales_qty=VALUES(sales_qty),net_profit=VALUES(net_profit),net_margin=VALUES(net_margin);
INSERT INTO finance_cash_flow (id,tenant_id,flow_date,flow_type,source_type,source_id,source_no,amount_cny,amount_origin,currency,exchange_rate,remark,create_by) VALUES
(910000000000001331,@tenant_id,'2026-05-28',1,'PLATFORM_SETTLEMENT',@bill1,'BILL-AMZ-202605-01',217587.24,27895.80,'EUR',7.800000,'Amazon settlement expected',@admin_id),
(910000000000001332,@tenant_id,'2026-06-11',2,'PAYABLE',910000000000001302,'AP-202605-1002',44000.00,44000.00,'CNY',1.000000,'Packaging payable remaining',@admin_id),
(910000000000001333,@tenant_id,'2026-06-27',2,'PAYABLE',910000000000001301,'AP-202605-1001',729000.00,729000.00,'CNY',1.000000,'Pet dryer payable due',@admin_id)
ON DUPLICATE KEY UPDATE amount_cny=VALUES(amount_cny),remark=VALUES(remark);
INSERT INTO bi_kpi_threshold (id,tenant_id,kpi_code,kpi_name,warning_value,danger_value,compare_type,notify_roles,is_enabled) VALUES
(910000000000001341,@tenant_id,'stockout_rate','Stockout Rate',0.0300,0.0800,2,JSON_ARRAY('WAREHOUSE_MANAGER','PURCHASE_MANAGER'),1),
(910000000000001342,@tenant_id,'purchase_on_time_rate','Purchase On-time Arrival Rate',0.9000,0.8000,1,JSON_ARRAY('PURCHASE_MANAGER'),1),
(910000000000001343,@tenant_id,'logistics_exception_rate','Logistics Exception Rate',0.0500,0.1000,2,JSON_ARRAY('OPS_MANAGER'),1)
ON DUPLICATE KEY UPDATE warning_value=VALUES(warning_value),danger_value=VALUES(danger_value),is_enabled=VALUES(is_enabled);
INSERT INTO sys_message (id,tenant_id,receiver_id,receiver_type,receiver_key,title,content,biz_type,biz_id,priority,read_status,read_time) VALUES
(910000000000001351,@tenant_id,@admin_id,'USER',NULL,'Low stock warning','SKU-US-AX902 available stock is below safety stock. Please review replenishment.','WMS_INVENTORY','SKU-US-AX902','HIGH',0,NULL),
(910000000000001352,@tenant_id,@admin_id,'USER',NULL,'Purchase order partially inbound','PO-202605-1001 first batch has been shelved in LA warehouse.','PMS_ORDER','PO-202605-1001','NORMAL',0,NULL),
(910000000000001353,@tenant_id,@admin_id,'USER',NULL,'Logistics exception','WB-202605-9002 has no tracking update for more than 7 days.','TMS_EXCEPTION','WB-202605-9002','HIGH',0,NULL)
ON DUPLICATE KEY UPDATE title=VALUES(title),content=VALUES(content),priority=VALUES(priority),read_status=VALUES(read_status);

SELECT 'demo_seed_repair_done' AS result,
 (SELECT COUNT(*) FROM purchase_order WHERE tenant_id=@tenant_id) AS purchase_orders,
 (SELECT COUNT(*) FROM inventory WHERE tenant_id=@tenant_id) AS inventory_rows,
 (SELECT COUNT(*) FROM inbound_order WHERE tenant_id=@tenant_id) AS inbound_orders,
 (SELECT COUNT(*) FROM stocktake_task WHERE tenant_id=@tenant_id) AS stocktake_tasks,
 (SELECT COUNT(*) FROM order_main WHERE tenant_id=@tenant_id) AS sales_orders,
 (SELECT COUNT(*) FROM finance_payable WHERE tenant_id=@tenant_id) AS payables;
