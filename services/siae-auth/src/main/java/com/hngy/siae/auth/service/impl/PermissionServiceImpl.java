package com.hngy.siae.auth.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.PermissionCreateDTO;
import com.hngy.siae.auth.dto.request.PermissionQueryDTO;
import com.hngy.siae.auth.dto.request.PermissionTreeUpdateDTO;
import com.hngy.siae.auth.dto.request.PermissionUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionTreeVO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.entity.Permission;
import com.hngy.siae.auth.mapper.PermissionMapper;
import com.hngy.siae.auth.service.PermissionService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 权限服务实现类
 * <p>
 * 提供系统权限的创建、查询、更新和删除功能，
 * 支持权限验证和分页查询。
 *
 * @author KEYKB
 */
@Service
public class PermissionServiceImpl
        extends ServiceImpl<PermissionMapper, Permission>
        implements PermissionService {

    /**
     * 创建权限
     */
    @Override
    public PermissionVO createPermission(PermissionCreateDTO request) {
        // 检查权限编码是否已存在
        boolean exists = lambdaQuery()
                .eq(Permission::getCode, request.getCode())
                .eq(Permission::getName, request.getName())
                .exists();

        AssertUtils.isFalse(exists, AuthResultCodeEnum.PERMISSION_CODE_EXISTS);
        
        // 创建权限
        Permission permission = BeanConvertUtil.to(request, Permission.class);
        AssertUtils.isTrue(save(permission), "权限创建失败");
        
        // 构建响应
        return BeanConvertUtil.to(permission, PermissionVO.class);
    }

    /**
     * 获取权限列表
     */
    @Override
    public List<PermissionVO> getPermissions() {
        List<Permission> permissions = lambdaQuery().list();
        return BeanConvertUtil.toList(permissions, PermissionVO.class);
    }

    /**
     * 获取权限详情
     */
    @Override
    public PermissionVO getPermission(Long permissionId) {
        return Optional.ofNullable(baseMapper.selectById(permissionId))
                .map(permission -> BeanConvertUtil.to(permission, PermissionVO.class))
                .orElseThrow(() -> new ServiceException("权限不存在"));
    }

    /**
     * 更新权限
     */
    @Override
    public PermissionVO updatePermission(PermissionUpdateDTO dto) {
        // 1. 查询并校验权限是否存在
        Permission existing = getById(dto.getId());
        AssertUtils.notNull(existing, AuthResultCodeEnum.PERMISSION_NOT_FOUND);

        // 2. 校验权限编码是否重复（排除自身）
        boolean duplicate = lambdaQuery()
                .eq(Permission::getName, dto.getName())
                .eq(Permission::getCode, dto.getCode())
                .ne(Permission::getId, dto.getId())
                .exists();
        AssertUtils.isFalse(duplicate, AuthResultCodeEnum.PERMISSION_CODE_EXISTS);

        // 3. 属性合并更新
        Permission permission = BeanConvertUtil.to(dto, Permission.class);
        permission.setUpdatedAt(LocalDateTime.now());
        AssertUtils.isTrue(updateById(permission), "权限更新失败");

        return BeanConvertUtil.to(permission, PermissionVO.class);
    }

    /**
     * 删除权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePermission(Long permissionId) {
        Permission permission = getById(permissionId);
        AssertUtils.notNull(permission, AuthResultCodeEnum.PERMISSION_NOT_FOUND);

        // TODO: 检查是否有角色关联该权限

        // 一次性获取所有权限，避免递归查库
        List<Permission> allPermissions = list();
        Map<Long, List<Permission>> parentMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        Set<Long> allPermissionIds = new HashSet<>();
        allPermissionIds.add(permissionId);
        collectAllChildrenIds(permissionId, allPermissionIds, parentMap);

        // 执行批量逻辑删除
        return lambdaUpdate()
                .set(Permission::getStatus, 0)
                .set(Permission::getUpdatedAt, LocalDateTime.now())
                .in(Permission::getId, allPermissionIds)
                .update();
    }

    /**
     * 批量删除权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeletePermissions(List<Long> permissionIds) {
        // 一次性查询所有权限，构建父子映射
        List<Permission> allPermissions = list();
        Map<Long, List<Permission>> parentChildMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        // 递归获取所有待删除权限ID（包括子权限）
        Set<Long> allDeleteIds = new HashSet<>();
        for (Long permissionId : permissionIds) {
            // 从缓存的 allPermissions 集合中查找权限
            boolean exists = allPermissions.stream().anyMatch(p -> p.getId().equals(permissionId));
            AssertUtils.isTrue(exists, "权限ID:" + permissionId + "不存在");

            // 递归获取子权限ID
            collectAllChildrenIds(permissionId, allDeleteIds, parentChildMap);
        }
        // 把传入的ID也加上
        allDeleteIds.addAll(permissionIds);

        // TODO: 批量检查是否有角色关联这些权限

        // 批量逻辑删除：状态设为0，更新时间
        return lambdaUpdate()
                .set(Permission::getStatus, 0)
                .set(Permission::getUpdatedAt, LocalDateTime.now())
                .in(Permission::getId, allDeleteIds)
                .update();
    }

     /**
     * 递归收集所有子权限ID，去重
     *
     * @param parentId       父权限ID
     * @param collector      用于收集权限ID的 Set，避免重复
     * @param parentChildMap 父权限ID -> 子权限列表 映射
     */
    private void collectAllChildrenIds(Long parentId, Set<Long> collector, Map<Long, List<Permission>> parentChildMap) {
        List<Permission> children = parentChildMap.getOrDefault(parentId, List.of());
        for (Permission child : children) {
            if (collector.add(child.getId())) {  // add 返回 true 表示刚添加，避免重复递归
                collectAllChildrenIds(child.getId(), collector, parentChildMap);
            }
        }
    }

    /**
     * 分页查询权限列表
     */
    @Override
    public PageVO<PermissionVO> getPermissionsPage(PageDTO<PermissionQueryDTO> pageDTO) {
        PermissionQueryDTO queryDTO = Optional.ofNullable(pageDTO.getParams()).orElseGet(PermissionQueryDTO::new);

        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<Permission>()
                .like(StrUtil.isNotBlank(queryDTO.getName()), Permission::getName, queryDTO.getName())
                .like(StrUtil.isNotBlank(queryDTO.getCode()), Permission::getCode, queryDTO.getCode())
                .eq(StrUtil.isNotBlank(queryDTO.getType()), Permission::getType, queryDTO.getType())
                .eq(queryDTO.getParentId() != null, Permission::getParentId, queryDTO.getParentId())
                .eq(queryDTO.getStatus() != null, Permission::getStatus, queryDTO.getStatus())
                .ge(StrUtil.isNotBlank(queryDTO.getCreatedAtStart()), Permission::getCreatedAt, queryDTO.getCreatedAtStart())
                .le(StrUtil.isNotBlank(queryDTO.getCreatedAtEnd()), Permission::getCreatedAt, queryDTO.getCreatedAtEnd())
                .orderByAsc(Permission::getSortOrder)
                .orderByDesc(Permission::getCreatedAt);

        IPage<Permission> page = PageConvertUtil.toPage(pageDTO);
        IPage<Permission> resultPage = page(page, queryWrapper);

        return PageConvertUtil.convert(resultPage, PermissionVO.class);
    }

    /**
     * 查询权限树结构
     */
    @Override
    public List<PermissionTreeVO> getPermissionTree(Boolean enabledOnly) {
        // 查询条件
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        if (Boolean.TRUE.equals(enabledOnly)) {
            queryWrapper.eq(Permission::getStatus, 1);
        }
        queryWrapper.orderByAsc(Permission::getSortOrder);

        List<Permission> allPermissions = list(queryWrapper);
        List<PermissionTreeVO> permissionVOs = BeanConvertUtil.toList(allPermissions, PermissionTreeVO.class);

        // 分组构建 parentId -> children map
        Map<Long, List<PermissionTreeVO>> childrenMap = permissionVOs.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));

        // 构建根节点（parentId == null or 0）
        List<PermissionTreeVO> roots = childrenMap.getOrDefault(0L, new ArrayList<>());

        // 递归构建树（带层级）
        for (PermissionTreeVO root : roots) {
            buildTree(root, childrenMap, 0);
        }

        return roots;
    }

    /**
     * 递归构建树（带层级）
     */
    private void buildTree(PermissionTreeVO parent, Map<Long, List<PermissionTreeVO>> childrenMap, int level) {
        List<PermissionTreeVO> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        parent.setIsLeaf(children.isEmpty());
        parent.setLevel(level);

        for (PermissionTreeVO child : children) {
            buildTree(child, childrenMap, level + 1);
        }

        // 排序子节点
        children.sort(Comparator.comparing(PermissionTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
    }

    /**
     * 批量更新权限树结构
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermissionTree(List<PermissionTreeUpdateDTO> permissionTreeUpdates) {
        List<Long> permissionIds = permissionTreeUpdates.stream()
                .map(PermissionTreeUpdateDTO::getId)
                .distinct()
                .collect(Collectors.toList());

        List<Permission> existingPermissions = listByIds(permissionIds);
        AssertUtils.isTrue(existingPermissions.size() == permissionIds.size(), "存在不存在的权限ID，请检查数据");

        // 预先查询所有权限，避免多次查询
        List<Permission> allPermissions = list();

        // 构建权限ID -> 权限对象映射，便于后续查找
        Map<Long, Permission> permissionMap = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, p -> p));

        // 构建更新后的父子关系映射
        Map<Long, Long> finalParentMap = buildFinalParentMap(allPermissions, permissionTreeUpdates);

        // 2. 验证循环依赖
        validateCircularDependency(permissionTreeUpdates, finalParentMap, permissionMap);

        // 3. 验证层级深度
        validateMaxLevel(permissionTreeUpdates, finalParentMap, permissionMap);

        // 4. 批量更新权限
        LocalDateTime currentTime = LocalDateTime.now();
        List<Permission> permissionsToUpdate = new ArrayList<>();

        for (PermissionTreeUpdateDTO updateDTO : permissionTreeUpdates) {
            Permission permission = permissionMap.get(updateDTO.getId());

            if (permission != null) {
                Long newParentId = updateDTO.getParentId();
                if (newParentId != null && newParentId == 0) {
                    newParentId = null;
                }

                permission.setParentId(newParentId);
                permission.setSortOrder(updateDTO.getSortOrder());
                permission.setUpdatedAt(currentTime);
                permissionsToUpdate.add(permission);
            }
        }

        boolean result = updateBatchById(permissionsToUpdate);
        AssertUtils.isTrue(result, "权限树结构更新失败");

        return true;
    }

    /**
     * 构建更新后的父子关系映射
     */
    private Map<Long, Long> buildFinalParentMap(List<Permission> allPermissions, List<PermissionTreeUpdateDTO> updates) {
        // 构建映射，Id -> ParentId
        Map<Long, Long> existingParentMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.toMap(
                        Permission::getId,
                        Permission::getParentId,
                        (existing, replacement) -> replacement
                ));

        // 构建更新后的父子关系映射
        Map<Long, Long> newParentMap = new HashMap<>();
        for (PermissionTreeUpdateDTO dto : updates) {
            Long id = dto.getId();
            Long parentId = dto.getParentId();
            newParentMap.put(id, (parentId == null || parentId == 0) ? null : parentId);
        }

        Map<Long, Long> finalParentMap = new HashMap<>(existingParentMap);
        finalParentMap.putAll(newParentMap);
        return finalParentMap;
    }

    /**
     * 验证循环依赖
     */
    private void validateCircularDependency(List<PermissionTreeUpdateDTO> updates, Map<Long, Long> finalParentMap,
                                            Map<Long, Permission> permissionMap) {
        for (PermissionTreeUpdateDTO updateDTO : updates) {
            Long permissionId = updateDTO.getId();
            Long parentId = updateDTO.getParentId();

            if (parentId != null && parentId != 0) {
                // 这里判断如果是后代关系，抛异常
                AssertUtils.isFalse(
                        isDescendant(permissionId, parentId, finalParentMap),
                        "不能将权限 [" + getPermissionName(permissionId, permissionMap) + "] 移动到其子权限下，这会形成循环依赖"
                );
            }
        }
    }

    /**
     * 验证层级深度
     */
    private void validateMaxLevel(List<PermissionTreeUpdateDTO> updates, Map<Long, Long> finalParentMap,
                                  Map<Long, Permission> permissionMap) {
        final int MAX_LEVEL = 5;

        for (PermissionTreeUpdateDTO updateDTO : updates) {
            int level = calculateLevel(updateDTO.getId(), finalParentMap);
            String permissionName = getPermissionName(updateDTO.getId(), permissionMap);
            AssertUtils.isFalse(level > MAX_LEVEL,
                    "权限 [" + permissionName + "] 的层级深度超过系统限制（最大" + MAX_LEVEL + "级）");
        }
    }

    /**
     * 获取权限名
     */
    private String getPermissionName(Long permissionId, Map<Long, Permission> permissionMap) {
        Permission permission = permissionMap.get(permissionId);
        return permission != null ? permission.getName() : "ID:" + permissionId;
    }

    /**
     * 判断是否存在后代关系
     */
    private boolean isDescendant(Long ancestorId, Long targetId, Map<Long, Long> parentMap) {
        if (targetId == null) {
            return false;
        }
        if (ancestorId.equals(targetId)) {
            return true;
        }
        Long parentId = parentMap.get(targetId);
        return isDescendant(ancestorId, parentId, parentMap);
    }

    /**
     * 计算层级深度
     */
    private int calculateLevel(Long permissionId, Map<Long, Long> parentMap) {
        Long parentId = parentMap.get(permissionId);
        if (parentId == null) {
            return 0;
        }
        return calculateLevel(parentId, parentMap) + 1;
    }

    /**
     * 验证权限是否都存在
     * <p>
     * 验证给定的权限ID列表中的所有权限都存在于数据库中。
     * 如果权限ID列表为空，则直接返回不进行验证。
     * 如果存在不存在的权限ID，则抛出异常。
     *
     * @param permissionIds 权限ID列表
     * @throws com.hngy.siae.core.exception.ServiceException 当存在不存在的权限ID时抛出异常
     * @author KEYKB
     */
    @Override
    public void validatePermissionsExist(List<Long> permissionIds) {
        // 如果权限ID列表为空，直接返回
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        // 查询权限列表
        List<Permission> permissions = listByIds(permissionIds);

        // 验证查询结果数量与输入ID数量是否一致
        AssertUtils.isTrue(permissions.size() == permissionIds.size(),
            AuthResultCodeEnum.PERMISSION_NOT_EXISTS);
    }
}