package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.request.PermissionCreateDTO;
import com.hngy.siae.auth.dto.request.PermissionQueryDTO;
import com.hngy.siae.auth.dto.request.PermissionTreeUpdateDTO;
import com.hngy.siae.auth.dto.request.PermissionUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionTreeVO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.util.List;

/**
 * 权限服务接口
 * 
 * @author KEYKB
 */
public interface PermissionService {
    
    /**
     * 创建权限
     *
     * @param PermissionCreate 创建权限请求
     * @return 权限响应
     */
    PermissionVO createPermission(PermissionCreateDTO PermissionCreate);

    /**
     * 分页查询权限列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页权限列表
     */
    PageVO<PermissionVO> getPermissionsPage(PageDTO<PermissionQueryDTO> pageDTO);

    /**
     * 查询权限树结构
     *
     * @param enabledOnly 是否只查询启用状态的权限
     * @return 权限树列表
     */
    List<PermissionTreeVO> getPermissionTree(Boolean enabledOnly);

    /**
     * 批量更新权限树结构
     * <p>
     * 用于支持前端拖拽操作后修改权限的层级依赖关系。
     * 在同一个事务中批量更新所有权限的parent_id和sort_order。
     *
     * @param permissionTreeUpdates 权限树结构批量更新请求列表
     * @return 更新结果
     */
    Boolean updatePermissionTree(List<PermissionTreeUpdateDTO> permissionTreeUpdates);

    /**
     * 获取权限列表
     *
     * @return 权限列表
     */
    List<PermissionVO> getPermissions();

    /**
     * 获取权限详情
     *
     * @param permissionId 权限ID
     * @return 权限响应
     */
    PermissionVO getPermission(Long permissionId);

    /**
     * 更新权限
     *
     * @param permissionUpdateDTO 更新权限请求
     * @return 权限响应
     */
    PermissionVO updatePermission(PermissionUpdateDTO permissionUpdateDTO);

    /**
     * 删除权限
     *
     * @param permissionId 权限ID
     * @return 删除结果
     */
    Boolean deletePermission(Long permissionId);

    /**
     * 批量删除权限
     *
     * @param permissionIds 权限ID列表
     * @return 删除结果
     */
    Boolean batchDeletePermissions(List<Long> permissionIds);
}