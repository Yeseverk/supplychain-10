package com.lyf.supplychain.order.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import com.lyf.supplychain.order.mapper.PlatformInventoryAllocationMapper;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderItemRequest;
import com.lyf.supplychain.order.service.impl.LoggingPlatformInventorySyncGateway;
import com.lyf.supplychain.order.service.impl.PlatformInventoryAllocationServiceImpl;
import org.apache.ibatis.session.ResultHandler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 平台库存分配服务测试。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
class PlatformInventoryAllocationServiceImplTest {

    @Test
    void freezeForOrderShouldUseCasDeductAllocation() {
        FakeAllocationMapper mapper = new FakeAllocationMapper();
        mapper.updateResult = 1;
        PlatformInventoryAllocationService service = new PlatformInventoryAllocationServiceImpl(
                mapper, new LoggingPlatformInventorySyncGateway());

        service.freezeForOrder(orderRequest());

        assertThat(mapper.updateCount).isEqualTo(1);
    }

    @Test
    void freezeForOrderShouldFailWhenAllocationNotEnough() {
        FakeAllocationMapper mapper = new FakeAllocationMapper();
        mapper.updateResult = 0;
        PlatformInventoryAllocationService service = new PlatformInventoryAllocationServiceImpl(
                mapper, new LoggingPlatformInventorySyncGateway());

        assertThatThrownBy(() -> service.freezeForOrder(orderRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("平台库存配额不足");
    }

    @Test
    void syncShouldCallPlatformGatewayAndUpdateResult() {
        FakeAllocationMapper mapper = new FakeAllocationMapper();
        PlatformInventoryAllocation allocation = new PlatformInventoryAllocation();
        allocation.setId(1L);
        allocation.setPlatform("AMAZON");
        allocation.setSkuId(10L);
        allocation.setAvailableQty(30);
        mapper.selected = allocation;
        PlatformInventoryAllocationService service = new PlatformInventoryAllocationServiceImpl(
                mapper, new LoggingPlatformInventorySyncGateway());

        service.sync(1L);

        assertThat(mapper.updatedById.getLastSyncStatus()).isEqualTo(1);
        assertThat(mapper.updatedById.getLastSyncMessage()).contains("已模拟同步平台库存");
    }

    private OrderCreateRequest orderRequest() {
        OrderItemRequest item = new OrderItemRequest();
        item.setSkuId(10L);
        item.setSkuCode("SKU-10");
        item.setSkuName("测试SKU");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.TEN);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setPlatform("AMAZON");
        request.setPlatformOrderNo("AMZ-001");
        request.setWarehouseId(1L);
        request.setCurrency("USD");
        request.setItems(List.of(item));
        return request;
    }

    private static class FakeAllocationMapper implements PlatformInventoryAllocationMapper {

        private int updateResult;
        private int updateCount;
        private PlatformInventoryAllocation selected;
        private PlatformInventoryAllocation updatedById;

        @Override
        public int insert(PlatformInventoryAllocation entity) {
            return 1;
        }

        @Override
        public int deleteById(PlatformInventoryAllocation entity) {
            return 0;
        }

        @Override
        public int deleteById(Serializable id) {
            return 0;
        }

        @Override
        public int delete(Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return 0;
        }

        @Override
        public int deleteBatchIds(Collection<?> idList) {
            return 0;
        }

        @Override
        public int updateById(PlatformInventoryAllocation entity) {
            this.updatedById = entity;
            return 1;
        }

        @Override
        public int update(PlatformInventoryAllocation entity, Wrapper<PlatformInventoryAllocation> updateWrapper) {
            updateCount++;
            return updateResult;
        }

        @Override
        public PlatformInventoryAllocation selectById(Serializable id) {
            return selected;
        }

        @Override
        public List<PlatformInventoryAllocation> selectBatchIds(Collection<? extends Serializable> idList) {
            return Collections.emptyList();
        }

        @Override
        public void selectBatchIds(Collection<? extends Serializable> idList, ResultHandler<PlatformInventoryAllocation> resultHandler) {
        }

        @Override
        public Long selectCount(Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return 1L;
        }

        @Override
        public List<PlatformInventoryAllocation> selectList(Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return Collections.emptyList();
        }

        @Override
        public void selectList(Wrapper<PlatformInventoryAllocation> queryWrapper, ResultHandler<PlatformInventoryAllocation> resultHandler) {
        }

        @Override
        public List<PlatformInventoryAllocation> selectList(IPage<PlatformInventoryAllocation> page, Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return Collections.emptyList();
        }

        @Override
        public void selectList(IPage<PlatformInventoryAllocation> page, Wrapper<PlatformInventoryAllocation> queryWrapper, ResultHandler<PlatformInventoryAllocation> resultHandler) {
        }

        @Override
        public List<Map<String, Object>> selectMaps(Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return Collections.emptyList();
        }

        @Override
        public void selectMaps(Wrapper<PlatformInventoryAllocation> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        }

        @Override
        public List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return Collections.emptyList();
        }

        @Override
        public void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<PlatformInventoryAllocation> queryWrapper, ResultHandler<Map<String, Object>> resultHandler) {
        }

        @Override
        public <E> List<E> selectObjs(Wrapper<PlatformInventoryAllocation> queryWrapper) {
            return Collections.emptyList();
        }

        @Override
        public <E> void selectObjs(Wrapper<PlatformInventoryAllocation> queryWrapper, ResultHandler<E> resultHandler) {
        }
    }
}
