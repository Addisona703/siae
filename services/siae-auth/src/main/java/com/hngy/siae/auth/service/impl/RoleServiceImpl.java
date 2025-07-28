package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.RoleCreateDTO;
import com.hngy.siae.auth.dto.request.RoleQueryDTO;
import com.hngy.siae.auth.dto.request.RoleUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.dto.response.RoleVO;
import com.hngy.siae.auth.entity.Permission;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.auth.entity.RolePermission;
import com.hngy.siae.auth.entity.UserRole;
import com.hngy.siae.auth.mapper.RoleMapper;

import com.hngy.siae.auth.service.PermissionService;
import com.hngy.siae.auth.service.RolePermissionService;
import com.hngy.siae.auth.service.RoleService;
import com.hngy.siae.auth.service.UserRoleService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl
        extends ServiceImpl<RoleMapper, Role>
        implements RoleService {

    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;
    private final PermissionService permissionService;
    
    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色响应
     * @author KEYKB
     */
    @Override
    public RoleVO createRole(RoleCreateDTO request) {
        AssertUtils.notNull(request, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色编码是否已存在
        boolean codeExists = lambdaQuery()
                .eq(Role::getName, request.getName())
                .eq(Role::getCode, request.getCode())
                .exists();
        AssertUtils.isFalse(codeExists, AuthResultCodeEnum.ROLE_CODE_EXISTS);

        // 创建角色
        Role role = BeanConvertUtil.to(request, Role.class);
        role.setCreatedAt(LocalDateTime.now());

        boolean saved = save(role);
        AssertUtils.isTrue(saved, AuthResultCodeEnum.ROLE_CREATE_FAILED);

        return BeanConvertUtil.to(role, RoleVO.class);
    }

    /**
     * 分页查询角色列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页角色列表
     * @author KEYKB
     */
    @Override
    public PageVO<RoleVO> getRolesPage(PageDTO<RoleQueryDTO> pageDTO) {
        AssertUtils.notNull(pageDTO, CommonResultCodeEnum.VALIDATE_FAILED);

        RoleQueryDTO queryDTO = Optional.ofNullable(pageDTO.getParams()).orElse(new RoleQueryDTO());

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<Role>()
                .like(StrUtil.isNotBlank(queryDTO.getName()), Role::getName, queryDTO.getName())
                .like(StrUtil.isNotBlank(queryDTO.getCode()), Role::getCode, queryDTO.getCode())
                .eq(queryDTO.getStatus() != null, Role::getStatus, queryDTO.getStatus())
                .ge(StrUtil.isNotBlank(queryDTO.getCreatedAtStart()), Role::getCreatedAt, queryDTO.getCreatedAtStart())
                .le(StrUtil.isNotBlank(queryDTO.getCreatedAtEnd()), Role::getCreatedAt, queryDTO.getCreatedAtEnd())
                .orderByDesc(Role::getCreatedAt);

        IPage<Role> page = PageConvertUtil.toPage(pageDTO);
        IPage<Role> pageResult = page(page, queryWrapper);

        return PageConvertUtil.convert(pageResult, RoleVO.class);
    }


    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 是否成功
     * @author KEYKB
     */
    @Override
    public boolean updateRole(Long roleId, RoleUpdateDTO request) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(request, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色是否存在
        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 检查系统内置角色保护
        checkSystemRoleProtection(role, AuthResultCodeEnum.SYSTEM_ROLE_CANNOT_UPDATE);

        // 更新角色信息
        BeanConvertUtil.to(request, role, "id");
        role.setUpdatedAt(LocalDateTime.now());

        return updateById(role);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     * @author KEYKB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色是否存在
        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 检查系统内置角色保护
        checkSystemRoleProtection(role, AuthResultCodeEnum.SYSTEM_ROLE_CANNOT_DELETE);

        // 检查是否有用户关联此角色，TODO: 后续可以实现角色降级
        boolean hasUsers = userRoleService.count(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getRoleId, roleId)
        ) > 0;
        AssertUtils.isFalse(hasUsers, AuthResultCodeEnum.ROLE_HAS_USERS);

        // 删除角色关联的权限
        rolePermissionService.remove(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        // 删除角色
        return removeById(roleId);
    }

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID列表
     * @return 删除结果
     * @author KEYKB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteRoles(List<Long> roleIds) {
        AssertUtils.notEmpty(roleIds, CommonResultCodeEnum.VALIDATE_FAILED);

        // 获取所有角色信息
        List<Role> roles = listByIds(roleIds);
        AssertUtils.isTrue(roles.size() == roleIds.size(), AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 校验每个角色是否可删除
        for (Role role : roles) {
            validateRoleDeletable(role);
        }

        // 校验角色是否被用户使用
        long count = userRoleService.count(
                new LambdaQueryWrapper<UserRole>()
                        .in(UserRole::getRoleId, roleIds)
        );
        AssertUtils.isFalse(count > 0, AuthResultCodeEnum.ROLE_HAS_USERS);

        // 删除角色-权限关联
        rolePermissionService.remove(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds)
        );

        // 删除角色本身
        return removeByIds(roleIds);
    }
    
    /**
     * 角色删除验证
     * <p>
     * 检查角色是否为系统内置角色,如果是则不允许删除
     * TODO: 后续可以在这个方法中添加更多验证
     *
     * @param role 要验证的角色对象
     * @author KEYKB
     */
    private void validateRoleDeletable(Role role) {
        checkSystemRoleProtection(role, AuthResultCodeEnum.SYSTEM_ROLE_CANNOT_DELETE);
    }

    /**
     * 检查系统内置角色保护
     * <p>
     * 验证角色是否为系统内置角色（ROLE_ROOT或ROLE_ADMIN），
     * TODO: 后续可以将常量集中管理系统内置角色
     * private static final Set<String> SYSTEM_ROLE_CODES = Set.of("ROLE_ROOT", "ROLE_ADMIN");
     * 如果是系统内置角色则抛出相应的异常。
     *
     * @param role 要检查的角色对象
     * @param errorCode 当角色为系统内置角色时抛出的错误码
     * @author KEYKB
     */
    private void checkSystemRoleProtection(Role role, AuthResultCodeEnum errorCode) {
        if ("ROLE_ROOT".equals(role.getCode()) || "ROLE_ADMIN".equals(role.getCode())) {
            AssertUtils.fail(errorCode);
        }
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表
     * @author KEYKB
     */
    @Override
    public List<RoleVO> getRoles() {
        List<Role> roles = list();
        return BeanConvertUtil.toList(roles, RoleVO.class);
    }

    /**
     * 获取指定角色详情
     *
     * @param roleId 角色ID
     * @return 角色信息
     * @author KEYKB
     */
    @Override
    public RoleVO getRole(Long roleId) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);

        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        return BeanConvertUtil.to(role, RoleVO.class);
    }
    
    /**
     * 追加角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     * @author KEYKB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(permissionIds, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色是否存在
        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 如果权限ID列表为空，直接返回
        if (permissionIds.isEmpty()) {
            return true;
        }

        // 检查权限是否都存在
        permissionService.validatePermissionsExist(permissionIds);

        // 查询已存在的权限关联
        List<RolePermission> existingRolePermissions = rolePermissionService.list(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
                        .in(RolePermission::getPermissionId, permissionIds)
        );

        // 过滤出需要新增的权限ID
        List<Long> existingPermissionIds = existingRolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .toList();

        List<Long> newPermissionIds = permissionIds.stream()
                .filter(id -> !existingPermissionIds.contains(id))
                .toList();

        // 批量插入新的权限关联
        return batchRolePermissionLink(roleId, newPermissionIds);
    }

    /**
     * 更新角色权限
     * <p>
     * 完全替换角色的权限列表
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 更新结果
     * @author KEYKB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(permissionIds, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色是否存在
        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 检查权限是否都存在
        permissionService.validatePermissionsExist(permissionIds);

        // 删除现有的角色权限关联
        rolePermissionService.remove(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        // 添加新的角色权限关联
        return batchRolePermissionLink(roleId, permissionIds);
    }

    /**
     * 批量创建角色-权限关联
     *
     * @param roleId          角色ID
     * @param newPermissionIds 需要新增的权限ID列表
     * @return 操作是否成功
     */
    private boolean batchRolePermissionLink(Long roleId, List<Long> newPermissionIds) {
        // 如果没有需要新增的权限，直接返回成功
        if (newPermissionIds.isEmpty()) {
            return true;
        }

        try {
            // 构建角色-权限关联对象列表
            List<RolePermission> rolePermissions = newPermissionIds.stream()
                    .map(permissionId -> {
                        RolePermission rolePermission = new RolePermission();
                        rolePermission.setRoleId(roleId);
                        rolePermission.setPermissionId(permissionId);
                        rolePermission.setCreatedAt(LocalDateTime.now());
                        return rolePermission;
                    })
                    .toList();

            // 批量插入角色-权限关联记录
            rolePermissionService.saveBatch(rolePermissions);

            return true;
        } catch (Exception e) {
            log.error("批量创建角色-权限关联失败, roleId={}, permissionIds={}", roleId, newPermissionIds, e);
            return false;
        }
    }



    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     * @author KEYKB
     */
    @Override
    public List<PermissionVO> getPermissionsByRoleId(Long roleId) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查角色是否存在
        Role role = getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 查询角色权限关联
        List<RolePermission> rolePermissions = rolePermissionService.list(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        if (rolePermissions.isEmpty()) {
            return List.of();
        }

        // 提取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 查询权限详情
        List<Permission> permissions = permissionService.listByIds(permissionIds);

        return BeanConvertUtil.toList(permissions, PermissionVO.class);
    }
}