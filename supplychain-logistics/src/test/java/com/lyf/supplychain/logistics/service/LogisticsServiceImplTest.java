package com.lyf.supplychain.logistics.service;

import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.common.feign.order.OrderFeignClient;
import com.lyf.supplychain.common.redis.RedisDistributedLockTemplate;
import com.lyf.supplychain.logistics.client.LogisticsCarrierApiClient;
import com.lyf.supplychain.logistics.client.LogisticsCarrierApiClientRegistry;
import com.lyf.supplychain.logistics.config.LogisticsProperties;
import com.lyf.supplychain.logistics.constant.LogisticsConstants;
import com.lyf.supplychain.logistics.entity.LogisticsCarrier;
import com.lyf.supplychain.logistics.entity.LogisticsChannel;
import com.lyf.supplychain.logistics.entity.LogisticsRate;
import com.lyf.supplychain.logistics.entity.LogisticsTrack;
import com.lyf.supplychain.logistics.entity.LogisticsWaybill;
import com.lyf.supplychain.logistics.mapper.LogisticsCarrierMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsChannelMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsFeeRecordMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsRateMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsReturnMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsTrackMapper;
import com.lyf.supplychain.logistics.mapper.LogisticsWaybillMapper;
import com.lyf.supplychain.logistics.model.CarrierTrackEvent;
import com.lyf.supplychain.logistics.model.CarrierTrackPullRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateRequest;
import com.lyf.supplychain.logistics.model.CarrierWaybillCreateResult;
import com.lyf.supplychain.logistics.request.WaybillRequest;
import com.lyf.supplychain.logistics.service.impl.LogisticsServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 物流业务核心流程测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class LogisticsServiceImplTest {

    private final LogisticsCarrier carrier = carrier();
    private final LogisticsChannel channel = channel();
    private final LogisticsRate rate = rate();
    private final List<LogisticsWaybill> insertedWaybills = new ArrayList<>();
    private final List<LogisticsWaybill> updatedWaybills = new ArrayList<>();
    private final List<LogisticsTrack> insertedTracks = new ArrayList<>();
    private final FakeCarrierApiClient carrierApiClient = new FakeCarrierApiClient();
    private LogisticsWaybill existingWaybill;
    private List<LogisticsWaybill> waybillList = List.of();
    private int callbackCount;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createWaybillShouldUseRedisLockAndCarrierAdapter() {
        TenantContext.set(101L, 1001L);
        ThreadPoolTaskExecutor executor = trackExecutor();
        LogisticsServiceImpl service = service(executor);

        Long waybillId = service.createWaybill(waybillRequest());

        assertThat(waybillId).isEqualTo(9001L);
        assertThat(insertedWaybills).hasSize(1);
        assertThat(insertedWaybills.get(0).getTrackingNo()).isEqualTo("DHL-WB001");
        assertThat(insertedWaybills.get(0).getLabelUrl()).contains("DHL-WB001");
        assertThat(carrierApiClient.createCount).isEqualTo(1);
        assertThat(callbackCount).isEqualTo(1);
        executor.shutdown();
    }

    @Test
    void createWaybillShouldReturnExistingWaybillWhenOrderAlreadyCreated() {
        TenantContext.set(101L, 1001L);
        ThreadPoolTaskExecutor executor = trackExecutor();
        LogisticsServiceImpl service = service(executor);
        existingWaybill = waybill(9001L);

        Long waybillId = service.createWaybill(waybillRequest());

        assertThat(waybillId).isEqualTo(9001L);
        assertThat(insertedWaybills).isEmpty();
        assertThat(carrierApiClient.createCount).isZero();
        executor.shutdown();
    }

    @Test
    void pullTracksShouldOnlyProcessCurrentShardWithThreadPool() {
        TenantContext.set(101L, 1001L);
        ThreadPoolTaskExecutor executor = trackExecutor();
        LogisticsServiceImpl service = service(executor);
        waybillList = List.of(waybill(1L), waybill(2L));

        int count = service.pullTracks(0, 2);

        assertThat(count).isEqualTo(1);
        assertThat(insertedTracks).hasSize(1);
        assertThat(updatedWaybills).hasSize(1);
        assertThat(updatedWaybills.get(0).getId()).isEqualTo(2L);
        assertThat(callbackCount).isEqualTo(1);
        executor.shutdown();
    }

    private LogisticsServiceImpl service(ThreadPoolTaskExecutor executor) {
        return new LogisticsServiceImpl(
                mapper(LogisticsCarrierMapper.class, (proxy, method, args) -> selectById(method.getName(), carrier)),
                mapper(LogisticsChannelMapper.class, (proxy, method, args) -> selectById(method.getName(), channel)),
                mapper(LogisticsRateMapper.class, (proxy, method, args) -> selectOne(method.getName(), rate)),
                mapper(LogisticsWaybillMapper.class, this::waybillMapper),
                mapper(LogisticsTrackMapper.class, this::trackMapper),
                mapper(LogisticsFeeRecordMapper.class, (proxy, method, args) -> insert(method.getName(), args)),
                mapper(LogisticsReturnMapper.class, this::defaultValue),
                prefix -> "WB001",
                orderFeignClient(),
                lockTemplate(),
                new LogisticsCarrierApiClientRegistry(List.of(carrierApiClient)),
                serviceProperties(),
                executor
        );
    }

    private Object waybillMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        return switch (method.getName()) {
            case "selectOne" -> existingWaybill;
            case "selectList" -> waybillList;
            case "selectById" -> waybillList.stream().filter(item -> item.getId().equals(args[0])).findFirst().orElse(null);
            case "insert" -> {
                LogisticsWaybill waybill = (LogisticsWaybill) args[0];
                waybill.setId(9001L);
                insertedWaybills.add(waybill);
                yield 1;
            }
            case "updateById" -> {
                updatedWaybills.add((LogisticsWaybill) args[0]);
                yield 1;
            }
            default -> defaultValue(proxy, method, args);
        };
    }

    private Object trackMapper(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if ("insert".equals(method.getName())) {
            insertedTracks.add((LogisticsTrack) args[0]);
            return 1;
        }
        return defaultValue(proxy, method, args);
    }

    private Object selectById(String methodName, Object value) {
        return "selectById".equals(methodName) ? value : null;
    }

    private Object selectOne(String methodName, Object value) {
        return "selectOne".equals(methodName) ? value : null;
    }

    private Object insert(String methodName, Object[] args) {
        return "insert".equals(methodName) ? 1 : null;
    }

    private Object defaultValue(Object proxy, java.lang.reflect.Method method, Object[] args) {
        Class<?> returnType = method.getReturnType();
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        return null;
    }

    private RedisDistributedLockTemplate lockTemplate() {
        return new RedisDistributedLockTemplate(null) {
            @Override
            public <T> T execute(String lockKey, Duration expireTime, Supplier<T> supplier) {
                return supplier.get();
            }
        };
    }

    private OrderFeignClient orderFeignClient() {
        return mapper(OrderFeignClient.class, (proxy, method, args) -> {
            if ("logisticsCallback".equals(method.getName())) {
                callbackCount++;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T mapper(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private LogisticsProperties serviceProperties() {
        LogisticsProperties properties = new LogisticsProperties();
        properties.setWaybillLockTtl(Duration.ofSeconds(30));
        properties.getTrackPull().setBatchSize(200);
        properties.getTrackPull().setTaskTimeout(Duration.ofSeconds(2));
        return properties;
    }

    private ThreadPoolTaskExecutor trackExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.initialize();
        return executor;
    }

    private WaybillRequest waybillRequest() {
        WaybillRequest request = new WaybillRequest();
        request.setChannelId(1L);
        request.setOrderId(10001L);
        request.setOrderNo("SO001");
        request.setWarehouseId(2001L);
        request.setReceiverName("Tom");
        request.setReceiverPhone("13800000000");
        request.setCountryCode("US");
        request.setCity("LA");
        request.setAddressLine1("address");
        request.setZipCode("90001");
        request.setActualWeightG(BigDecimal.valueOf(500));
        request.setDeclaredValue(BigDecimal.valueOf(20));
        request.setDeclaredNameEn("Phone Case");
        return request;
    }

    private LogisticsChannel channel() {
        LogisticsChannel channel = new LogisticsChannel();
        channel.setId(1L);
        channel.setCarrierId(10L);
        channel.setStatus(LogisticsConstants.STATUS_ENABLED);
        channel.setVolumeFactor(5000);
        return channel;
    }

    private LogisticsCarrier carrier() {
        LogisticsCarrier carrier = new LogisticsCarrier();
        carrier.setId(10L);
        carrier.setCarrierCode("DHL");
        carrier.setStatus(LogisticsConstants.STATUS_ENABLED);
        return carrier;
    }

    private LogisticsRate rate() {
        LogisticsRate rate = new LogisticsRate();
        rate.setId(3001L);
        rate.setCurrency("CNY");
        rate.setFirstWeightG(BigDecimal.valueOf(500));
        rate.setFirstWeightPrice(BigDecimal.TEN);
        rate.setExtraWeightG(BigDecimal.valueOf(500));
        rate.setExtraWeightPrice(BigDecimal.valueOf(5));
        rate.setFuelRate(BigDecimal.ZERO);
        rate.setPeakRate(BigDecimal.ZERO);
        rate.setRemoteAreaFee(BigDecimal.ZERO);
        rate.setMinCharge(BigDecimal.ZERO);
        rate.setEffectiveDate(LocalDate.now().minusDays(1));
        return rate;
    }

    private LogisticsWaybill waybill(Long id) {
        LogisticsWaybill waybill = new LogisticsWaybill();
        waybill.setId(id);
        waybill.setTenantId(101L);
        waybill.setWaybillNo("WB00" + id);
        waybill.setTrackingNo("DHL-WB00" + id);
        waybill.setCarrierId(10L);
        waybill.setOrderNo("SO00" + id);
        waybill.setStatus(LogisticsConstants.WAYBILL_WAIT_PICKUP);
        return waybill;
    }

    private static class FakeCarrierApiClient implements LogisticsCarrierApiClient {

        private int createCount;

        @Override
        public boolean supports(String carrierCode) {
            return "DHL".equals(carrierCode);
        }

        @Override
        public CarrierWaybillCreateResult createWaybill(CarrierWaybillCreateRequest request) {
            createCount++;
            CarrierWaybillCreateResult result = new CarrierWaybillCreateResult();
            result.setTrackingNo("DHL-" + request.getWaybillNo());
            result.setLabelUrl("https://labels.example.com/dhl/" + result.getTrackingNo() + ".pdf");
            result.setLabelFormat("PDF");
            return result;
        }

        @Override
        public List<CarrierTrackEvent> pullTracks(CarrierTrackPullRequest request) {
            CarrierTrackEvent event = new CarrierTrackEvent();
            event.setTrackCode("IN_TRANSIT");
            event.setTrackStage(LogisticsConstants.WAYBILL_IN_TRANSIT);
            event.setRawStatus("In transit");
            event.setStatusDesc("运输中");
            event.setTrackTime(LocalDateTime.now());
            event.setException(false);
            return List.of(event);
        }
    }
}
