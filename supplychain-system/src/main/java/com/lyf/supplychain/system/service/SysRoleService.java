package com.lyf.supplychain.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyf.supplychain.common.api.PageResult;
import com.lyf.supplychain.system.entity.SysRole;
import com.lyf.supplychain.system.request.SysRolePageQuery;

import java.util.List;

/**
 * 角色服务接口。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysRoleService extends IService<SysRole> {

    PageResult<SysRole> pageRoles(SysRolePageQuery query);

    /**
     * 查询角色已绑定的菜单ID。
     *
     * @param roleId 角色ID
     * @return 菜单ID集合
     */
    List<Long> listMenuIds(Long roleId);

    /**
     * 批量保存角色菜单授权。
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID集合
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
