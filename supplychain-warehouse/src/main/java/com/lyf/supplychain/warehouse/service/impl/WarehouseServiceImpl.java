package com.lyf.supplychain.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.context.TenantContext;
import com.lyf.supplychain.warehouse.constant.WmsConstants;
import com.lyf.supplychain.warehouse.entity.Warehouse;
import com.lyf.supplychain.warehouse.mapper.WarehouseMapper;
import com.lyf.supplychain.warehouse.request.WarehouseRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;
import com.lyf.supplychain.warehouse.service.WarehouseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 仓库管理服务实现。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    /**
     * 分页查询仓库列表。
     *
     * @param query 分页参数
     * @return 仓库分页结果
     */
    @Override
    public PageResult<Warehouse> pageWarehouses(WmsPageQuery query) {
        query.normalize();
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Warehouse::getWarehouseCode, query.getKeyword())
                    .or().like(Warehouse::getWarehouseName, query.getKeyword())
                    .or().like(Warehouse::getCity, query.getKeyword())
                    .or().like(Warehouse::getRemark, query.getKeyword())
            );
        }
        if (query.getStatus() != null) {
            wrapper.eq(Warehouse::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Warehouse::getCreateTime);
        Page<Warehouse> page = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.from(page);
    }

    /**
     * 新增仓库。
     *
     * @param request 仓库保存请求
     * @return 仓库ID
     */
    @Override
    public Long create(WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(request, warehouse);
        warehouse.setTenantId(TenantContext.getTenantId());
        warehouse.setCountryCode(request.getCountryCode() == null ? "CN" : request.getCountryCode());
        warehouse.setTotalLocations(0);
        warehouse.setUsedLocations(0);
        warehouse.setStatus(request.getStatus() == null ? WmsConstants.ENABLED : request.getStatus());
        warehouse.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
        save(warehouse);
        return warehouse.getId();
    }

    /**
     * 编辑仓库。
     *
     * @param id      仓库ID
     * @param request 仓库保存请求
     */
    @Override
    public void update(Long id, WarehouseRequest request) {
        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(request, warehouse);
        warehouse.setId(id);
        updateById(warehouse);
    }
}
