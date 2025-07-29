package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.UserPermissionDTO;
import com.hngy.siae.auth.entity.UserPermission;
import com.hngy.siae.auth.dto.response.UserPermissionVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

/**
 * 用户权限关联服务接口
 *
 * @author KEYKB
 */
public interface UserPermissionService extends IService<UserPermission> {

    /**
     * 分页查询用户权限关联列表
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 用户权限关联分页列表
     */
    PageVO<UserPermissionVO> getUserPermissionsByUserId(Long userId, PageDTO<Object> pageDTO);

    /**
     * 给用户分配权限（覆盖模式）
     *
     * @param dto 用户权限DTO
     * @return 操作结果
     */
    Boolean assignPermissionsToUser(UserPermissionDTO dto);

    /**
     * 追加用户权限（增量模式）
     *
     * @param dto 用户权限DTO
     * @return 操作结果
     */
    Boolean appendPermissionsToUser(UserPermissionDTO dto);

    /**
     * 移除用户所有权限
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Boolean removeAllPermissionsFromUser(Long userId);

    /**
     * 移除用户指定权限
     *
     * @param dto 用户权限DTO
     * @return 操作结果
     */
    Boolean removePermissionsFromUser(UserPermissionDTO dto);
}