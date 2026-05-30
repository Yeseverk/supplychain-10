package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.logistics.entity.LogisticsChannel;
import com.lyf.supplychain.logistics.request.ChannelRequest;
import com.lyf.supplychain.logistics.request.LogisticsPageQuery;
import com.lyf.supplychain.logistics.request.RateRequest;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物流渠道管理控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@RequestMapping({"/api/tms/channels", "/tms/channels"})
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsChannelController {

    private final LogisticsService logisticsService;

    public LogisticsChannelController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 分页查询物流渠道。
     *
     * @param query 分页参数
     * @return 渠道分页结果
     */
    @GetMapping
    public R<PageResult<LogisticsChannel>> page(LogisticsPageQuery query) {
        return R.ok(logisticsService.pageChannels(query));
    }

    /**
     * 创建物流渠道。
     *
     * @param request 渠道请求
     * @return 渠道ID
     */
    @PostMapping
    @TenantWriteGuard(scene = "创建物流渠道")
    public R<Long> create(@Valid @RequestBody ChannelRequest request) {
        return R.ok(logisticsService.createChannel(request));
    }

    /**
     * 停用物流渠道。
     *
     * @param id 渠道ID
     * @return 无数据响应
     */
    @PutMapping("/{id}/disable")
    @TenantWriteGuard(scene = "停用物流渠道")
    public R<Void> disable(@PathVariable("id") Long id) {
        logisticsService.disableChannel(id);
        return R.ok();
    }

    /**
     * 保存物流渠道费率。
     *
     * @param id      渠道ID
     * @param request 费率请求
     * @return 无数据响应
     */
    @PutMapping("/{id}/rates")
    @TenantWriteGuard(scene = "保存物流渠道费率")
    public R<Void> saveRates(@PathVariable("id") Long id, @Valid @RequestBody RateRequest request) {
        logisticsService.saveRates(id, request);
        return R.ok();
    }
}
