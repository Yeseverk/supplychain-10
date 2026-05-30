package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.request.SysUserPageQuery;

import java.util.List;

/**
 * 系统用户服务接口。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysUserService extends IService<SysUser> {

    PageResult<SysUser> pageUsers(SysUserPageQuery query);

    /**
     * 手动解锁用户账号。
     *
     * @param id 用户ID
     */
    void unlock(Long id);

    void updateStatus(Long id, Integer status);

    List<Long> listRoleIds(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);
}
