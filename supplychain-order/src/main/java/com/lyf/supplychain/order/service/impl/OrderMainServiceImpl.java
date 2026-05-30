package com.lyf.supplychain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.common.feign.warehouse.WarehouseFeignClient;
import com.lyf.supplychain.common.feign.warehouse.WarehouseOutboundOrderCreateRequest;
import com.lyf.supplychain.common.feign.warehouse.WarehouseStockItem;
import com.lyf.supplychain.common.security.annotation.DataScope;
import com.lyf.supplychain.common.security.datascope.DataScopeQueryHelper;
import com.lyf.supplychain.common.security.datascope.DataScopeResource;
import com.lyf.supplychain.order.constant.OrderConstants;
import com.lyf.supplychain.order.entity.OrderAddress;
import com.lyf.supplychain.order.entity.OrderItem;
import com.lyf.supplychain.order.entity.OrderLog;
import com.lyf.supplychain.order.entity.OrderMain;
import com.lyf.supplychain.order.entity.OrderPlatformRaw;
import com.lyf.supplychain.order.mapper.OrderAddressMapper;
import com.lyf.supplychain.order.mapper.OrderItemMapper;
import com.lyf.supplychain.order.mapper.OrderLogMapper;
import com.lyf.supplychain.order.mapper.OrderMainMapper;
import com.lyf.supplychain.order.mapper.OrderPlatformRawMapper;
import com.lyf.supplychain.order.request.OrderCancelRequest;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderFlagRequest;
import com.lyf.supplychain.order.request.OrderItemRequest;
import com.lyf.supplychain.order.request.OrderMergeRequest;
import com.lyf.supplychain.order.request.OrderPageQuery;
import com.lyf.supplychain.order.request.OrderSplitRequest;
import com.lyf.supplychain.order.request.WebhookRequest;
import com.lyf.supplychain.order.service.OrderMainService;
import com.lyf.supplychain.order.service.OrderNumberService;
import com.lyf.supplychain.order.service.PlatformOrderStandardizationService;
import com.lyf.supplychain.order.service.PlatformInventoryAllocationService;
import com.lyf.supplychain.order.service.PlatformWebhookSignatureVerifier;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单主流程业务服务实现。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Service
public class OrderMainServiceImpl implements OrderMainService {

    private static final Logger log = LoggerFactory.getLogger(OrderMainServiceImpl.class);

    private final OrderMainMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final OrderAddressMapper addressMapper;
    private final OrderLogMapper logMapper;
    private final OrderPlatformRawMapper rawMapper;
    private final OrderNumberService numberService;
    private final StringRedisTemplate redisTemplate;
    private final WarehouseFeignClient warehouseFeignClient;
    private final PlatformInventoryAllocationService platformInventoryAllocationService;
    private final PlatformWebhookSignatureVerifier webhookSignatureVerifier;
    private final PlatformOrderStandardizationService orderStandardizationService;

    public OrderMainServiceImpl(OrderMainMapper orderMapper,
                                OrderItemMapper itemMapper,
                                OrderAddressMapper addressMapper,
                                OrderLogMapper logMapper,
                                OrderPlatformRawMapper rawMapper,
                                OrderNumberService numberService,
                                StringRedisTemplate redisTemplate,
                                WarehouseFeignClient warehouseFeignClient,
                                PlatformInventoryAllocationService platformInventoryAllocationService,
                                PlatformWebhookSignatureVerifier webhookSignatureVerifier,
                                PlatformOrderStandardizationService orderStandardizationService) {
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
        this.addressMapper = addressMapper;
        this.logMapper = logMapper;
        this.rawMapper = rawMapper;
        this.numberService = numberService;
        this.redisTemplate = redisTemplate;
        this.warehouseFeignClient = warehouseFeignClient;
        this.platformInventoryAllocationService = platformInventoryAllocationService;
        this.webhookSignatureVerifier = webhookSignatureVerifier;
        this.orderStandardizationService = orderStandardizationService;
    }

