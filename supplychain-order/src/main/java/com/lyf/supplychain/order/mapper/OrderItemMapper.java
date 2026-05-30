package com.lyf.supplychain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
