package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.UserPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户权限关联表Mapper接口
 */
@Mapper
public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    /**
     * 通过用户ID查询权限ID列表
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM user_permission WHERE user_id = #{userId}")
    List<Long> selectPermissionIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 是否拥有权限
     */
    @Select("SELECT COUNT(1) > 0 FROM user_permission WHERE user_id = #{userId} AND permission_id = #{permissionId}")
    boolean hasPermission(@Param("userId") Long userId, @Param("permissionId") Long permissionId);

    /**
     * 通过用户ID查询用户权限代码列表（使用JOIN查询）
     *
     * @param userId 用户ID
     * @return 权限代码列表
     */
    @Select("SELECT p.code FROM user_permission up " +
            "JOIN permission p ON up.permission_id = p.id " +
            "WHERE up.user_id = #{userId} AND p.status = 1")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}