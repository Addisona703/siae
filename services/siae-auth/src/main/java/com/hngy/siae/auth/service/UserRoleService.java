package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.UserRoleQueryDTO;
import com.hngy.siae.auth.dto.request.UserRoleUpdateDTO;
import com.hngy.siae.auth.dto.response.UserRoleVO;
import com.hngy.siae.auth.entity.UserRole;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.util.List;

/**
 * 用户角色关联服务接口
 * 
 * @author KEYKB
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 为用户分配单个角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean assignUserRole(Long userId, Long roleId);

    /**
     * 批量分配角色给用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    Boolean batchAssignRoleToUsers(Long roleId, List<Long> userIds);

    /**
     * 分页查询用户角色关联列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    PageVO<UserRoleVO> getUserRoleList(PageDTO<UserRoleQueryDTO> pageDTO);

    /**
     * 更新用户角色关联
     *
     * @param userRoleId 用户角色关联ID
     * @param updateDTO  更新参数
     * @return 是否成功
     */
    Boolean updateUserRole(Long userRoleId, UserRoleUpdateDTO updateDTO);
}
