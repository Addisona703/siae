package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.entity.RolePermission;
import com.hngy.siae.auth.mapper.RolePermissionMapper;
import com.hngy.siae.auth.service.RolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色权限关联服务实现类
 * <p>
 * 提供角色权限关联关系的基础管理功能。
 *
 * @author KEYKB
 */
@Slf4j
@Service
public class RolePermissionServiceImpl
        extends ServiceImpl<RolePermissionMapper, RolePermission>
        implements RolePermissionService {
}
