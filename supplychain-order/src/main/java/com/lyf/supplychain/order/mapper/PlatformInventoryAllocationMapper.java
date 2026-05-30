package com.lyf.supplychain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.order.entity.PlatformInventoryAllocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 多平台库存分配 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Mapper
public interface PlatformInventoryAllocationMapper extends BaseMapper<PlatformInventoryAllocation> {
}
