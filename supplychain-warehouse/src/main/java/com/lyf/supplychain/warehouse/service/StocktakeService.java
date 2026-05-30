package com.lyf.supplychain.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.warehouse.entity.StocktakeItem;
import com.lyf.supplychain.warehouse.entity.StocktakeTask;
import com.lyf.supplychain.warehouse.request.StocktakeAuditRequest;
import com.lyf.supplychain.warehouse.request.StocktakeCountRequest;
import com.lyf.supplychain.warehouse.request.StocktakeTaskRequest;
import com.lyf.supplychain.warehouse.request.WmsPageQuery;

/**
 * 库存盘点服务。
 *
 * @author liyunfei
 * @date 2026-05-19
 */
public interface StocktakeService extends IService<StocktakeTask> {

    /**
     * 分页查询盘点任务。
     *
     * @param query 分页参数
     * @return 盘点任务分页结果
     */
    PageResult<StocktakeTask> pageStocktake(WmsPageQuery query);

    /**
     * 创建盘点任务并生成账面快照。
     *
     * @param request 创建请求
     * @return 盘点任务ID
     */
    Long create(StocktakeTaskRequest request);

    /**
     * 查询盘点任务明细。
     *
     * @param id 盘点任务ID
     * @return 盘点明细列表
     */
    java.util.List<StocktakeItem> items(Long id);

    /**
     * 提交实盘数据。
     *
     * @param id      盘点任务ID
     * @param request 实盘请求
     */
    void count(Long id, StocktakeCountRequest request);

    /**
     * 审核盘点差异并执行库存调整。
     *
     * @param id      盘点任务ID
     * @param request 审核请求
     */
    void audit(Long id, StocktakeAuditRequest request);
}
