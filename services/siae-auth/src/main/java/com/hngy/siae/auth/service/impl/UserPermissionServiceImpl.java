package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.mapper.PermissionMapper;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.entity.UserPermission;
import com.hngy.siae.auth.mapper.UserPermissionMapper;
import com.hngy.siae.auth.service.UserPermissionService;
import com.hngy.siae.auth.dto.response.UserPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户权限关联服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl extends ServiceImpl<UserPermissionMapper, UserPermission> implements UserPermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<UserPermissionVO> getUserPermissionsByUserId(Long userId) {
//        if (userId == null) {
//            return Collections.emptyList();
//        }
//
//        // 查询用户权限关联列表
//        LambdaQueryWrapper<UserPermission> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(UserPermission::getUserId, userId);
//        List<UserPermission> userPermissions = this.list(queryWrapper);
//
//        if (userPermissions.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // 获取权限ID列表
//        List<Long> permissionIds = userPermissions.stream()
//                .map(UserPermission::getPermissionId)
//                .collect(Collectors.toList());
//
//        // 查询权限详情
//        List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
//        Map<Long, Permission> permissionMap = permissions.stream()
//                .collect(Collectors.toMap(Permission::getId, permission -> permission));
//
//        // 组装VO列表
//        return userPermissions.stream().map(userPermission -> {
//            Permission permission = permissionMap.get(userPermission.getPermissionId());
//            return UserPermissionVO.builder()
//                    .id(userPermission.getId())
//                    .userId(userPermission.getUserId())
//                    .permissionId(userPermission.getPermissionId())
//                    .permissionName(permission != null ? permission.getName() : null)
//                    .permissionCode(permission != null ? permission.getCode() : null)
//                    .createdAt(userPermission.getCreatedAt())
//                    .build();
//        }).collect(Collectors.toList());
        return null;
    }

    @Override
    public List<Long> getPermissionIdsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectPermissionIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> assignPermissionsToUser(UserPermissionDTO dto) {
//        if (dto == null || dto.getUserId() == null || dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
//            return CommonResult.failed("参数错误");
//        }
//
//        try {
//            // 移除可能存在的重复权限
//            List<Long> existingPermissionIds = baseMapper.selectPermissionIdsByUserId(dto.getUserId());
//            List<Long> newPermissionIds = new ArrayList<>(dto.getPermissionIds());
//            newPermissionIds.removeAll(existingPermissionIds);
//
//            if (!newPermissionIds.isEmpty()) {
//                // 使用MyBatis-Plus的批量保存
//                List<UserPermission> userPermissions = newPermissionIds.stream()
//                        .map(permissionId -> {
//                            UserPermission up = new UserPermission();
//                            up.setUserId(dto.getUserId());
//                            up.setPermissionId(permissionId);
//                            return up;
//                        })
//                        .collect(Collectors.toList());
//
//                this.saveBatch(userPermissions);
//            }
//
//            return CommonResult.success("权限分配成功");
//        } catch (Exception e) {
//            log.error("分配用户权限失败", e);
//            return CommonResult.failed("权限分配失败");
//        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> removeAllPermissionsFromUser(Long userId) {
//        if (userId == null) {
//            return CommonResult.failed("用户ID不能为空");
//        }
//
//        try {
//            LambdaQueryWrapper<UserPermission> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(UserPermission::getUserId, userId);
//            this.remove(queryWrapper);
//            return CommonResult.success("用户权限移除成功");
//        } catch (Exception e) {
//            log.error("移除用户所有权限失败", e);
//            return CommonResult.failed("权限移除失败");
//        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> removePermissionsFromUser(UserPermissionDTO dto) {
//        if (dto == null || dto.getUserId() == null || dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
//            return Result.failed("参数错误");
//        }
//
//        try {
//            baseMapper.deleteByUserIdAndPermissionIds(dto.getUserId(), dto.getPermissionIds());
//            return Result.success("用户权限移除成功");
//        } catch (Exception e) {
//            log.error("移除用户指定权限失败", e);
//            return Result.failed("权限移除失败");
//        }
        return null;
    }

    @Override
    public boolean hasPermission(Long userId, Long permissionId) {
        if (userId == null || permissionId == null) {
            return false;
        }
        return baseMapper.hasPermission(userId, permissionId);
    }
} 