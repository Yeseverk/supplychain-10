package com.lyf.supplychain.logistics.controller;

import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.security.annotation.TenantWriteGuard;
import com.lyf.supplychain.logistics.entity.LogisticsFeeRecord;
import com.lyf.supplychain.logistics.model.LogisticsBillConfirmResult;
import com.lyf.supplychain.logistics.model.LogisticsBillImportResult;
import com.lyf.supplychain.logistics.request.FeeEstimateRequest;
import com.lyf.supplychain.logistics.service.LogisticsBillService;
import com.lyf.supplychain.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 物流费用控制器。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
@RestController
@com.lyf.supplychain.common.security.annotation.RequiresPermission(com.lyf.supplychain.common.security.constant.PermissionCodes.TMS_LOGISTICS_MANAGE)
public class LogisticsFeeController {

    private final LogisticsService logisticsService;
    private final LogisticsBillService logisticsBillService;

    public LogisticsFeeController(LogisticsService logisticsService,
                                  LogisticsBillService logisticsBillService) {
        this.logisticsService = logisticsService;
        this.logisticsBillService = logisticsBillService;
    }

    /**
     * 预估物流费用。
     *
     * @param request 预估请求
     * @return 费用明细
     */
    @PostMapping({"/api/tms/fee/estimate", "/tms/fee/estimate"})
    @TenantWriteGuard(scene = "预估物流费用")
    public R<Map<String, Object>> estimate(@Valid @RequestBody FeeEstimateRequest request) {
        return R.ok(logisticsService.estimate(request));
    }

    /**
     * 分页查询物流费用记录。
     *
     * @param query 分页参数
     * @return 费用分页结果
     */
    @GetMapping({"/api/tms/fees", "/tms/fees"})
    public R<PageResult<LogisticsFeeRecord>> pageFees(PageQuery query) {
        return R.ok(logisticsService.pageFees(query));
    }

    /**
     * 导入物流商账单。
     *
     * @param file        账单文件
     * @param carrierCode 物流商编码
     * @return 导入结果
     */
    @PostMapping({"/api/tms/fees/import-bill", "/tms/fees/import-bill"})
    @TenantWriteGuard(scene = "导入物流商账单")
    public R<LogisticsBillImportResult> importBill(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("carrierCode") String carrierCode) {
        return R.ok(logisticsBillService.importBill(file, carrierCode));
    }

    /**
     * 确认物流商账单批次并生成财务应付。
     *
     * @param billBatchNo 账单批次号
     * @return 确认结果
     */
    @PostMapping({"/api/tms/fees/bills/{billBatchNo}/confirm", "/tms/fees/bills/{billBatchNo}/confirm"})
    @TenantWriteGuard(scene = "确认物流商账单")
    public R<LogisticsBillConfirmResult> confirmBillBatch(@PathVariable("billBatchNo") String billBatchNo) {
        return R.ok(logisticsBillService.confirmBillBatch(billBatchNo));
    }
}
