package com.lyf.supplychain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.order.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款单 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {
}
