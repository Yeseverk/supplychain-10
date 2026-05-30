package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.logistics.request.RecommendRequest;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 物流渠道推荐控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsRecommendController {

    private final LogisticsService logisticsService;

    public LogisticsRecommendController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    /**
     * 根据包裹、目的国和禁运规则推荐物流渠道。
     *
     * @param request 推荐请求
     * @return 推荐结果
     */
    @PostMapping({"/api/tms/recommend", "/tms/recommend"})
    public R<Map<String, Object>> recommend(@Valid @RequestBody RecommendRequest request) {
        return R.ok(logisticsService.recommend(request));
    }
}