    /**
     * 分页查询订单。
     *
     * @param query 分页参数
     * @return 订单分页结果
     */
    @Override
    @DataScope(resource = DataScopeResource.STORE)
    public PageResult<OrderMain> page(OrderPageQuery query) {
        query.normalize();
        QueryWrapper<OrderMain> wrapper = DataScopeQueryHelper.apply(new QueryWrapper<OrderMain>(),
                "create_by", "store_id", "warehouse_id", null).orderByDesc("create_time");
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like("order_no", keyword)
                    .or().like("platform_order_no", keyword)
                    .or().like("platform", keyword)
                    .or().like("waybill_no", keyword));
        }
        Page<OrderMain> page = orderMapper.selectPage(Page.of(query.getPageNum(), query.getPageSize()),
                wrapper);
        return PageResult.from(page);
    }

    /**
     * 查询订单详情。
     *
     * @param id 订单ID
     * @return 订单
     */
    @Override
    public OrderMain detail(Long id) {
        OrderMain order = orderMapper.selectById(id);
        if (order == null) {
            BusinessException.throwException(15001, "订单不存在");
        }
        return order;
    }

    /**
     * 手动创建订单并执行幂等与 Redis 防超卖。
     *
     * @param request 创建请求
     * @return 订单ID
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateRequest request) {
        OrderMain exists = orderMapper.selectOne(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getPlatform, request.getPlatform())
                .eq(OrderMain::getPlatformOrderNo, request.getPlatformOrderNo())
                .last("limit 1"));
        if (exists != null) {
            return exists.getId();
        }
        List<DeductedRedisStock> deductedStocks = deductRedisStock(request);
        try {
            platformInventoryAllocationService.freezeForOrder(request);
            OrderMain order = buildOrder(request);
            orderMapper.insert(order);
            saveItems(order, request.getItems());
            saveAddress(order.getId(), request);
            writeLog(order, null, order.getStatus(), "创建订单", "订单幂等创建成功");
            createWmsOutbound(order);
            return order.getId();
        } catch (RuntimeException ex) {
            rollbackRedisStock(deductedStocks);
            throw ex;
        }
    }

    /**
     * 取消订单。
     *
     * @param id      订单ID
     * @param request 取消请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, OrderCancelRequest request) {
        OrderMain order = detail(id);
        if (order.getStatus() >= OrderConstants.SHIPPED) {
            BusinessException.throwException(15006, "订单已发货，不能取消");
        }
        platformInventoryAllocationService.releaseForCancel(order, orderItems(order.getId()));
        changeStatus(order, OrderConstants.CANCELED, "取消订单", request.getReason());
        order.setCancelReason(request.getReason());
        orderMapper.updateById(order);
    }

    /**
     * 风控审核通过。
     *
     * @param id 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        OrderMain order = detail(id);
        if (order.getStatus() <= OrderConstants.WAIT_SHIP) {
            createWmsOutbound(order);
        }
        changeStatus(order, OrderConstants.WAIT_SHIP, "风控审核通过", "订单进入待发货");
    }

    /**
     * 风控审核拒绝。
     *
     * @param id 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id) {
        changeStatus(detail(id), OrderConstants.CANCELED, "风控审核拒绝", "订单取消");
    }

    /**
     * 标记异常订单。
     *
     * @param id      订单ID
     * @param request 异常请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void flag(Long id, OrderFlagRequest request) {
        OrderMain order = detail(id);
        order.setIsAbnormal(1);
        order.setAbnormalReason(request.getReason());
        orderMapper.updateById(order);
        writeLog(order, order.getStatus(), order.getStatus(), "标记异常", request.getReason());
    }

    /**
     * 同步平台状态。
     *
     * @param id 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sync(Long id) {
        OrderMain order = detail(id);
        writeLog(order, order.getStatus(), order.getStatus(), "同步平台状态", "已触发平台同步占位逻辑");
    }

    /**
     * 查询订单操作日志。
     *
     * @param id 订单ID
     * @return 日志列表
     */
    @Override
    public List<OrderLog> logs(Long id) {
        return logMapper.selectList(new LambdaQueryWrapper<OrderLog>()
                .eq(OrderLog::getOrderId, id)
                .orderByDesc(OrderLog::getOperateTime));
    }

    /**
     * 拆分订单。
     *
     * @param id      订单ID
     * @param request 拆单请求
     * @return 新订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long split(Long id, OrderSplitRequest request) {
        OrderMain source = detail(id);
        if (source.getStatus() > OrderConstants.WAIT_SHIP) {
            BusinessException.throwException(15008, "已发货订单不能拆单");
        }
        OrderMain target = copyOrder(source);
        target.setOrderNo(numberService.nextNo("ORD"));
        orderMapper.insert(target);
        List<OrderItem> moveItems = itemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id)
                .in(OrderItem::getSkuId, request.getSkuIds()));
        if (moveItems.isEmpty()) {
            BusinessException.throwException(15008, "拆单失败，明细不满足拆单条件");
        }
        for (OrderItem item : moveItems) {
            item.setOrderId(target.getId());
            item.setOrderNo(target.getOrderNo());
            itemMapper.updateById(item);
        }
        writeLog(source, source.getStatus(), source.getStatus(), "拆单", "拆出订单：" + target.getOrderNo());
        writeLog(target, null, target.getStatus(), "拆单生成", "来源订单：" + source.getOrderNo());
        return target.getId();
    }

    /**
     * 合并订单。
     *
     * @param request 合单请求
     * @return 主订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long merge(OrderMergeRequest request) {
        List<OrderMain> orders = orderMapper.selectBatchIds(request.getOrderIds());
        if (orders.size() < 2) {
            BusinessException.throwException(15009, "合单失败，订单数量不足");
        }
        OrderMain main = orders.get(0);
        for (OrderMain order : orders) {
            if (!main.getStatus().equals(order.getStatus())) {
                BusinessException.throwException(15009, "合单失败，订单状态不一致");
            }
            if (!main.getWarehouseId().equals(order.getWarehouseId())) {
                BusinessException.throwException(15009, "合单失败，仓库不一致");
            }
        }
        for (int i = 1; i < orders.size(); i++) {
            OrderMain order = orders.get(i);
            itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()))
                    .forEach(item -> {
                        item.setOrderId(main.getId());
                        item.setOrderNo(main.getOrderNo());
                        itemMapper.updateById(item);
                    });
            changeStatus(order, OrderConstants.CANCELED, "合单取消", "合并到订单：" + main.getOrderNo());
        }
        writeLog(main, main.getStatus(), main.getStatus(), "合单", "合并订单：" + request.getOrderIds());
        return main.getId();
    }

    /**
     * 接收平台 Webhook 并保存原始报文。
     *
     * @param platform 平台
     * @param request  Webhook 请求
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void webhook(String platform, WebhookRequest request) {
        webhookSignatureVerifier.verify(platform, request.getRawData(), request.getSignature());
        Long orderId = ingestPlatformOrder(platform, request.getRawData(), 2);
        log.info("Webhook 签名验证通过并完成订单标准化，platform={}, platformOrderNo={}, orderId={}",
                platform, request.getPlatformOrderNo(), orderId);
    }

    /**
     * 导入平台原始订单报文并标准化为 OMS 订单。
     *
     * @param platform 平台
     * @param rawData  原始报文
     * @return OMS 订单ID
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Long importPlatformOrder(String platform, String rawData) {
        return ingestPlatformOrder(platform, rawData, 1);
    }

    private Long ingestPlatformOrder(String platform, String rawData, Integer syncType) {
        OrderCreateRequest createRequest = orderStandardizationService.standardize(platform, rawData);
        OrderPlatformRaw raw = new OrderPlatformRaw();
        raw.setTenantId(TenantContext.getTenantId());
        raw.setPlatform(platform);
        raw.setPlatformOrderNo(createRequest.getPlatformOrderNo());
        raw.setRawData(rawData);
        raw.setSyncTime(LocalDateTime.now());
        raw.setSyncType(syncType);
        rawMapper.insert(raw);
        Long orderId = create(createRequest);
        raw.setOrderId(orderId);
        rawMapper.updateById(raw);
        return orderId;
    }

    /**
     * WMS 出库完成回调推进订单状态。
     *
     * @param orderNo    订单号
     * @param outboundNo 出库单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outboundCallback(String orderNo, String outboundNo) {
        OrderMain order = orderMapper.selectOne(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getOrderNo, orderNo)
                .last("limit 1"));
        if (order != null && order.getStatus() < OrderConstants.SHIPPED) {
            order.setShipTime(LocalDateTime.now());
            platformInventoryAllocationService.confirmShipment(order, orderItems(order.getId()));
            changeStatus(order, OrderConstants.SHIPPED, "WMS出库回调", "出库单：" + outboundNo);
        }
    }

    /**
     * TMS 物流状态回调推进订单物流状态。
     *
     * @param orderNo         订单号
     * @param waybillNo       TMS内部运单号
     * @param trackingNo      物流商追踪号
     * @param logisticsStatus TMS物流状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logisticsCallback(String orderNo, String waybillNo, String trackingNo, Integer logisticsStatus) {
        OrderMain order = orderMapper.selectOne(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getOrderNo, orderNo)
                .last("limit 1"));
        if (order == null) {
            log.warn("TMS物流回调未找到订单，orderNo={}, waybillNo={}", orderNo, waybillNo);
            return;
        }
        order.setWaybillNo(trackingNo == null ? waybillNo : trackingNo);
        orderMapper.updateById(order);
        Integer targetStatus = mapLogisticsStatus(logisticsStatus);
        if (targetStatus == null || order.getStatus() >= targetStatus) {
            writeLog(order, order.getStatus(), order.getStatus(), "TMS物流回调", "运单：" + waybillNo + "，追踪号：" + trackingNo);
            return;
        }
        advanceLogisticsStatus(order, targetStatus, waybillNo, trackingNo);
    }

    /**
     * 订单概览报表。
     *
     * @return 概览数据
     */
    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrderCount", orderMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("waitingShipCount", orderMapper.selectCount(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getStatus, OrderConstants.WAIT_SHIP)));
        result.put("abnormalCount", orderMapper.selectCount(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getIsAbnormal, 1)));
        return result;
    }

    /**
     * 今日订单报表。
     *
     * @return 今日数据
     */
    @Override
    public Map<String, Object> today() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayOrderCount", orderMapper.selectCount(new LambdaQueryWrapper<OrderMain>().ge(OrderMain::getCreateTime, start)));
        result.put("pendingCount", orderMapper.selectCount(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getStatus, OrderConstants.PENDING)));
        result.put("waitingShipCount", orderMapper.selectCount(new LambdaQueryWrapper<OrderMain>().eq(OrderMain::getStatus, OrderConstants.WAIT_SHIP)));
        return result;
    }

    /**
     * 平台同步日志。
     *
     * @return 同步日志
     */
    @Override
    public List<Map<String, Object>> syncLogs() {
        return rawMapper.selectList(new LambdaQueryWrapper<OrderPlatformRaw>().orderByDesc(OrderPlatformRaw::getSyncTime).last("limit 50"))
                .stream()
                .map(raw -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("platform", raw.getPlatform());
                    item.put("platformOrderNo", raw.getPlatformOrderNo());
                    item.put("syncTime", raw.getSyncTime());
                    return item;
                })
                .toList();
    }

    /**
     * 扫描超期发货风险订单。
     *
     * @return 风险订单数
     */
    @Override
    public int scanDeliveryWarnings() {
        List<OrderMain> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderMain>()
                .eq(OrderMain::getStatus, OrderConstants.WAIT_SHIP)
                .le(OrderMain::getDeliveryDeadline, LocalDate.now().plusDays(2)));
        orders.forEach(order -> log.warn("订单即将超期发货，orderNo={}, deadline={}", order.getOrderNo(), order.getDeliveryDeadline()));
        return orders.size();
    }

    private List<DeductedRedisStock> deductRedisStock(OrderCreateRequest request) {
        List<DeductedRedisStock> deductedStocks = new ArrayList<>();
        Long tenantId = TenantContext.getTenantId();
        for (OrderItemRequest item : request.getItems()) {
            String key = OrderConstants.stockKey(tenantId, item.getSkuId(), request.getWarehouseId());
            String cachedStock = redisTemplate.opsForValue().get(key);
            if (cachedStock == null) {
                continue;
            }
            Long remain = redisTemplate.opsForValue().decrement(key, item.getQuantity());
            if (remain != null && remain < 0) {
                redisTemplate.opsForValue().increment(key, item.getQuantity());
                BusinessException.throwException(15003, "库存不足，订单无法处理");
            }
            deductedStocks.add(new DeductedRedisStock(key, item.getQuantity()));
        }
        return deductedStocks;
    }

    private void rollbackRedisStock(List<DeductedRedisStock> deductedStocks) {
        for (DeductedRedisStock deductedStock : deductedStocks) {
            redisTemplate.opsForValue().increment(deductedStock.key(), deductedStock.quantity());
        }
    }

    private record DeductedRedisStock(String key, Integer quantity) {
    }

    private OrderMain buildOrder(OrderCreateRequest request) {
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())).subtract(item.getDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        OrderMain order = new OrderMain();
        order.setTenantId(TenantContext.getTenantId());
        order.setOrderNo(numberService.nextNo("ORD"));
        order.setPlatform(request.getPlatform());
        order.setPlatformOrderNo(request.getPlatformOrderNo());
        order.setStoreId(request.getStoreId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(request.getDiscountAmount());
        order.setShippingFee(request.getShippingFee());
        order.setPaymentAmount(totalAmount.subtract(request.getDiscountAmount()).add(request.getShippingFee()));
        order.setCurrency(request.getCurrency());
        order.setExchangeRate(request.getExchangeRate());
        order.setCnyAmount(order.getPaymentAmount().multiply(request.getExchangeRate()));
        order.setPlatformFee(BigDecimal.ZERO);
        order.setStatus(OrderConstants.WAIT_SHIP);
        order.setIsAbnormal(0);
        order.setWarehouseId(request.getWarehouseId());
        order.setDeliveryDeadline(request.getDeliveryDeadline());
        order.setPlatformOrderTime(request.getPlatformOrderTime() == null ? LocalDateTime.now() : request.getPlatformOrderTime());
        order.setPlatformPayTime(request.getPlatformPayTime());
        return order;
    }

    private void saveItems(OrderMain order, List<OrderItemRequest> items) {
        for (OrderItemRequest request : items) {
            OrderItem item = new OrderItem();
            item.setTenantId(order.getTenantId());
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setSkuId(request.getSkuId());
            item.setSkuCode(request.getSkuCode());
            item.setSkuName(request.getSkuName());
            item.setPlatformSkuId(request.getPlatformSkuId());
            item.setQuantity(request.getQuantity());
            item.setUnitPrice(request.getUnitPrice());
            item.setDiscount(request.getDiscount());
            item.setAmount(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())).subtract(request.getDiscount()));
            item.setCurrency(order.getCurrency());
            item.setRefundedQty(0);
            itemMapper.insert(item);
        }
    }

    private void saveAddress(Long orderId, OrderCreateRequest request) {
        OrderAddress address = new OrderAddress();
        address.setTenantId(TenantContext.getTenantId());
        address.setOrderId(orderId);
        address.setReceiverName(request.getAddress().getReceiverName());
        address.setPhone(request.getAddress().getPhone());
        address.setEmail(request.getAddress().getEmail());
        address.setCountryCode(request.getAddress().getCountryCode());
        address.setCountryName(request.getAddress().getCountryName());
        address.setState(request.getAddress().getState());
        address.setCity(request.getAddress().getCity());
        address.setAddressLine1(request.getAddress().getAddressLine1());
        address.setAddressLine2(request.getAddress().getAddressLine2());
        address.setZipCode(request.getAddress().getZipCode());
        address.setFullAddress(String.join(" ",
                request.getAddress().getAddressLine1(),
                request.getAddress().getCity() == null ? "" : request.getAddress().getCity(),
                request.getAddress().getCountryCode()));
        address.setIsVerified(0);
        addressMapper.insert(address);
    }

    private void createWmsOutbound(OrderMain order) {
        WarehouseOutboundOrderCreateRequest request = new WarehouseOutboundOrderCreateRequest();
        request.setTenantId(order.getTenantId());
        request.setOutboundType(1);
        request.setWarehouseId(order.getWarehouseId());
        request.setRefType("SALES_ORDER");
        request.setRefId(order.getId());
        request.setRefNo(order.getOrderNo());
        request.setPlanDate(order.getDeliveryDeadline());
        request.setRemark("OMS订单审核生成销售出库单");
        request.setItems(itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()))
                .stream()
                .map(item -> {
                    WarehouseStockItem stockItem = new WarehouseStockItem();
                    stockItem.setSkuId(item.getSkuId());
                    stockItem.setSkuCode(item.getSkuCode());
                    stockItem.setSkuName(item.getSkuName());
                    stockItem.setQuantity(item.getQuantity());
                    return stockItem;
                })
                .toList());
        warehouseFeignClient.createOutboundOrder(request);
    }

    private List<OrderItem> orderItems(Long orderId) {
        return itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    private Integer mapLogisticsStatus(Integer logisticsStatus) {
        if (logisticsStatus == null) {
            return null;
        }
        if (logisticsStatus >= 7) {
            return OrderConstants.SIGNED;
        }
        if (logisticsStatus >= 2) {
            return OrderConstants.IN_TRANSIT;
        }
        if (logisticsStatus >= 1) {
            return OrderConstants.SHIPPED;
        }
        return null;
    }

    private void advanceLogisticsStatus(OrderMain order, Integer targetStatus, String waybillNo, String trackingNo) {
        if (order.getStatus() < OrderConstants.SHIPPED && targetStatus >= OrderConstants.SHIPPED) {
            changeStatus(order, OrderConstants.SHIPPED, "TMS物流回调", "运单：" + waybillNo + "，追踪号：" + trackingNo);
        }
        if (order.getStatus() < OrderConstants.IN_TRANSIT && targetStatus >= OrderConstants.IN_TRANSIT) {
            changeStatus(order, OrderConstants.IN_TRANSIT, "TMS运输中回调", "运单：" + waybillNo + "，追踪号：" + trackingNo);
        }
        if (order.getStatus() < OrderConstants.SIGNED && targetStatus >= OrderConstants.SIGNED) {
            order.setSignedTime(LocalDateTime.now());
            changeStatus(order, OrderConstants.SIGNED, "TMS签收回调", "运单：" + waybillNo + "，追踪号：" + trackingNo);
        }
    }

    private OrderMain copyOrder(OrderMain source) {
        OrderMain target = new OrderMain();
        target.setTenantId(source.getTenantId());
        target.setPlatform(source.getPlatform());
        target.setPlatformOrderNo(source.getPlatformOrderNo() + "-SPLIT-" + System.nanoTime());
        target.setStoreId(source.getStoreId());
        target.setTotalAmount(source.getTotalAmount());
        target.setDiscountAmount(BigDecimal.ZERO);
        target.setShippingFee(source.getShippingFee());
        target.setPaymentAmount(source.getPaymentAmount());
        target.setCurrency(source.getCurrency());
        target.setExchangeRate(source.getExchangeRate());
        target.setCnyAmount(source.getCnyAmount());
        target.setPlatformFee(source.getPlatformFee());
        target.setStatus(source.getStatus());
        target.setIsAbnormal(0);
        target.setWarehouseId(source.getWarehouseId());
        target.setDeliveryDeadline(source.getDeliveryDeadline());
        target.setPlatformOrderTime(source.getPlatformOrderTime());
        return target;
    }

    private void changeStatus(OrderMain order, Integer toStatus, String action, String remark) {
        Integer fromStatus = order.getStatus();
        if (fromStatus != null && !fromStatus.equals(toStatus)
                && !OrderConstants.STATE_WHITE_LIST.getOrDefault(fromStatus, java.util.Set.of()).contains(toStatus)) {
            BusinessException.throwException(15002, "订单状态不允许此操作");
        }
        order.setStatus(toStatus);
        orderMapper.updateById(order);
        writeLog(order, fromStatus, toStatus, action, remark);
    }

    private void writeLog(OrderMain order, Integer fromStatus, Integer toStatus, String action, String remark) {
        OrderLog orderLog = new OrderLog();
        orderLog.setTenantId(order.getTenantId());
        orderLog.setOrderId(order.getId());
        orderLog.setOrderNo(order.getOrderNo());
        orderLog.setFromStatus(fromStatus);
        orderLog.setToStatus(toStatus);
        orderLog.setAction(action);
        orderLog.setOperatorType(1);
        orderLog.setOperatorName("SYSTEM");
        orderLog.setRemark(remark);
        orderLog.setOperateTime(LocalDateTime.now());
        logMapper.insert(orderLog);
    }
}
