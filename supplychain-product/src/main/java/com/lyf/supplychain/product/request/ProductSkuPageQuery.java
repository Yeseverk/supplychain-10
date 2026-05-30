package com.lyf.supplychain.product.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU 分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSkuPageQuery extends PageQuery {

    /**
     * 关键词：SKU 编码/名称/条码/FNSKU/规格/备注.
     */
    private String keyword;

    /**
     * 状态：0=草稿 1=正常.
     */
    private Integer status;

    /**
     * SPU ID。
     */
    private Long spuId;
}
