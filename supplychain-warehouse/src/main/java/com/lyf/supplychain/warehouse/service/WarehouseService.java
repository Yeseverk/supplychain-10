package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.Warehouse;
import com.lyf.supplychain.warehouse.request.WarehouseRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;

/**
 * 仓库管理服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询仓库列表。
     *
     * @param query 分页参数
     * @return 仓库分页结果
     */
    PageResult<Warehouse> pageWarehouses(WmsPageQuery query);

    /**
     * 新增仓库。
     *
     * @param request 仓库保存请求
     * @return 仓库ID
     */
    Long create(WarehouseRequest request);

    /**
     * 编辑仓库。
     *
     * @param id      仓库ID
     * @param request 仓库保存请求
     */
    void update(Long id, WarehouseRequest request);
}
