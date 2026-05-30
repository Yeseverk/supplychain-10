package com.lyf.supplychain.product.constant;

/**
 * 商品模块业务常量。
 *
 * @author liyunfei
 * @date 2026-05-20
 */
public final class ProductConstants {

    public static final int SPU_DRAFT = 0;

    public static final int SPU_PENDING_AUDIT = 1;

    public static final int SPU_ON_SALE = 2;

    public static final int SPU_OFF_SALE = 3;

    public static final int SPU_STOPPED = 4;

    public static final int SKU_DRAFT = 0;

    public static final int SKU_ON_SALE = 1;

    public static final int PRICE_SUGGESTED = 2;

    public static final int PRICE_PLATFORM = 3;

    public static final int PRICE_ACTIVITY = 5;

    public static final String PLATFORM_ALL = "ALL";

    public static final String REF_TYPE_SPU = "SPU";

    public static final String LANG_ZH_CN = "zh-CN";

    public static final String LANG_EN_US = "en-US";

    private ProductConstants() {
    }
}
