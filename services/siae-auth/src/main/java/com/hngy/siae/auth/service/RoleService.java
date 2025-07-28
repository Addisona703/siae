package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.RoleCreateDTO;
import com.hngy.siae.auth.dto.request.RoleQueryDTO;
import com.hngy.siae.auth.dto.request.RoleUpdateDTO;
import com.hngy.siae.auth.dto.response.PermissionVO;
import com.hngy.siae.auth.dto.response.RoleVO;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author KEYKB
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色响应
     */
    RoleVO createRole(RoleCreateDTO request);

    /**
     * 分页查询角色列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页角色列表
     */
    PageVO<RoleVO> getRolesPage(PageDTO<RoleQueryDTO> pageDTO);

    /**
     * 获取角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> getRoles();

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新角色请求
     * @return 是否成功
     */
    boolean updateRole(Long roleId, RoleUpdateDTO request);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID列表
     * @return 删除结果
     */
    Boolean batchDeleteRoles(List<Long> roleIds);
    
    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色响应
     */
    RoleVO getRole(Long roleId);
    
    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Long roleId);

    /**
     * 更新角色权限
     * <p>
     * 完全替换角色的权限列表
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 更新结果
     */
    Boolean updateRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 追加角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

}