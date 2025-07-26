package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.auth.dto.request.RoleCreateDTO;
import com.hngy.siae.auth.dto.response.RoleVO;
import com.hngy.siae.auth.dto.request.RoleUpdateDTO;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.auth.entity.RolePermission;
import com.hngy.siae.auth.entity.UserRole;
import com.hngy.siae.auth.mapper.RoleMapper;
import com.hngy.siae.auth.mapper.RolePermissionMapper;
import com.hngy.siae.auth.mapper.UserRoleMapper;
import com.hngy.siae.auth.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 
 * @author KEYKB
 */
@Service
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    
    /**
     * 构造函数
     *
     * @param roleMapper          角色Mapper
     * @param rolePermissionMapper 角色权限Mapper
     * @param userRoleMapper       用户角色Mapper
     */
    public RoleServiceImpl(RoleMapper roleMapper, RolePermissionMapper rolePermissionMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
    }
    
    @Override
    public RoleVO createRole(RoleCreateDTO request) {
        // 检查角色编码是否已存在
        Role existingRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, request.getCode())
        );
        
        if (existingRole != null) {
            throw new ServiceException("角色编码已存在");
        }
        
        // 创建角色
        Role role = new Role();
        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());
        role.setStatus(1);  // 默认启用
        role.setCreatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        
        // 构建响应
        return convertToRoleResponse(role);
    }
    
    @Override
    public boolean updateRole(Long roleId, RoleUpdateDTO request) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        
        // 更新角色信息
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setUpdatedAt(LocalDateTime.now());
        
        return roleMapper.updateById(role) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteRole(Long roleId) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        
        // 检查特殊角色是否可删除
        if ("ROLE_ROOT".equals(role.getCode()) || "ROLE_ADMIN".equals(role.getCode())) {
            throw new ServiceException("系统内置角色不可删除");
        }
        
        // 删除角色关联的权限
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );
        
        // 删除角色关联的用户
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getRoleId, roleId)
        );
        
        // 删除角色
        return roleMapper.deleteById(roleId) > 0;
    }
    
    @Override
    public List<RoleVO> getRoles() {
        List<Role> roles = roleMapper.selectList(null);
        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public RoleVO getRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return convertToRoleResponse(role);
    }
    
    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        
        // 删除现有的角色权限关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );
        
        // 如果权限ID列表为空，则仅删除现有关联
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        
        // 创建新的角色权限关联
        List<RolePermission> rolePermissions = new ArrayList<>();
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedAt(LocalDateTime.now());
            rolePermissions.add(rolePermission);
        }
        
        // 批量插入新的关联
        for (RolePermission rolePermission : rolePermissions) {
            rolePermissionMapper.insert(rolePermission);
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean assignUserRoles(Long userId, List<Long> roleIds) {
        // 删除现有的用户角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );
        
        // 如果角色ID列表为空，则仅删除现有关联
        if (roleIds == null || roleIds.isEmpty()) {
            return true;
        }
        
        // 创建新的用户角色关联
        List<UserRole> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(LocalDateTime.now());
            userRoles.add(userRole);
        }
        
        // 批量插入新的关联
        for (UserRole userRole : userRoles) {
            userRoleMapper.insert(userRole);
        }
        
        return true;
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(Long roleId) {
        return List.of();
    }

    /**
     * 将角色实体转换为角色响应
     *
     * @param role 角色实体
     * @return 角色响应
     */
    private RoleVO convertToRoleResponse(Role role) {
        RoleVO response = new RoleVO();
        response.setRoleId(role.getId());
        response.setName(role.getName());
        response.setCode(role.getCode());
        response.setDescription(role.getDescription());
        response.setStatus(role.getStatus());
        return response;
    }
} 