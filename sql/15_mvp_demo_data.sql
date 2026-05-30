USE `supplychain_dev`;

-- FlexChain seller-workbench MVP demo data.
-- This seed is intentionally tenant-scoped and repeatable.
SET @tenant_id := 2059984036520636418;
SET @admin_id := 2059984037695041538;

-- Core IDs
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

-- Suppliers
INSERT INTO supplier
(id, tenant_id, supplier_code, supplier_name, supplier_type, category_ids, province, city, address, website, company_size, founded_year,
 contact_name, contact_phone, contact_email, contact_wechat, contact_whatsapp, bank_name, bank_account, bank_account_name, tax_no,
 invoice_type, moq, lead_time_days, monthly_capacity, currency, payment_days, grade, score, last_score_month, status,
 audit_user_id, audit_time, audit_remark, portal_enabled, remark, tags, create_by, update_by)
VALUES
(@sup_elec, @tenant_id, 'SUP-202605-0001', '娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃', 1, JSON_ARRAY(@cat_pet), '骞夸笢鐪?, '娣卞湷甯?, '瀹濆畨鍖鸿埅鍩庤閬撴櫤閫犲洯 8 鏍?,
 'https://huajing.example.com', 3, 2014, '闄堟晱', '13800138001', 'chenmin@huajing.example.com', 'huajing-cm', '+86 13800138001',
 '鎷涘晢閾惰娣卞湷瀹濆畨鏀', '**** **** **** 8301', '娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃', '91440300MAHJ0001X', 1, 200, 18, 30000, 'CNY', 30,
 'S', 94.50, '202605', 2, @admin_id, '2026-05-20 10:00:00', '璧勮川瀹屾暣锛屼氦浠樼ǔ瀹?, 1, '鏍稿績鐢靛瓙绫讳緵搴斿晢',
 JSON_ARRAY('鏍稿績渚涘簲鍟?,'鏀寔鎵撴牱','鍙紑涓撶エ'), @admin_id, @admin_id),
(@sup_pack, @tenant_id, 'SUP-202605-0002', '瀹佹尝杩滆埅鍖呰鍘?, 1, JSON_ARRAY(@cat_pack), '娴欐睙鐪?, '瀹佹尝甯?, '鍖椾粦鍖烘槬鏅撳伐涓氬洯 12 鍙?,
 'https://yuanhang-pack.example.com', 2, 2018, '鍛ㄨ埅', '13800138002', 'zhouhang@yuanhang.example.com', 'yh-pack', '+86 13800138002',
 '瀹佹尝閾惰鍖椾粦鏀', '**** **** **** 2088', '瀹佹尝杩滆埅鍖呰鍘?, '91330206MAYH0002X', 1, 1000, 10, 200000, 'CNY', 15,
 'A', 88.20, '202605', 2, @admin_id, '2026-05-18 15:20:00', '浠锋牸绋冲畾锛屾椇瀛ｉ渶鎻愬墠閿佷骇鑳?, 1, '鍖呰鑰楁潗闀挎湡鍚堜綔',
 JSON_ARRAY('鍖呰鑰楁潗','璐︽湡15澶?), @admin_id, @admin_id),
(@sup_outdoor, @tenant_id, 'SUP-202605-0003', '鍘﹂棬妫捣鎴峰鐢ㄥ搧鏈夐檺鍏徃', 1, JSON_ARRAY(@cat_outdoor), '绂忓缓鐪?, '鍘﹂棬甯?, '闆嗙編鍖烘潖鏋楁咕鍟嗗姟钀ヨ繍涓績',
 'https://senhai-outdoor.example.com', 2, 2016, '鏋楀惎', '13800138003', 'linqi@senhai.example.com', 'senhai-lq', '+86 13800138003',
 '鍏翠笟閾惰鍘﹂棬鍒嗚', '**** **** **** 7712', '鍘﹂棬妫捣鎴峰鐢ㄥ搧鏈夐檺鍏徃', '91350200MASH0003X', 1, 300, 22, 50000, 'USD', 45,
 'B', 78.60, '202605', 2, @admin_id, '2026-05-16 09:30:00', '娆ф床璁㈠崟浜ゆ湡鐣ラ暱锛岄渶璺熷偓', 1, '鎴峰鐢ㄥ搧渚涘簲鍟?,
 JSON_ARRAY('娆ц璁よ瘉','闇€璺熷偓'), @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE supplier_name=VALUES(supplier_name), score=VALUES(score), grade=VALUES(grade), status=VALUES(status), update_by=@admin_id;

INSERT INTO supplier_contact
(id, tenant_id, supplier_id, contact_name, position, phone, email, wechat, whatsapp, department, is_primary, contact_type, remark, create_by)
VALUES
(910000000000000111, @tenant_id, @sup_elec, '闄堟晱', '閿€鍞€荤洃', '13800138001', 'chenmin@huajing.example.com', 'huajing-cm', '+86 13800138001', '閿€鍞儴', 1, 1, '涓昏仈绯讳汉', @admin_id),
(910000000000000112, @tenant_id, @sup_elec, '榛勬磥', '璐㈠姟缁忕悊', '13800138011', 'finance@huajing.example.com', 'huajing-fin', '+86 13800138011', '璐㈠姟閮?, 0, 2, '寮€绁ㄤ笌瀵硅处', @admin_id),
(910000000000000113, @tenant_id, @sup_pack, '鍛ㄨ埅', '鍘傞暱', '13800138002', 'zhouhang@yuanhang.example.com', 'yh-pack', '+86 13800138002', '涓氬姟閮?, 1, 1, '鍖呰璁㈠崟鑱旂郴浜?, @admin_id),
(910000000000000114, @tenant_id, @sup_outdoor, '鏋楀惎', '澶栬锤缁忕悊', '13800138003', 'linqi@senhai.example.com', 'senhai-lq', '+86 13800138003', '澶栬锤閮?, 1, 1, '娆ф床璁㈠崟鑱旂郴浜?, @admin_id)
ON DUPLICATE KEY UPDATE contact_name=VALUES(contact_name), phone=VALUES(phone), email=VALUES(email);

INSERT INTO supplier_cert
(id, tenant_id, supplier_id, cert_type, cert_name, file_name, file_url, file_size, file_type, issue_date, expire_date, is_expired, cert_no, remark, create_by)
VALUES
(910000000000000121, @tenant_id, @sup_elec, 1, '钀ヤ笟鎵х収', 'huajing-business-license.pdf', 'oss://demo/supplier/huajing-license.pdf', 1820342, 'application/pdf', '2024-01-10', '2029-01-09', 0, 'BL-HJ-2024', '宸插鏍?, @admin_id),
(910000000000000122, @tenant_id, @sup_elec, 3, 'CE 璁よ瘉', 'pet-dryer-ce.pdf', 'oss://demo/supplier/pet-dryer-ce.pdf', 920112, 'application/pdf', '2025-03-01', '2027-03-01', 0, 'CE-PD-2025', '娆х洘閿€鍞繀澶?, @admin_id),
(910000000000000123, @tenant_id, @sup_pack, 2, '绾哥鎶楀帇娴嬭瘯鎶ュ憡', 'carton-bct-report.pdf', 'oss://demo/supplier/carton-bct.pdf', 618442, 'application/pdf', '2026-02-16', '2026-08-16', 0, 'BCT-202602', '30 澶╁唴闇€澶嶆牳', @admin_id),
(910000000000000124, @tenant_id, @sup_outdoor, 3, 'RoHS 璁よ瘉', 'camp-light-rohs.pdf', 'oss://demo/supplier/camp-rohs.pdf', 805210, 'application/pdf', '2025-09-01', '2026-06-25', 0, 'ROHS-SH-2025', '鍗冲皢鍒版湡棰勮', @admin_id)
ON DUPLICATE KEY UPDATE cert_name=VALUES(cert_name), expire_date=VALUES(expire_date), is_expired=VALUES(is_expired);

INSERT INTO supplier_score_log
(id, tenant_id, supplier_id, score_month, total_orders, delivered_on_time, quality_passed, quality_total, response_hours_avg, price_comparison,
 delivery_score, quality_score, response_score, price_score, total_score, grade, grade_changed, prev_grade, calc_remark, calc_time)
VALUES
(910000000000000131, @tenant_id, @sup_elec, '202605', 12, 12, 18, 18, 1.80, 0.9700, 96, 98, 93, 91, 94.50, 'S', 0, 'S', '鏍稿績鐢靛瓙渚涘簲鍟嗭紝鍑嗘椂鐜囦笌璐ㄩ噺绋冲畾', '2026-05-28 08:00:00'),
(910000000000000132, @tenant_id, @sup_pack, '202605', 9, 8, 11, 12, 3.50, 0.9300, 88, 91, 84, 90, 88.20, 'A', 0, 'A', '鍖呰璁㈠崟浠锋牸浼樺娍鏄庢樉', '2026-05-28 08:00:00'),
(910000000000000133, @tenant_id, @sup_outdoor, '202605', 5, 3, 7, 8, 5.70, 1.0600, 72, 82, 76, 85, 78.60, 'B', 1, 'A', '娆ф床璁㈠崟浜ゆ湡鍋忛暱', '2026-05-28 08:00:00')
ON DUPLICATE KEY UPDATE total_score=VALUES(total_score), grade=VALUES(grade), calc_remark=VALUES(calc_remark);

-- Products
INSERT INTO product_category
(id, tenant_id, parent_id, category_name, category_name_en, level, path, sort_order, status, create_by, update_by)
VALUES
(@cat_pet, @tenant_id, 0, '瀹犵墿鐢靛櫒', 'Pet Appliances', 1, CONCAT('/', @cat_pet, '/'), 10, 1, @admin_id, @admin_id),
(@cat_pack, @tenant_id, 0, '鍖呰鑰楁潗', 'Packaging Supplies', 1, CONCAT('/', @cat_pack, '/'), 20, 1, @admin_id, @admin_id),
(@cat_outdoor, @tenant_id, 0, '鎴峰鐓ф槑', 'Outdoor Lighting', 1, CONCAT('/', @cat_outdoor, '/'), 30, 1, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE category_name=VALUES(category_name), status=VALUES(status);

INSERT INTO product_spu
(id, tenant_id, spu_code, spu_name, category_id, category_path, brand, hs_code, origin_country, material, certifications, status, publish_time,
 spu_desc, package_desc, remark, create_by, update_by)
VALUES
(@spu_pet_dryer, @tenant_id, 'SPU-PET-DRYER-01', '澶氳鏍煎疇鐗╃儤骞茬', @cat_pet, CONCAT('/', @cat_pet, '/'), 'FlexPet', '84193990', 'CN', 'ABS/鐢靛瓙鍏冧欢',
 JSON_ARRAY('CE','FCC'), 2, '2026-05-10 10:00:00', '闈㈠悜娆х編瀹犵墿瀹跺涵鐨勫皬鍨嬫櫤鑳界儤骞茬锛屾敮鎸佹亽娓╀笌浣庡櫔妯″紡銆?, '鍗曞彴褰╃洅+澶栫锛屽惈娉℃搏缂撳啿', 'Amazon/TikTok 涓绘帹娆?, @admin_id, @admin_id),
(@spu_pack_box, @tenant_id, 'SPU-PKG-CARTON-01', '鎶楀帇蹇€掔焊绠?, @cat_pack, CONCAT('/', @cat_pack, '/'), 'YH Pack', '48191000', 'CN', '浜斿眰鐡︽绾?,
 JSON_ARRAY('ISTA'), 2, '2026-04-18 09:00:00', '閫傜敤浜庤法澧冨皬瀹剁數涓庢埛澶栫敤鍝佸彂璐х殑楂樺己搴︾焊绠便€?, '50 涓?鎹嗭紝鎵樼洏鍙戣繍', '浠撳偍鑰楁潗', @admin_id, @admin_id),
(@spu_camp_light, @tenant_id, 'SPU-EU-CAMP-LIGHT-01', '鎶樺彔寮忔埛澶栧偍鑳界伅', @cat_outdoor, CONCAT('/', @cat_outdoor, '/'), 'CampLite', '94054100', 'CN', '閾濆悎閲?閿傜數姹?,
 JSON_ARRAY('CE','RoHS'), 2, '2026-05-01 12:00:00', '娆ф床绔欑儹閿€鎶樺彔闇茶惀鐏紝鏀寔 USB-C 鍏呯數涓庝笁妗ｄ寒搴︺€?, '鍗曞彴鐗涚毊鐩?璇存槑涔?, '鍚攤鐢碉紝鐗╂祦闇€璧板彲甯︾數娓犻亾', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE spu_name=VALUES(spu_name), status=VALUES(status), hs_code=VALUES(hs_code);

INSERT INTO product_sku
(id, tenant_id, spu_id, sku_code, sku_name, barcode, fnsku, spec_values, spec_values_en, net_weight_g, gross_weight_g, length_mm, width_mm, height_mm,
 is_battery, is_liquid, is_powder, cost_price, cost_currency, abc_class, status, remark, create_by, update_by)
VALUES
(@sku_pet_white, @tenant_id, @spu_pet_dryer, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', '6970000009011', 'X004AX901W',
 JSON_OBJECT('棰滆壊','鐧借壊','鐢靛帇','110V'), JSON_OBJECT('Color','White','Voltage','110V'), 5200, 6100, 520, 410, 460, 0, 0, 0, 850.0000, 'CNY', 'A', 1, '缇庡浗绔欎富閿€ SKU', @admin_id, @admin_id),
(@sku_pet_gray, @tenant_id, @spu_pet_dryer, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', '6970000009028', 'X004AX902G',
 JSON_OBJECT('棰滆壊','鐏拌壊','鐢靛帇','110V'), JSON_OBJECT('Color','Gray','Voltage','110V'), 5200, 6120, 520, 410, 460, 0, 0, 0, 912.5000, 'CNY', 'A', 1, '缇庡浗绔欏埄娑︽', @admin_id, @admin_id),
(@sku_pack_box, @tenant_id, @spu_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', '6970000011007', NULL,
 JSON_OBJECT('灏哄','40*30*28cm','灞傛暟','浜斿眰'), JSON_OBJECT('Size','40*30*28cm','Layers','5-ply'), 180, 220, 400, 300, 280, 0, 0, 0, 12.8000, 'CNY', 'B', 1, '浠撳偍鑰楁潗 SKU', @admin_id, @admin_id),
(@sku_camp_light, @tenant_id, @spu_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', '6970000020016', 'X004CAMP01',
 JSON_OBJECT('棰滆壊','榛戣壊','鐗堟湰','娆ц'), JSON_OBJECT('Color','Black','Version','EU'), 760, 980, 180, 120, 105, 1, 0, 0, 21.5000, 'USD', 'A', 1, '娆ф床绔欏惈鐢?SKU', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE sku_name=VALUES(sku_name), cost_price=VALUES(cost_price), status=VALUES(status);

INSERT INTO product_sku_price
(id, tenant_id, sku_id, price_type, platform, country_code, price, currency, min_qty, effective_time, is_active, create_by, update_by)
VALUES
(910000000000000411, @tenant_id, @sku_pet_white, 1, 'Amazon', 'US', 189.9900, 'USD', 1, '2026-05-01 00:00:00', 1, @admin_id, @admin_id),
(910000000000000412, @tenant_id, @sku_pet_gray, 1, 'TikTok', 'US', 199.9900, 'USD', 1, '2026-05-01 00:00:00', 1, @admin_id, @admin_id),
(910000000000000413, @tenant_id, @sku_camp_light, 1, 'Amazon', 'DE', 49.9900, 'EUR', 1, '2026-05-01 00:00:00', 1, @admin_id, @admin_id),
(910000000000000414, @tenant_id, @sku_pack_box, 2, 'Internal', 'US', 12.8000, 'CNY', 1000, '2026-05-01 00:00:00', 1, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE price=VALUES(price), is_active=VALUES(is_active);

INSERT INTO product_image
(id, tenant_id, spu_id, sku_id, image_type, image_url, thumb_url, image_width, image_height, file_size, sort_order, alt_text, create_by, update_by)
VALUES
(910000000000000421, @tenant_id, @spu_pet_dryer, @sku_pet_white, 1, 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=900', 'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=240', 900, 600, 245000, 1, '瀹犵墿鐑樺共绠变富鍥?, @admin_id, @admin_id),
(910000000000000422, @tenant_id, @spu_pack_box, @sku_pack_box, 1, 'https://images.unsplash.com/photo-1605600659873-d808a13e4d2a?w=900', 'https://images.unsplash.com/photo-1605600659873-d808a13e4d2a?w=240', 900, 600, 198000, 1, '蹇€掔焊绠变富鍥?, @admin_id, @admin_id),
(910000000000000423, @tenant_id, @spu_camp_light, @sku_camp_light, 1, 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=900', 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=240', 900, 600, 238000, 1, '鎴峰鍌ㄨ兘鐏富鍥?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE image_url=VALUES(image_url), alt_text=VALUES(alt_text);

-- Warehouses and inventory
INSERT INTO warehouse
(id, tenant_id, warehouse_code, warehouse_name, warehouse_type, country_code, country_name, province, city, address, zip_code, contact_name, contact_phone,
 contact_email, area_sqm, total_locations, used_locations, is_default, status, remark, create_by, update_by)
VALUES
(@wh_la, @tenant_id, 'US-LA-01', '缇庡浗娲涙潐鐭惰嚜钀ヤ粨', 3, 'US', '缇庡浗', 'CA', 'Los Angeles', '1200 S Alameda St, Los Angeles, CA', '90021', 'Amy Chen', '+1-213-555-0198', 'la-ops@flexchain.local', 3200.00, 3600, 1420, 1, 1, '缇庡浗瑗块儴涓讳粨', @admin_id, @admin_id),
(@wh_nj, @tenant_id, 'US-NJ-02', '缇庡浗鏂版辰瑗?3PL 浠?, 4, 'US', '缇庡浗', 'NJ', 'Newark', '88 Port Newark Ave, Newark, NJ', '07114', 'Jack Lin', '+1-973-555-0166', 'nj-3pl@flexchain.local', 2600.00, 2400, 880, 0, 1, '缇庡浗涓滈儴涓夋柟浠?, @admin_id, @admin_id),
(@wh_de, @tenant_id, 'DE-HH-01', '寰峰浗姹夊牎娴峰浠?, 3, 'DE', '寰峰浗', 'Hamburg', 'Hamburg', 'Wendenstrasse 21, Hamburg', '20097', 'Lukas Weber', '+49-40-555-0101', 'de-hh@flexchain.local', 1800.00, 1800, 730, 0, 1, '娆ф床灞ョ害浠?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name), used_locations=VALUES(used_locations), status=VALUES(status);

INSERT INTO warehouse_location
(id, tenant_id, warehouse_id, location_code, zone, row_no, column_no, floor_no, location_type, max_weight_kg, max_volume_l, is_occupied, status, remark, create_by)
VALUES
(@loc_la_a, @tenant_id, @wh_la, 'A-02-06', 'A', 2, 6, 1, 1, 250.00, 1200.00, 1, 1, '瀹犵墿鐢靛櫒甯哥敤搴撲綅', @admin_id),
(@loc_la_b, @tenant_id, @wh_la, 'B-01-03', 'B', 1, 3, 1, 2, 120.00, 600.00, 1, 1, '涓嶈壇鍝佹殏瀛?, @admin_id),
(@loc_nj_a, @tenant_id, @wh_nj, 'P-04-11', 'P', 4, 11, 1, 1, 180.00, 900.00, 1, 1, '鍖呰鑰楁潗鍖?, @admin_id),
(@loc_de_a, @tenant_id, @wh_de, 'E-03-02', 'E', 3, 2, 1, 1, 160.00, 720.00, 1, 1, '娆ф床鐣呴攢 SKU', @admin_id)
ON DUPLICATE KEY UPDATE is_occupied=VALUES(is_occupied), status=VALUES(status);

INSERT INTO inventory
(id, tenant_id, warehouse_id, location_id, sku_id, sku_code, sku_name, quantity, frozen_qty, in_transit_qty, defective_qty, reserved_qty,
 safety_stock, max_stock, reorder_point, avg_cost, total_cost, last_inbound_time, last_outbound_time, create_by, update_by)
VALUES
(910000000000000521, @tenant_id, @wh_la, @loc_la_a, 'A-02-06', @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 320, 18, 720, 4, 278, 120, 1200, 180, 850.0000, 272000.0000, '2026-05-28 09:30:00', '2026-05-28 11:42:00', @admin_id, @admin_id),
(910000000000000522, @tenant_id, @wh_la, @loc_la_a, @sku_pet_gray, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', 96, 12, 240, 2, 35, 100, 900, 160, 912.5000, 87600.0000, '2026-05-24 14:10:00', '2026-05-28 10:18:00', @admin_id, @admin_id),
(910000000000000523, @tenant_id, @wh_nj, @loc_nj_a, @sku_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', 15600, 400, 5000, 22, 2000, 3000, 30000, 5000, 12.8000, 199680.0000, '2026-05-27 16:05:00', '2026-05-28 08:10:00', @admin_id, @admin_id),
(910000000000000524, @tenant_id, @wh_de, @loc_de_a, @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', 0, 0, 460, 0, 0, 80, 1500, 120, 21.5000, 0.0000, '2026-05-19 11:20:00', '2026-05-27 19:10:00', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), frozen_qty=VALUES(frozen_qty), in_transit_qty=VALUES(in_transit_qty), defective_qty=VALUES(defective_qty), reserved_qty=VALUES(reserved_qty), safety_stock=VALUES(safety_stock), total_cost=VALUES(total_cost);

INSERT INTO inventory_log
(id, tenant_id, log_type, warehouse_id, location_id, sku_id, sku_code, sku_name, change_qty, before_qty, after_qty, ref_type, ref_no, ref_id, batch_no, operator_id, operator_name, operate_time, remark)
VALUES
(910000000000000531, @tenant_id, 1, @wh_la, @loc_la_a, 'A-02-06', @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 120, 200, 320, 'PURCHASE_RECEIPT', 'RC-202605-8871', 910000000000000711, 'BATCH-AX901-0528', @admin_id, '娴嬭瘯绠＄悊鍛?, '2026-05-28 09:30:00', '閲囪喘鍗?PO-202605-2388 棣栨壒鍏ュ簱'),
(910000000000000532, @tenant_id, 2, @wh_la, @loc_la_a, 'A-02-06', @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', -42, 362, 320, 'SALES_ORDER', 'SO-202605-1001', @order1, 'BATCH-AX901-0528', @admin_id, '璁㈠崟绯荤粺', '2026-05-28 11:42:00', 'TikTok 搴楅摵璁㈠崟鎵ｅ噺'),
(910000000000000533, @tenant_id, 1, @wh_nj, @loc_nj_a, @sku_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', 5800, 9800, 15600, 'PURCHASE_RECEIPT', 'RC-202605-8879', 910000000000000712, 'BATCH-PKG-0527', @admin_id, '娴嬭瘯绠＄悊鍛?, '2026-05-27 16:05:00', '鍖呰鑰楁潗鍏ュ簱'),
(910000000000000534, @tenant_id, 2, @wh_de, @loc_de_a, @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', -18, 18, 0, 'SALES_ORDER', 'SO-202605-1002', @order2, 'BATCH-CAMP-0527', @admin_id, '璁㈠崟绯荤粺', '2026-05-27 19:10:00', 'Amazon DE 灏炬壒鍙戣揣鍚庨浂搴撳瓨')
ON DUPLICATE KEY UPDATE change_qty=VALUES(change_qty), after_qty=VALUES(after_qty), remark=VALUES(remark);

INSERT INTO inventory_warning_event
(id, tenant_id, warehouse_id, sku_id, sku_code, warning_level, available_qty, safety_stock, status, first_detected_time, last_detected_time, resolved_time)
VALUES
(910000000000000541, @tenant_id, @wh_la, @sku_pet_white, 'SKU-US-AX901', 2, 24, 120, 1, '2026-05-28 09:00:00', '2026-05-28 12:00:00', NULL),
(910000000000000542, @tenant_id, @wh_de, @sku_camp_light, 'SKU-EU-CAMP01', 3, 0, 80, 1, '2026-05-27 19:10:00', '2026-05-28 12:00:00', NULL)
ON DUPLICATE KEY UPDATE available_qty=VALUES(available_qty), safety_stock=VALUES(safety_stock), status=VALUES(status);

-- Purchase flow
INSERT INTO purchase_requisition
(id, tenant_id, req_no, req_source, title, warehouse_id, expect_date, total_amount, priority, status, approval_level, approval_role,
 apply_user_id, apply_user_name, apply_time, audit_user_id, audit_time, audit_remark, remark, create_by, update_by)
VALUES
(@req1, @tenant_id, 'PR-202605-1001', 1, '缇庡浗娲涙潐鐭朵粨瀹犵墿鐑樺共绠辫ˉ璐?, @wh_la, '2026-06-08', 948000.00, 1, 4, 1, 'PURCHASE_MANAGER',
 @admin_id, '娴嬭瘯绠＄悊鍛?, '2026-05-26 09:10:00', @admin_id, '2026-05-26 10:00:00', '搴撳瓨棰勮瑙﹀彂锛屽噯浜堣浆閲囪喘鍗?, 'AX901/AX902 琛ヨ揣', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), total_amount=VALUES(total_amount);

INSERT INTO purchase_requisition_item
(id, tenant_id, req_id, sku_id, sku_code, sku_name, quantity, current_stock, safety_stock, in_transit_qty, ref_price, expect_date, remark)
VALUES
(910000000000000611, @tenant_id, @req1, @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 600, 24, 120, 720, 850.0000, '2026-06-08', '鍙敭搴撳瓨浣庝簬瀹夊叏搴撳瓨'),
(910000000000000612, @tenant_id, @req1, @sku_pet_gray, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', 240, 49, 100, 240, 912.5000, '2026-06-08', '鐏拌壊娆惧悓姝ヨˉ璐?)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), current_stock=VALUES(current_stock);

INSERT INTO purchase_inquiry
(id, tenant_id, inquiry_no, req_id, supplier_id, supplier_name, status, send_time, quote_deadline, quoted_time, response_hours, total_quote_amt,
 quote_valid_days, quote_expire_date, remark, supplier_remark, create_by)
VALUES
(@inq1, @tenant_id, 'INQ-202605-0601', @req1, @sup_elec, '娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃', 2, '2026-05-26 10:15:00', '2026-05-27 18:00:00',
 '2026-05-26 13:20:00', 3.08, 729000.00, 30, '2026-06-25', '璇锋寜 6 鏈堜笂鏃埌 LA 浠撴姤浠?, '鍙寜 30 澶╄处鏈熸墽琛岋紝棣栨壒 120 鍙?5 鏈?28 鏃ュ彂鍑?, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), total_quote_amt=VALUES(total_quote_amt), supplier_remark=VALUES(supplier_remark);

INSERT INTO purchase_inquiry_item
(id, tenant_id, inquiry_id, sku_id, sku_code, sku_name, inquiry_qty, quoted_price, quoted_qty, delivery_days, min_order_qty, remark)
VALUES
(910000000000000631, @tenant_id, @inq1, @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 600, 850.0000, 600, 13, 200, '鐧借壊娆剧幇璐у鏂欏厖瓒?),
(910000000000000632, @tenant_id, @inq1, @sku_pet_gray, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', 240, 912.5000, 240, 15, 200, '鐏拌壊澶栧３闇€鎺掍骇')
ON DUPLICATE KEY UPDATE quoted_price=VALUES(quoted_price), quoted_qty=VALUES(quoted_qty);

INSERT INTO purchase_order
(id, tenant_id, po_no, req_id, inquiry_id, supplier_id, supplier_name, warehouse_id, warehouse_name, total_amount, tax_amount, currency, exchange_rate,
 payment_type, payment_days, paid_amount, order_date, expected_date, confirmed_date, actual_delivery_date, status, logistics_company, tracking_no,
 contract_no, invoice_no, remark, create_by, update_by)
VALUES
(@po1, @tenant_id, 'PO-202605-2388', @req1, @inq1, @sup_elec, '娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃', @wh_la, '缇庡浗娲涙潐鐭惰嚜钀ヤ粨', 729000.00, 94770.00, 'CNY', 1.000000,
 1, 30, 0.00, '2026-05-26', '2026-06-08', '2026-05-27', '2026-05-28', 1, '椤轰赴鍥介檯', 'SFUS2026052388', 'CT-202605-HJ-01', 'INV-HJ-2026052388', '寰呬緵搴斿晢鍦?Portal 纭灏炬壒鎺掍骇璁″垝', @admin_id, @admin_id),
(@po2, @tenant_id, 'PO-202605-2391', NULL, NULL, @sup_pack, '瀹佹尝杩滆埅鍖呰鍘?, @wh_nj, '缇庡浗鏂版辰瑗?3PL 浠?, 186500.00, 24245.00, 'CNY', 1.000000,
 1, 15, 86500.00, '2026-05-22', '2026-05-31', '2026-05-23', '2026-05-27', 3, '缇庢．蹇埞', 'MSNJ2026052391', 'CT-202605-YH-02', 'INV-YH-2026052391', '鍖呰鑰楁潗鍒嗘壒鍒颁粨', @admin_id, @admin_id),
(@po3, @tenant_id, 'PO-202605-2402', NULL, NULL, @sup_outdoor, '鍘﹂棬妫捣鎴峰鐢ㄥ搧鏈夐檺鍏徃', @wh_de, '寰峰浗姹夊牎娴峰浠?, 42800.00, 0.00, 'USD', 7.200000,
 1, 45, 0.00, '2026-05-18', '2026-05-25', '2026-05-19', '2026-05-21', 4, 'DHL Freight', 'DHLDE2026052402', 'CT-202605-SH-03', 'INV-SH-2026052402', '閮ㄥ垎鍒拌揣锛屽熬鎵瑰欢璇渶璺熻繘', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), total_amount=VALUES(total_amount), paid_amount=VALUES(paid_amount), remark=VALUES(remark);

INSERT INTO purchase_order_item
(id, tenant_id, po_id, sku_id, sku_code, sku_name, spec, unit, quantity, received_qty, unit_price, amount, expect_date, remark)
VALUES
(910000000000000711, @tenant_id, @po1, @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', '鐧借壊 / 110V', '鍙?, 600, 120, 850.0000, 510000.00, '2026-06-08', '棣栨壒 120 鍙板凡鍏ュ簱'),
(910000000000000712, @tenant_id, @po1, @sku_pet_gray, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', '鐏拌壊 / 110V', '鍙?, 240, 0, 912.5000, 219000.00, '2026-06-08', '绛夊緟鎺掍骇'),
(910000000000000713, @tenant_id, @po2, @sku_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', '40*30*28cm / 浜斿眰', '涓?, 5000, 2200, 12.8000, 64000.00, '2026-05-31', '鍖呰鑰楁潗棣栨壒宸插埌'),
(910000000000000714, @tenant_id, @po3, @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', '娆ц / 榛戣壊', '鍙?, 1200, 860, 21.5000, 25800.00, '2026-05-25', '寰峰浗浠撳凡闆跺簱瀛橈紝灏炬壒鍔犳€?)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), received_qty=VALUES(received_qty), amount=VALUES(amount);

INSERT INTO purchase_receipt
(id, tenant_id, receipt_no, po_id, po_no, supplier_id, warehouse_id, receive_date, receiver_id, receiver_name, status, total_qty, pass_qty, reject_qty, is_on_time, remark, create_by, update_by)
VALUES
(910000000000000721, @tenant_id, 'RC-202605-8871', @po1, 'PO-202605-2388', @sup_elec, @wh_la, '2026-05-28', @admin_id, '娴嬭瘯绠＄悊鍛?, 2, 120, 116, 4, 1, '棣栨壒鍒拌揣锛? 鍙板绠辩牬鎹熻浆涓嶈壇', @admin_id, @admin_id),
(910000000000000722, @tenant_id, 'RC-202605-8879', @po2, 'PO-202605-2391', @sup_pack, @wh_nj, '2026-05-27', @admin_id, '娴嬭瘯绠＄悊鍛?, 2, 5800, 5778, 22, 1, '鍖呰鑰楁潗棣栨壒鍏ュ簱', @admin_id, @admin_id),
(910000000000000723, @tenant_id, 'RC-202605-8882', @po3, 'PO-202605-2402', @sup_outdoor, @wh_de, '2026-05-25', @admin_id, '娴嬭瘯绠＄悊鍛?, 2, 860, 860, 0, 0, '鍒拌揣寤惰繜 1 澶?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE total_qty=VALUES(total_qty), pass_qty=VALUES(pass_qty), status=VALUES(status);

INSERT INTO purchase_receipt_item
(id, tenant_id, receipt_id, po_item_id, sku_id, sku_code, sku_name, expected_qty, actual_qty, pass_qty, reject_qty, reject_reason, location_id, status)
VALUES
(910000000000000731, @tenant_id, 910000000000000721, 910000000000000711, @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 120, 120, 116, 4, '澶栫鐮存崯', @loc_la_a, 1),
(910000000000000732, @tenant_id, 910000000000000722, 910000000000000713, @sku_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', 5800, 5800, 5778, 22, '杈硅鍘嬬棔', @loc_nj_a, 1),
(910000000000000733, @tenant_id, 910000000000000723, 910000000000000714, @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', 860, 860, 860, 0, NULL, @loc_de_a, 1)
ON DUPLICATE KEY UPDATE actual_qty=VALUES(actual_qty), pass_qty=VALUES(pass_qty), reject_qty=VALUES(reject_qty);

INSERT INTO inbound_order
(id, tenant_id, inbound_no, inbound_type, warehouse_id, warehouse_name, ref_type, ref_id, ref_no, expected_date, actual_date, status, total_sku_count, total_qty, actual_qty, operator_id, remark, create_by, update_by)
VALUES
(@in1, @tenant_id, 'IN-202605-8871', 1, @wh_la, '缇庡浗娲涙潐鐭惰嚜钀ヤ粨', 'PURCHASE_ORDER', @po1, 'PO-202605-2388', '2026-06-08', '2026-05-28', 1, 2, 840, 120, @admin_id, '閲囪喘鍏ュ簱棣栨壒锛屽熬鎵瑰湪閫?, @admin_id, @admin_id),
(@in2, @tenant_id, 'IN-202605-8879', 1, @wh_nj, '缇庡浗鏂版辰瑗?3PL 浠?, 'PURCHASE_ORDER', @po2, 'PO-202605-2391', '2026-05-31', '2026-05-27', 2, 1, 5000, 2200, @admin_id, '鍖呰鑰楁潗閮ㄥ垎鍏ュ簱', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), actual_qty=VALUES(actual_qty), remark=VALUES(remark);

INSERT INTO inbound_order_item
(id, tenant_id, inbound_id, sku_id, sku_code, sku_name, expected_qty, actual_qty, defective_qty, location_id, location_code, unit_cost, status, remark)
VALUES
(910000000000000811, @tenant_id, @in1, @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 600, 120, 4, @loc_la_a, 'A-02-06', 850.0000, 2, '棣栨壒宸蹭笂鏋?),
(910000000000000812, @tenant_id, @in1, @sku_pet_gray, 'SKU-US-AX902', '澶氳鏍煎疇鐗╃儤骞茬 鐏拌壊 110V', 240, 0, 0, @loc_la_a, 'A-02-06', 912.5000, 0, '寰呭埌璐?),
(910000000000000813, @tenant_id, @in2, @sku_pack_box, 'SKU-PKG-110', '鎶楀帇蹇€掔焊绠?40*30*28cm', 5000, 2200, 22, @loc_nj_a, 'P-04-11', 12.8000, 2, '棣栨壒宸插叆搴?)
ON DUPLICATE KEY UPDATE actual_qty=VALUES(actual_qty), defective_qty=VALUES(defective_qty), status=VALUES(status);

INSERT INTO stocktake_task
(id, tenant_id, task_no, task_type, warehouse_id, task_name, plan_date, start_time, end_time, status, total_sku_count, diff_sku_count,
 profit_qty, loss_qty, profit_amount, loss_amount, auditor_id, audit_time, remark, create_by, update_by)
VALUES
(@st1, @tenant_id, 'STK-202605-2099', 1, @wh_de, '寰峰浗姹夊牎浠撴湀搴﹀惊鐜洏鐐?, '2026-05-27', '2026-05-27 08:00:00', NULL, 1, 12, 2, 14, 7, 301.00, 150.50, NULL, NULL, '杩涜涓紝閲嶇偣鏍稿鍚數 SKU', @admin_id, @admin_id),
(@st2, @tenant_id, 'STK-202605-2104', 2, @wh_la, '娲涙潐鐭朵粨鍔ㄩ攢 SKU 宸紓澶嶆牳', '2026-05-26', '2026-05-26 10:30:00', '2026-05-26 18:00:00', 2, 18, 3, 2, 31, 1700.00, 26350.00, @admin_id, NULL, '绛夊緟浠撳偍涓荤瀹℃牳璋冩暣', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), profit_qty=VALUES(profit_qty), loss_qty=VALUES(loss_qty), remark=VALUES(remark);

INSERT INTO stocktake_item
(id, tenant_id, task_id, warehouse_id, location_id, location_code, sku_id, sku_code, sku_name, book_qty, actual_qty, diff_qty, diff_amount, diff_reason, is_adjusted, adjust_time, picker_id, pick_time)
VALUES
(910000000000000911, @tenant_id, @st1, @wh_de, @loc_de_a, 'E-03-02', @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', 0, 0, 0, 0.00, '璐﹀疄涓€鑷?, 0, NULL, @admin_id, '2026-05-27 11:20:00'),
(910000000000000912, @tenant_id, @st2, @wh_la, @loc_la_a, 'A-02-06', @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 351, 320, -31, 26350.00, '閿€鍞嚭搴撳洖鍐欏欢杩?, 0, NULL, @admin_id, '2026-05-26 16:30:00')
ON DUPLICATE KEY UPDATE actual_qty=VALUES(actual_qty), diff_qty=VALUES(diff_qty), diff_reason=VALUES(diff_reason);

-- OMS and TMS sample data
INSERT INTO order_main
(id, tenant_id, order_no, platform, platform_order_no, store_id, total_amount, discount_amount, shipping_fee, payment_amount, currency, exchange_rate, cny_amount,
 platform_fee, status, cancel_reason, is_abnormal, abnormal_reason, warehouse_id, logistics_channel, waybill_no, ship_time, delivery_deadline, signed_time,
 platform_order_time, platform_pay_time, create_by, update_by)
VALUES
(@order1, @tenant_id, 'SO-202605-1001', 'TikTok', 'TT-US-202605280001', 1001, 199.99, 10.00, 0.00, 189.99, 'USD', 7.200000, 1367.93,
 18.20, 6, NULL, 0, NULL, @wh_la, 'YunExpress US Standard', 'WB-202605-9001', '2026-05-28 13:20:00', '2026-05-29', NULL, '2026-05-28 09:15:00', '2026-05-28 09:16:00', @admin_id, @admin_id),
(@order2, @tenant_id, 'SO-202605-1002', 'Amazon', 'AMZ-DE-303-778899', 2001, 49.99, 0.00, 4.99, 54.98, 'EUR', 7.800000, 428.84,
 7.60, 7, NULL, 1, '鐩殑鍥借建杩硅秴杩?7 澶╂湭鏇存柊', @wh_de, 'DHL Paket DE', 'WB-202605-9002', '2026-05-21 11:30:00', '2026-05-22', NULL, '2026-05-20 20:05:00', '2026-05-20 20:07:00', @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), is_abnormal=VALUES(is_abnormal), abnormal_reason=VALUES(abnormal_reason);

INSERT INTO order_item
(id, tenant_id, order_id, order_no, sku_id, sku_code, sku_name, platform_sku_id, quantity, unit_price, discount, amount, currency, refunded_qty)
VALUES
(910000000000001011, @tenant_id, @order1, 'SO-202605-1001', @sku_pet_white, 'SKU-US-AX901', '澶氳鏍煎疇鐗╃儤骞茬 鐧借壊 110V', 'TT-SKU-AX901', 1, 199.9900, 10.00, 189.99, 'USD', 0),
(910000000000001012, @tenant_id, @order2, 'SO-202605-1002', @sku_camp_light, 'SKU-EU-CAMP01', '鎶樺彔寮忔埛澶栧偍鑳界伅 娆ц榛戣壊', 'AMZ-SKU-CAMP01', 1, 49.9900, 0.00, 49.99, 'EUR', 0)
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity), amount=VALUES(amount);

INSERT INTO order_address
(id, tenant_id, order_id, receiver_name, phone, email, country_code, country_name, state, city, address_line1, address_line2, zip_code, full_address, is_verified)
VALUES
(910000000000001021, @tenant_id, @order1, 'M*** Johnson', '+1-213-***-0198', 'buyer-us@example.com', 'US', '缇庡浗', 'CA', 'Los Angeles', '742 Evergreen Terrace', 'Apt 6', '90021', '742 Evergreen Terrace Apt 6, Los Angeles, CA 90021, US', 1),
(910000000000001022, @tenant_id, @order2, 'L*** M眉ller', '+49-40-***-0101', 'buyer-de@example.com', 'DE', '寰峰浗', 'Hamburg', 'Hamburg', 'Musterstrasse 18', NULL, '20097', 'Musterstrasse 18, 20097 Hamburg, DE', 1)
ON DUPLICATE KEY UPDATE receiver_name=VALUES(receiver_name), full_address=VALUES(full_address);

INSERT INTO logistics_carrier
(id, tenant_id, carrier_code, carrier_name, carrier_name_en, carrier_type, support_label, support_track, status, remark, create_by, update_by)
VALUES
(@carrier1, @tenant_id, 'YUNEXPRESS', '浜戦€旂墿娴?, 'YunExpress', 1, 1, 1, 1, '缇庡浗灏忓寘鏍囧噯娓犻亾', @admin_id, @admin_id),
(@carrier2, @tenant_id, 'DHL', 'DHL 寰峰浗', 'DHL', 1, 1, 1, 1, '娆ф床鏈湴娲鹃€佹笭閬?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE carrier_name=VALUES(carrier_name), status=VALUES(status);

INSERT INTO logistics_channel
(id, tenant_id, carrier_id, channel_code, channel_name, channel_type, country_codes, min_weight_g, max_weight_g, max_length_mm, max_girth_mm,
 allow_battery, allow_liquid, allow_powder, allow_food, min_days, max_days, volume_factor, declared_value_limit, status, sort_order, remark, create_by, update_by)
VALUES
(@channel1, @tenant_id, @carrier1, 'YUN-US-STD', '浜戦€旂編鍥芥爣鍑嗗皬鍖?, 1, JSON_ARRAY('US'), 0, 30000, 600, 1200, 0, 0, 0, 1, 6.0, 10.0, 5000, 800.00, 1, 10, '缇庡浗杞诲皬浠堕閫?, @admin_id, @admin_id),
(@channel2, @tenant_id, @carrier2, 'DHL-DE-PAKET', 'DHL 寰峰浗鏈湴娲鹃€?, 1, JSON_ARRAY('DE','AT'), 0, 31500, 1200, 3000, 1, 0, 0, 1, 2.0, 5.0, 5000, 1000.00, 1, 20, '寰峰浗鏈湴浠撴淳閫?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE channel_name=VALUES(channel_name), status=VALUES(status);

INSERT INTO logistics_waybill
(id, tenant_id, waybill_no, tracking_no, carrier_id, channel_id, order_id, order_no, warehouse_id, receiver_name, receiver_phone, country_code,
 state, city, address_line1, address_line2, zip_code, actual_weight_g, volume_weight_g, charge_weight_g, length_mm, width_mm, height_mm,
 package_count, declared_value, declared_currency, declared_name_en, hs_code, is_gift, estimated_fee, actual_fee, fee_currency, status,
 label_url, label_format, create_waybill_time, pickup_time, signed_time, exception_desc, create_by, update_by)
VALUES
(@waybill1, @tenant_id, 'WB-202605-9001', 'YT2515280001US', @carrier1, @channel1, @order1, 'SO-202605-1001', @wh_la, 'M*** Johnson', '+1-213-***-0198', 'US',
 'CA', 'Los Angeles', '742 Evergreen Terrace', 'Apt 6', '90021', 6100, 9800, 9800, 520, 410, 460, 1, 89.00, 'USD', 'Pet Dryer', '84193990', 0, 126.50, 130.80, 'CNY', 2,
 'oss://demo/labels/WB-202605-9001.pdf', 'PDF', '2026-05-28 12:50:00', '2026-05-28 16:30:00', NULL, NULL, @admin_id, @admin_id),
(@waybill2, @tenant_id, 'WB-202605-9002', 'DHLDE05219002', @carrier2, @channel2, @order2, 'SO-202605-1002', @wh_de, 'L*** M眉ller', '+49-40-***-0101', 'DE',
 'Hamburg', 'Hamburg', 'Musterstrasse 18', NULL, '20097', 980, 1200, 1200, 180, 120, 105, 1, 28.00, 'EUR', 'Camping Lantern', '94054100', 0, 36.20, 36.20, 'CNY', 5,
 'oss://demo/labels/WB-202605-9002.pdf', 'PDF', '2026-05-21 10:40:00', '2026-05-21 15:00:00', NULL, '瓒呰繃 7 澶╂棤鏂拌建杩癸紝闇€瑕佸鏈嶈窡杩?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE status=VALUES(status), actual_fee=VALUES(actual_fee), exception_desc=VALUES(exception_desc);

INSERT INTO logistics_track
(id, tenant_id, waybill_id, tracking_no, track_code, track_stage, raw_status, status_desc, location, location_country, track_time, fetch_time, is_exception, exception_type, exception_desc)
VALUES
(910000000000001211, @tenant_id, @waybill1, 'YT2515280001US', 'PICKED', 2, 'Picked up', '鐗╂祦鍟嗗凡鎻芥敹', 'Los Angeles, CA', 'US', '2026-05-28 16:30:00', '2026-05-28 16:40:00', 0, NULL, NULL),
(910000000000001212, @tenant_id, @waybill1, 'YT2515280001US', 'TRANSIT', 3, 'In transit', '杩愯緭閫斾腑', 'YunExpress LA Hub', 'US', '2026-05-28 20:10:00', '2026-05-28 20:20:00', 0, NULL, NULL),
(910000000000001213, @tenant_id, @waybill2, 'DHLDE05219002', 'NO_UPDATE', 6, 'No update', '瓒呰繃 7 澶╂棤杞ㄨ抗鏇存柊', 'Hamburg', 'DE', '2026-05-21 18:40:00', '2026-05-28 08:00:00', 1, 2, '鍖呰９鍋滄粸锛岄渶鑱旂郴 DHL 鏍告煡')
ON DUPLICATE KEY UPDATE status_desc=VALUES(status_desc), is_exception=VALUES(is_exception);

INSERT INTO logistics_fee_record
(id, tenant_id, waybill_id, waybill_no, base_fee, fuel_surcharge, peak_surcharge, remote_fee, oversize_fee, insurance_fee, other_fee, estimated_total, actual_total, currency, billing_weight_g, rate_id)
VALUES
(910000000000001221, @tenant_id, @waybill1, 'WB-202605-9001', 98.20, 12.40, 0.00, 0.00, 0.00, 4.20, 16.00, 126.50, 130.80, 'CNY', 9800, NULL),
(910000000000001222, @tenant_id, @waybill2, 'WB-202605-9002', 28.00, 3.20, 0.00, 0.00, 0.00, 0.00, 5.00, 36.20, 36.20, 'CNY', 1200, NULL)
ON DUPLICATE KEY UPDATE actual_total=VALUES(actual_total);

-- Finance and BI
INSERT INTO finance_payable
(id, tenant_id, payable_no, source_type, source_biz_no, po_id, po_no, supplier_id, supplier_name, invoice_no, invoice_date, payable_amount, paid_amount,
 currency, payment_days, due_date, status, overdue_days, remark, create_by, update_by)
VALUES
(910000000000001401, @tenant_id, 'AP-202605-2388', 'PURCHASE_ORDER', 'PO-202605-2388', @po1, 'PO-202605-2388', @sup_elec, '娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃', 'INV-HJ-2026052388', '2026-05-28', 729000.00, 0.00, 'CNY', 30, '2026-06-25', 0, 0, '瀹犵墿鐑樺共绠遍噰璐簲浠?, @admin_id, @admin_id),
(910000000000001402, @tenant_id, 'AP-202605-2391', 'PURCHASE_ORDER', 'PO-202605-2391', @po2, 'PO-202605-2391', @sup_pack, '瀹佹尝杩滆埅鍖呰鍘?, 'INV-YH-2026052391', '2026-05-27', 186500.00, 86500.00, 'CNY', 15, '2026-06-15', 1, 0, '鍖呰鑰楁潗閮ㄥ垎浠樻', @admin_id, @admin_id),
(910000000000001403, @tenant_id, 'AP-202605-2402', 'PURCHASE_ORDER', 'PO-202605-2402', @po3, 'PO-202605-2402', @sup_outdoor, '鍘﹂棬妫捣鎴峰鐢ㄥ搧鏈夐檺鍏徃', 'INV-SH-2026052402', '2026-05-25', 42800.00, 0.00, 'USD', 45, '2026-06-10', 0, 0, '鎴峰鍌ㄨ兘鐏噰璐簲浠?, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE payable_amount=VALUES(payable_amount), paid_amount=VALUES(paid_amount), status=VALUES(status);

INSERT INTO finance_platform_bill
(id, tenant_id, bill_no, platform, store_id, store_name, platform_bill_id, settlement_start, settlement_end, currency, total_sales, total_refund,
 referral_fee, fba_fee, storage_fee, advertising_fee, other_fee, net_amount, cny_amount, exchange_rate, status, source_file_url, import_time, import_user_id,
 create_by, update_by)
VALUES
(@bill1, @tenant_id, 'BILL-202605-AMZ-US', 'Amazon', 'AMZ-US-001', 'FlexPet Amazon US', 'AMZ-STL-202605-01', '2026-05-01', '2026-05-28', 'USD',
 128600.00, 3800.00, 19290.00, 8400.00, 1260.00, 9800.00, 720.00, 85330.00, 614376.00, 7.200000, 3, 'oss://demo/bills/amazon-us-202605.csv', '2026-05-28 08:30:00', @admin_id, @admin_id, @admin_id)
ON DUPLICATE KEY UPDATE total_sales=VALUES(total_sales), net_amount=VALUES(net_amount), status=VALUES(status);

INSERT INTO finance_bill_item
(id, tenant_id, bill_id, item_type, order_no, sku_id, platform_sku, amount, currency, description, transaction_date, is_matched, match_order_id)
VALUES
(910000000000001311, @tenant_id, @bill1, 'SALE', 'TT-US-202605280001', @sku_pet_white, 'TT-SKU-AX901', 189.9900, 'USD', 'TikTok US 閿€鍞敹鍏?, '2026-05-28', 1, @order1),
(910000000000001312, @tenant_id, @bill1, 'REFERRAL_FEE', 'TT-US-202605280001', @sku_pet_white, 'TT-SKU-AX901', -18.2000, 'USD', '骞冲彴浣ｉ噾', '2026-05-28', 1, @order1),
(910000000000001313, @tenant_id, @bill1, 'ADVERTISING_FEE', NULL, NULL, NULL, -9800.0000, 'USD', '绔欏唴骞垮憡鎶曟斁', '2026-05-27', 0, NULL)
ON DUPLICATE KEY UPDATE amount=VALUES(amount), is_matched=VALUES(is_matched);

INSERT INTO finance_profit_snapshot
(id, tenant_id, snapshot_type, snapshot_date, sku_id, sku_code, platform, store_id, country_code, currency, exchange_rate, order_count, sales_qty,
 gross_revenue, gross_revenue_cny, purchase_cost, logistics_fee, platform_fee, fba_storage_fee, advertising_fee, refund_loss, vat_fee, other_cost,
 total_cost, gross_profit, net_profit, gross_margin, net_margin)
VALUES
(910000000000001501, @tenant_id, 1, '2026-05-28', @sku_pet_white, 'SKU-US-AX901', 'TikTok', 'TT-US-001', 'US', 'USD', 7.200000, 86, 86, 16339.14, 117641.81, 73100.00, 10948.00, 11764.18, 0.00, 6800.00, 0.00, 0.00, 920.00, 103532.18, 44541.81, 14109.63, 0.3786, 0.1199),
(910000000000001502, @tenant_id, 1, '2026-05-28', @sku_camp_light, 'SKU-EU-CAMP01', 'Amazon', 'AMZ-DE-001', 'DE', 'EUR', 7.800000, 18, 18, 899.82, 7018.60, 387.00, 651.60, 1053.00, 88.00, 420.00, 0.00, 1333.53, 120.00, 4013.13, 6631.60, 3005.47, 0.9449, 0.4282)
ON DUPLICATE KEY UPDATE order_count=VALUES(order_count), net_profit=VALUES(net_profit), net_margin=VALUES(net_margin);

INSERT INTO finance_cash_flow
(id, tenant_id, flow_date, flow_type, source_type, source_id, source_no, amount_cny, amount_origin, currency, exchange_rate, remark, create_by)
VALUES
(910000000000001601, @tenant_id, '2026-05-28', 1, 'PLATFORM_SETTLEMENT', @bill1, 'BILL-202605-AMZ-US', 614376.00, 85330.00, 'USD', 7.200000, 'Amazon US 缁撶畻鍏ヨ处棰勬祴', @admin_id),
(910000000000001602, @tenant_id, '2026-05-27', 2, 'PURCHASE_PAYMENT', @po2, 'PO-202605-2391', 86500.00, 86500.00, 'CNY', 1.000000, '瀹佹尝杩滆埅鍖呰鍘傞浠樻', @admin_id),
(910000000000001603, @tenant_id, '2026-05-28', 2, 'LOGISTICS_FEE', @waybill1, 'WB-202605-9001', 130.80, 130.80, 'CNY', 1.000000, '浜戦€旂編鍥藉皬鍖呰繍璐?, @admin_id)
ON DUPLICATE KEY UPDATE amount_cny=VALUES(amount_cny), remark=VALUES(remark);

INSERT INTO bi_kpi_threshold
(id, tenant_id, kpi_code, kpi_name, warning_value, danger_value, compare_type, notify_roles, is_enabled)
VALUES
(910000000000001701, @tenant_id, 'inventory_turnover', '搴撳瓨鍛ㄨ浆鐜?, 3.0000, 1.5000, 1, JSON_ARRAY('ROLE_TENANT_ADMIN','ROLE_WAREHOUSE'), 1),
(910000000000001702, @tenant_id, 'stockout_rate', '缂鸿揣鐜?, 0.0500, 0.1000, 2, JSON_ARRAY('ROLE_TENANT_ADMIN','ROLE_PURCHASE'), 1),
(910000000000001703, @tenant_id, 'supplier_otd', '渚涘簲鍟嗗噯鏃剁巼', 0.9000, 0.8000, 1, JSON_ARRAY('ROLE_TENANT_ADMIN','ROLE_PURCHASE'), 1)
ON DUPLICATE KEY UPDATE warning_value=VALUES(warning_value), danger_value=VALUES(danger_value), is_enabled=VALUES(is_enabled);

INSERT INTO sys_message
(id, tenant_id, receiver_id, receiver_type, receiver_key, title, content, biz_type, biz_id, priority, read_status)
VALUES
(910000000000001801, @tenant_id, @admin_id, 'USER', NULL, '搴撳瓨棰勮锛歋KU-US-AX901 鍙敭搴撳瓨涓嶈冻', '缇庡浗娲涙潐鐭朵粨 SKU-US-AX901 褰撳墠鍙敭浣庝簬瀹夊叏搴撳瓨锛岃浼樺厛琛ヨ揣鎴栬皟鏁村钩鍙板垎閰嶃€?, 'WMS_INVENTORY_WARNING', CAST(@sku_pet_white AS CHAR), 'URGENT', 0),
(910000000000001802, @tenant_id, @admin_id, 'USER', NULL, '閲囪喘鍗曞緟渚涘簲鍟嗙‘璁?, 'PO-202605-2388 宸插彂閫佺粰娣卞湷鍗庢櫙鐢靛瓙鏈夐檺鍏徃锛屽熬鎵逛粛寰呯‘璁ゆ帓浜ф棩鏈熴€?, 'PMS_PURCHASE_ORDER', CAST(@po1 AS CHAR), 'HIGH', 0),
(910000000000001803, @tenant_id, @admin_id, 'USER', NULL, '鐗╂祦寮傚父锛欴HLDE05219002 瓒?7 澶╂棤杞ㄨ抗', '寰峰浗姹夊牎浠撳彂鍑虹殑鎴峰鍌ㄨ兘鐏鍗曠墿娴佸仠婊烇紝璇疯仈绯?DHL 鏍告煡銆?, 'TMS_WAYBILL_EXCEPTION', CAST(@waybill2 AS CHAR), 'HIGH', 0),
(910000000000001804, @tenant_id, @admin_id, 'USER', NULL, '鐩樼偣宸紓寰呭鏍?, '娲涙潐鐭朵粨鍔ㄩ攢 SKU 宸紓澶嶆牳浠诲姟 STK-202605-2104 宸插畬鎴愬綍鍏ワ紝绛夊緟瀹℃牳璋冩暣銆?, 'WMS_STOCKTAKE', CAST(@st2 AS CHAR), 'NORMAL', 0)
ON DUPLICATE KEY UPDATE title=VALUES(title), content=VALUES(content), priority=VALUES(priority), read_status=VALUES(read_status);

SELECT 'FlexChain MVP demo data seed completed' AS result,
       @tenant_id AS tenant_id,
       (SELECT COUNT(*) FROM purchase_order WHERE tenant_id=@tenant_id AND is_deleted=0) AS purchase_orders,
       (SELECT COUNT(*) FROM inventory WHERE tenant_id=@tenant_id AND is_deleted=0) AS inventory_rows,
       (SELECT COUNT(*) FROM inbound_order WHERE tenant_id=@tenant_id AND is_deleted=0) AS inbound_orders,
       (SELECT COUNT(*) FROM order_main WHERE tenant_id=@tenant_id AND is_deleted=0) AS sales_orders,
       (SELECT COUNT(*) FROM sys_message WHERE tenant_id=@tenant_id AND read_status=0 AND is_deleted=0) AS unread_messages;

