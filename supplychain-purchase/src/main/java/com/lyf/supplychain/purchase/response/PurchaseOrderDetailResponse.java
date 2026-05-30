package com.lyf.supplychain.purchase.response;

import com.lyf.supplychain.purchase.entity.PurchaseOrder;
import com.lyf.supplychain.purchase.entity.PurchaseOrderItem;
import com.lyf.supplychain.purchase.entity.PurchaseReceipt;
import lombok.Data;

import java.util.List;

/**
 * Purchase order detail view for seller workspace.
 */
@Data
public class PurchaseOrderDetailResponse {

    private PurchaseOrder order;
    private List<PurchaseOrderItem> items;
    private List<PurchaseReceipt> receipts;
}
