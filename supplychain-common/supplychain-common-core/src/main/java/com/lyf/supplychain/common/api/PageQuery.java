package com.lyf.supplychain.common.api;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

/**
 * 通用分页查询参数。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
public class PageQuery {

    private static final long DEFAULT_PAGE_NUM = 1L;

    private static final long DEFAULT_PAGE_SIZE = 10L;

    private static final long MAX_PAGE_SIZE = 100L;

    private Long pageNum = DEFAULT_PAGE_NUM;

    private Long pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 规范化分页参数，避免非法页码或过大的分页查询。
     */
    public void normalize() {
        if (ObjectUtil.isNull(pageNum) || pageNum < 1) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        if (ObjectUtil.isNull(pageSize) || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
    }
}
