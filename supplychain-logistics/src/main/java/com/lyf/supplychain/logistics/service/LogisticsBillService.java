package com.lyf.supplychain.logistics.service;

import com.lyf.supplychain.logistics.model.LogisticsBillImportResult;
import com.lyf.supplychain.logistics.model.LogisticsBillConfirmResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物流商账单导入与对账服务。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
public interface LogisticsBillService {

    /**
     * 导入物流商账单并与预估运费自动对账。
     *
     * @param file        账单文件
     * @param carrierCode 物流商编码
     * @return 导入结果
     */
    LogisticsBillImportResult importBill(MultipartFile file, String carrierCode);

    /**
     * 确认物流账单批次并推送财务生成应付账款。
     *
     * @param billBatchNo 账单批次号
     * @return 确认结果
     */
    LogisticsBillConfirmResult confirmBillBatch(String billBatchNo);
}
