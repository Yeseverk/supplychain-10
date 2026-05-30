package com.lyf.supplychain.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyf.supplychain.system.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色编码。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 角色编码集合
     */
    @Select("""
            SELECT DISTINCT r.role_code
            FROM sys_user_role ur
                     INNER JOIN sys_role r ON ur.role_id = r.id
            WHERE ur.tenant_id IN (0, #{tenantId})
              AND ur.user_id = #{userId}
              AND r.status = 1
              AND r.is_deleted = 0
            """)
    List<String> selectRoleCodesByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * 查询用户角色中最宽的数据权限范围。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 数据权限范围
     */
    @Select("""
            SELECT MIN(r.data_scope)
            FROM sys_user_role ur
                     INNER JOIN sys_role r ON ur.role_id = r.id
            WHERE ur.tenant_id IN (0, #{tenantId})
              AND ur.user_id = #{userId}
              AND r.status = 1
              AND r.is_deleted = 0
            """)
    Integer selectDataScopeByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
