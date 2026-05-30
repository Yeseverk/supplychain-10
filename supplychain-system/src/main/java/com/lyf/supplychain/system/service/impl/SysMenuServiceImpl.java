package com.lyf.supplychain.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyf.supplychain.system.entity.SysMenu;
import com.lyf.supplychain.system.mapper.SysMenuMapper;
import com.lyf.supplychain.system.service.SysMenuService;
import org.springframework.stereotype.Service;

/**
 * 菜单权限服务实现。
 *
 * @author liyunfei
 * @date 2026-05-15
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
}
