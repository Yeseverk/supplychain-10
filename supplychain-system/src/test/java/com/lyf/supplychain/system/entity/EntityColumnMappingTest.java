package com.lyf.supplychain.system.entity;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 实体列映射测试。
 *
 * @author liyunfei
 * @date 2026-05-16
 */
class EntityColumnMappingTest {

    @Test
    void dictItemShouldNotMapColumnsMissingFromTable() {
        assertThat(mappedColumns(SysDictItem.class))
                .doesNotContain("tenant_id", "create_by", "update_by", "version")
                .contains("create_time", "update_time", "is_deleted");
    }

    @Test
    void dictTypeShouldNotMapColumnsMissingFromTable() {
        assertThat(mappedColumns(SysDictType.class))
                .doesNotContain("tenant_id", "version")
                .contains("create_by", "update_by", "is_deleted");
    }

    @Test
    void menuShouldNotMapColumnsMissingFromTable() {
        assertThat(mappedColumns(SysMenu.class))
                .doesNotContain("tenant_id", "version")
                .contains("create_by", "update_by", "is_deleted");
    }

    @Test
    void tenantShouldNotMapColumnsMissingFromTable() {
        assertThat(mappedColumns(SysTenant.class))
                .doesNotContain("tenant_id")
                .contains("create_by", "update_by", "version", "is_deleted");
    }

    private Set<String> mappedColumns(Class<?> entityType) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), entityType.getName()), entityType);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityType);
        return tableInfo.getFieldList().stream()
                .map(TableFieldInfo::getColumn)
                .collect(Collectors.toSet());
    }
}
