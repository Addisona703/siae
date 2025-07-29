package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.entity.Permission;
import com.hngy.siae.auth.service.PermissionService;
import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.entity.UserPermission;
import com.hngy.siae.auth.mapper.UserPermissionMapper;
import com.hngy.siae.auth.service.UserPermissionService;
import com.hngy.siae.auth.dto.response.UserPermissionVO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户权限关联服务实现类
 * <p>
 * 提供用户权限的分配、查询和管理功能，
 * 支持分页查询和批量操作。
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl 
        extends ServiceImpl<UserPermissionMapper, UserPermission> 
        implements UserPermissionService {

    private final PermissionService permissionService;

    @Override
    public PageVO<UserPermissionVO> getUserPermissionsByUserId(Long userId, PageDTO<Object> pageDTO) {
        // 参数校验
        AssertUtils.notNull(userId, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(pageDTO, CommonResultCodeEnum.VALIDATE_FAILED);

        // 创建分页对象并查询
        Page<UserPermission> page = PageConvertUtil.toPage(pageDTO);
        LambdaQueryWrapper<UserPermission> queryWrapper = Wrappers.lambdaQuery(UserPermission.class)
                .eq(UserPermission::getUserId, userId)
                .orderByDesc(UserPermission::getCreatedAt);

        IPage<UserPermission> userPermissionPage = page(page, queryWrapper);

        // 判空返回
        List<UserPermission> records = userPermissionPage.getRecords();
        if (records.isEmpty()) {
            return PageConvertUtil.empty(pageDTO);
        }

        // 查询权限信息
        List<Long> permissionIds = records.stream()
                .map(UserPermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Permission> permissionMap = permissionService.listByIds(permissionIds).stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));

        // 封装 VO
        List<UserPermissionVO> voList = records.stream().map(record -> {
            Permission permission = permissionMap.get(record.getPermissionId());
            return UserPermissionVO.builder()
                    .id(record.getId())
                    .userId(record.getUserId())
                    .permissionId(record.getPermissionId())
                    .permissionName(permission != null ? permission.getName() : null)
                    .permissionCode(permission != null ? permission.getCode() : null)
                    .createdAt(record.getCreatedAt())
                    .build();
        }).toList();

        return PageConvertUtil.build(userPermissionPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignPermissionsToUser(UserPermissionDTO dto) {
        // 参数校验
        AssertUtils.notNull(dto, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(dto.getUserId(), CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notEmpty(dto.getPermissionIds(), CommonResultCodeEnum.VALIDATE_FAILED);

        // 验证权限是否存在
        validatePermissionsExist(dto.getPermissionIds());

        // 删除旧的权限记录
        remove(new LambdaQueryWrapper<UserPermission>().eq(UserPermission::getUserId, dto.getUserId()));

        // 批量插入新权限
        LocalDateTime now = LocalDateTime.now();
        List<UserPermission> userPermissions = dto.getPermissionIds().stream()
                .map(pid -> UserPermission.builder()
                        .userId(dto.getUserId())
                        .permissionId(pid)
                        .createdAt(now)
                        .build())
                .toList();

        boolean saved = saveBatch(userPermissions);
        AssertUtils.isTrue(saved, CommonResultCodeEnum.DB_SAVE_FAILED);

        log.info("用户权限分配成功，用户ID: {}, 权限数量: {}", dto.getUserId(), dto.getPermissionIds().size());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean appendPermissionsToUser(UserPermissionDTO dto) {
        // 参数校验
        AssertUtils.notNull(dto, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(dto.getUserId(), CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notEmpty(dto.getPermissionIds(), CommonResultCodeEnum.VALIDATE_FAILED);

        // 验证权限是否存在
        validatePermissionsExist(dto.getPermissionIds());

        // 查询用户已有的权限 ID，使用 Set 提高过滤性能
        Set<Long> existingPermissionIds = this.list(new LambdaQueryWrapper<UserPermission>()
                        .eq(UserPermission::getUserId, dto.getUserId())
                        .select(UserPermission::getPermissionId))
                .stream()
                .map(UserPermission::getPermissionId)
                .collect(Collectors.toSet());

        // 过滤出新权限 ID（去重）
        Set<Long> newPermissionIds = dto.getPermissionIds().stream()
                .filter(pid -> !existingPermissionIds.contains(pid))
                .collect(Collectors.toSet());

        // 无需追加
        if (newPermissionIds.isEmpty()) {
            log.info("用户已拥有所有指定权限，无需追加，用户ID: {}", dto.getUserId());
            return true;
        }

        // 构造新增权限实体列表
        LocalDateTime now = LocalDateTime.now();
        List<UserPermission> userPermissions = newPermissionIds.stream()
                .map(pid -> UserPermission.builder()
                        .userId(dto.getUserId())
                        .permissionId(pid)
                        .createdAt(now)
                        .build())
                .collect(Collectors.toList());

        boolean saved = saveBatch(userPermissions);
        AssertUtils.isTrue(saved, CommonResultCodeEnum.DB_SAVE_FAILED);

        log.info("用户权限追加成功，用户ID: {}, 新增权限数量: {}", dto.getUserId(), newPermissionIds.size());
        return true;
    }

    /**
     * 验证权限是否存在
     *
     * @param permissionIds 权限ID列表
     */
    private void validatePermissionsExist(List<Long> permissionIds) {
        permissionService.validatePermissionsExist(permissionIds);
    }

    @Override
    public Boolean removeAllPermissionsFromUser(Long userId) {
        AssertUtils.notNull(userId, CommonResultCodeEnum.VALIDATE_FAILED);

        boolean removed = remove(Wrappers.<UserPermission>lambdaQuery()
                .eq(UserPermission::getUserId, userId));

        if (removed) {
            log.info("用户所有权限移除成功，用户ID: {}", userId);
        } else {
            log.warn("用户权限移除失败或无权限记录，用户ID: {}", userId);
        }

        return removed;
    }

    @Override
    public Boolean removePermissionsFromUser(UserPermissionDTO dto) {
        AssertUtils.notNull(dto, CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notNull(dto.getUserId(), CommonResultCodeEnum.VALIDATE_FAILED);
        AssertUtils.notEmpty(dto.getPermissionIds(), CommonResultCodeEnum.VALIDATE_FAILED);

        boolean removed = remove(Wrappers.<UserPermission>lambdaQuery()
                .eq(UserPermission::getUserId, dto.getUserId())
                .in(UserPermission::getPermissionId, dto.getPermissionIds()));

        if (removed) {
            log.info("用户指定权限移除成功，用户ID: {}, 移除权限数量: {}", dto.getUserId(), dto.getPermissionIds().size());
        } else {
            log.warn("用户指定权限移除失败，用户ID: {}, 移除权限数量: {}", dto.getUserId(), dto.getPermissionIds().size());
        }

        return removed;
    }
}