package com.lyf.supplychain.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.hutool.core.util.ObjectUtil;
import com.lyf.supplychain.common.api.PageQuery;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.common.api.R;
import com.lyf.supplychain.common.constant.ResultCode;
import com.lyf.supplychain.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通用 CRUD 控制器基类。
 *
 * @param <T> 实体类型
 * @author liyunfei
 * @date 2026-05-15
 */
public abstract class BaseCrudController<T> {

    private final IService<T> service;

    protected BaseCrudController(IService<T> service) {
        this.service = service;
    }

    /**
     * 根据主键查询详情。
     *
     * @param id 主键ID
     * @return 详情响应
     */
    @GetMapping("/{id}")
    public R<T> getById(@PathVariable("id") Long id) {
        validateId(id);
        T entity = service.getById(id);
        if (ObjectUtil.isNull(entity)) {
            BusinessException.throwException(ResultCode.DATA_NOT_FOUND);
        }
        return R.ok(entity);
    }

    /**
     * 分页查询列表。
     *
     * @param pageQuery 分页参数
     * @return 分页响应
     */
    @GetMapping("/page")
    public R<PageResult<T>> page(PageQuery pageQuery) {
        // 参数校验 如果传递的参数不合法 会给默认值
        pageQuery.normalize();
        Page<T> page = service.page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()));
        return R.ok(PageResult.from(page));
    }

    /**
     * 新增数据。
     *
     * @param entity 请求实体
     * @return 新增数据主键
     */
    @PostMapping
    public R<Object> create(@Valid @RequestBody T entity) {
        boolean success = service.save(entity);
        if (!success) {
            BusinessException.throwException("新增数据失败");
        }
        return R.ok(readId(entity));
    }

    /**
     * 更新数据。
     *
     * @param id     主键ID
     * @param entity 请求实体
     * @return 无数据响应
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody T entity) {
        validateId(id);
        // 前端传递过来的参数类型 应该是有 我们来定义的
        // 把ID和实体分别传输 ID放到请求行上 实体放到请求体中
        writeId(entity, id);
        boolean success = service.updateById(entity);
        if (!success) {
            BusinessException.throwException(ResultCode.DATA_VERSION_CONFLICT);
        }
        return R.ok();
    }

    /**
     * 删除数据。
     *
     * @param id 主键ID
     * @return 无数据响应
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        validateId(id);
        boolean success = service.removeById(id);
        if (!success) {
            BusinessException.throwException(ResultCode.DATA_NOT_FOUND);
        }
        return R.ok();
    }

    private Object readId(T entity) {
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        return wrapper.isReadableProperty("id") ? wrapper.getPropertyValue("id") : null;
    }

    private void validateId(Long id) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            BusinessException.throwException(ResultCode.PARAM_ERROR.getCode(), "主键ID必须大于0");
        }
    }

    private void writeId(T entity, Long id) {
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        if (wrapper.isWritableProperty("id")) {
            wrapper.setPropertyValue("id", id);
        }
    }
}
