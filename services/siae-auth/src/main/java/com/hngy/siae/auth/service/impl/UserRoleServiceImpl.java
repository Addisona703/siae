package com.hngy.siae.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.UserRoleQueryDTO;
import com.hngy.siae.auth.dto.request.UserRoleUpdateDTO;
import com.hngy.siae.auth.dto.response.UserRoleVO;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.auth.entity.UserRole;

import com.hngy.siae.auth.feign.UserClient;
import com.hngy.siae.auth.mapper.UserRoleMapper;
import com.hngy.siae.auth.service.RoleService;
import com.hngy.siae.auth.service.UserRoleService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import cn.hutool.core.util.StrUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色关联服务实现类
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl
        extends ServiceImpl<UserRoleMapper, UserRole>
        implements UserRoleService {

    private final RoleService roleService;
    private final UserClient userClient;

    /**
     * 批量分配角色给用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchAssignRoleToUsers(Long roleId, List<Long> userIds) {
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(userIds, CommonResultCodeEnum.VALIDATE_FAILED);

        // 如果用户ID列表为空，直接返回
        if (userIds.isEmpty()) {
            return true;
        }

        // 检查角色是否存在
        Role role = roleService.getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 检查用户是否都存在
        for (Long userId : userIds) {
            Boolean userExists = userClient.checkUserExists(userId);
            AssertUtils.isTrue(userExists, AuthResultCodeEnum.USER_NOT_FOUND);
        }

        // 删除现有的用户角色关联
        remove(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, roleId)
                .in(UserRole::getUserId, userIds));

        // 创建新的用户角色关联
        LocalDateTime now = LocalDateTime.now();
        List<UserRole> userRoles = userIds.stream()
                .map(userId -> {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setCreatedAt(now);
                    return userRole;
                })
                .toList();

        return saveBatch(userRoles);
    }

    /**
     * 分页查询用户角色关联列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageVO<UserRoleVO> getUserRoleList(PageDTO<UserRoleQueryDTO> pageDTO) {
        AssertUtils.notNull(pageDTO, CommonResultCodeEnum.VALIDATE_FAILED);

        UserRoleQueryDTO queryDTO = Optional.ofNullable(pageDTO.getParams()).orElse(new UserRoleQueryDTO());

        IPage<UserRole> page = PageConvertUtil.toPage(pageDTO);

        // 使用链式调用优化查询条件构建，添加roleId筛选逻辑
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<UserRole>()
                .eq(queryDTO.getRoleId() != null, UserRole::getRoleId, queryDTO.getRoleId())
                .ge(StrUtil.isNotBlank(queryDTO.getCreatedAtStart()), UserRole::getCreatedAt, queryDTO.getCreatedAtStart())
                .le(StrUtil.isNotBlank(queryDTO.getCreatedAtEnd()), UserRole::getCreatedAt, queryDTO.getCreatedAtEnd())
                .orderByDesc(UserRole::getCreatedAt);

        IPage<UserRole> userRolePage = page(page, queryWrapper);
        List<UserRole> userRoles = userRolePage.getRecords();

        // 如果没有查到任何数据，直接返回空结果
        if (CollectionUtil.isEmpty(userRoles)) {
            return PageConvertUtil.empty(pageDTO);
        }

        // 批量查询 userIds 和 roleIds，保持性能优化
        Set<Long> userIds = userRoles.stream().map(UserRole::getUserId).collect(Collectors.toSet());
        Set<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());

        // ① 通过 UserClient 批量查询用户名（避免N+1查询问题）
        Map<Long, String> userMap = userClient.getUserMapByIds(userIds);

        // ② 批量查询角色信息（避免N+1查询问题）
        List<Role> roles = roleService.listByIds(roleIds);
        Map<Long, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, role -> role));

        // ③ 批量转换 VO，使用预先查询的数据
        List<UserRoleVO> voList = userRoles.stream().map(ur -> {
            UserRoleVO vo = new UserRoleVO();
            vo.setId(ur.getId());
            vo.setUserId(ur.getUserId());
            vo.setRoleId(ur.getRoleId());
            vo.setCreatedAt(ur.getCreatedAt());

            // 设置用户名（使用批量查询结果）
            vo.setUsername(userMap.getOrDefault(ur.getUserId(), "未知用户"));

            // 设置角色信息（使用批量查询结果）
            Role role = roleMap.get(ur.getRoleId());
            if (role != null) {
                vo.setRoleName(role.getName());
                vo.setRoleCode(role.getCode());
            }

            return vo;
        }).toList();

        return PageConvertUtil.build(userRolePage, voList);
    }


    /**
     * 更新用户角色关联
     *
     * @param userRoleId 用户角色关联ID
     * @param updateDTO  更新参数
     * @return 是否成功
     */
    @Override
    public Boolean updateUserRole(Long userRoleId, UserRoleUpdateDTO updateDTO) {
        AssertUtils.notNull(userRoleId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(updateDTO, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查用户角色关联是否存在
        UserRole userRole = getById(userRoleId);
        AssertUtils.notNull(userRole, "用户角色关联不存在");

        // 检查角色是否存在
        Role role = roleService.getById(updateDTO.getRoleId());
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 检查用户是否存在
        Boolean userExists = userClient.checkUserExists(updateDTO.getUserId());
        AssertUtils.isTrue(userExists, AuthResultCodeEnum.USER_NOT_FOUND);

        // 更新用户角色关联
        userRole.setUserId(updateDTO.getUserId());
        userRole.setRoleId(updateDTO.getRoleId());

        return updateById(userRole);
    }

    /**
     * 为用户分配单个角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    @Override
    public Boolean assignUserRole(Long userId, Long roleId) {
        AssertUtils.notNull(userId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(roleId, CommonResultCodeEnum.VALIDATE_FAILED);

        // 检查用户是否存在
        Boolean userExists = userClient.checkUserExists(userId);
        AssertUtils.isTrue(userExists, AuthResultCodeEnum.USER_NOT_FOUND);

        // 检查角色是否存在
        Role role = roleService.getById(roleId);
        AssertUtils.notNull(role, AuthResultCodeEnum.ROLE_NOT_FOUND);

        // 删除用户现有的角色关联
        remove(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        // 创建新的用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(LocalDateTime.now());

        return save(userRole);
    }
}
