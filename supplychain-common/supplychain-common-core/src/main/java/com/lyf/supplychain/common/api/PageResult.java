package com.lyf.supplychain.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应体。
 *
 * @param <T> 分页记录类型
 * @author liyunfei
 * @date 2026-05-15
 */
@Data
public class PageResult<T> {

    private Long pageNum;

    private Long pageSize;

    private Long total;

    private Long pages;

    private List<T> records;

    /**
     * 从 MyBatis-Plus 分页对象转换为统一分页响应。
     *
     * @param page 分页对象
     * @param <T>  记录类型
     * @return 统一分页响应
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setRecords(CollUtil.emptyIfNull(page.getRecords()));
        return result;
    }
}
