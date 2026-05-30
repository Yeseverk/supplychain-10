package com.lyf.supplychain.product.request;

import com.lyf.supplychain.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SPU 商品分页查询。
 *
 * @author liyunfei
 * @date 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSpuPageQuery extends PageQuery {

    /**
     * 商品编码、名称、品牌或类目路径关键词。
     */
    private String keyword;

    /**
     * 商品状态。
     */
    private Integer status;
}
