package com.lyf.supplychain.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lyf.supplychain.common.context.TenantContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充处理器。
 * 当我们执行了 insert操作或者update操作时
 * 我们的实体中 不用去配置那些通用的字段 这个类会字段进行填充
 * 但是我们要在实体类上添加相关的注解 填充的操作类型
 * @author liyunfei
 * @date 2026-05-15
 */
@Component
public class SupplychainMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增数据时填充创建和更新字段。
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // metaObject 传递给MyBatis-Plus的实体类
        // insert 操作 传递一个实体类对象 这个实体类对象 就是 metaObject
        // 先获取 外面是否有指定的值 如果有 就可以使用外面指定的值 如果没有 我们可以自动填充
        // metaObject.getValue("createTime");

        LocalDateTime now = LocalDateTime.now();

        // 创建人 也就是 当前登录的用户的ID
        Long userId = TenantContext.getUserId();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", Long.class, userId);
        strictInsertFill(metaObject, "updateBy", Long.class, userId);
    }

    /**
     * 更新数据时填充更新时间和更新人。
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人也是当前登录的用户ID
        strictUpdateFill(metaObject, "updateBy", Long.class, TenantContext.getUserId());
    }
}
