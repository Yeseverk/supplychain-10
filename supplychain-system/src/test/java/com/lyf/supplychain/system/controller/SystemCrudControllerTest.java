package com.lyf.supplychain.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyf.supplychain.common.handler.GlobalExceptionHandler;
import com.lyf.supplychain.system.entity.SysDictType;
import com.lyf.supplychain.system.entity.SysUser;
import com.lyf.supplychain.system.service.SysDictTypeService;
import com.lyf.supplychain.system.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * 系统管理 CRUD 接口合约测试。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@ExtendWith(MockitoExtension.class)
class SystemCrudControllerTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysDictTypeService sysDictTypeService;

    private MockMvc userMockMvc;

    private MockMvc dictTypeMockMvc;

    @BeforeEach
    void setUp() {
        userMockMvc = standaloneSetup(new SysUserController(sysUserService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        dictTypeMockMvc = standaloneSetup(new SysDictTypeController(sysDictTypeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUserShouldReturnUnifiedResponse() throws Exception {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("tenant_admin");
        user.setRealName("租户管理员");
        when(sysUserService.getById(1L)).thenReturn(user);

        userMockMvc.perform(get("/system/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.username").value("tenant_admin"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    void getUserShouldRejectInvalidId() throws Exception {
        userMockMvc.perform(get("/system/users/{id}", 0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("主键ID必须大于0"));

        verifyNoInteractions(sysUserService);
    }

    @Test
    void pageDictTypeShouldReturnPageResult() throws Exception {
        SysDictType dictType = new SysDictType();
        dictType.setId(1L);
        dictType.setDictName("供应商类型");
        dictType.setDictCode("supplier_type");
        Page<SysDictType> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dictType));
        when(sysDictTypeService.page(any(Page.class))).thenReturn(page);

        dictTypeMockMvc.perform(get("/system/dict-types/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].dictCode").value("supplier_type"));
    }

    @Test
    void deleteUserShouldRejectInvalidId() throws Exception {
        userMockMvc.perform(delete("/system/users/{id}", 0L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("主键ID必须大于0"));

        verifyNoInteractions(sysUserService);
    }
}
