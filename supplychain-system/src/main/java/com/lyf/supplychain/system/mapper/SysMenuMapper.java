package com.lyf.supplychain.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.system.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单权限 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户拥有的权限标识。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 权限标识集合
     */
    @Select("""
            SELECT DISTINCT m.permission
            FROM sys_user_role ur
                     INNER JOIN sys_role_menu rm ON ur.role_id = rm.role_id
                     INNER JOIN sys_menu m ON rm.menu_id = m.id
            WHERE ur.tenant_id IN (0, #{tenantId})
              AND ur.user_id = #{userId}
              AND m.permission IS NOT NULL
              AND m.permission <> ''
              AND m.status = 1
              AND m.is_deleted = 0
            """)
    List<String> selectPermissionsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
