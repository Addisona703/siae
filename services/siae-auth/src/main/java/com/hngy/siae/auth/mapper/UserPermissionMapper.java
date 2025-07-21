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
     * 通过用户ID查询用户权限关联列表
     *
     * @param userId 用户ID
     * @return 用户权限关联列表
     */
    List<UserPermission> selectListByUserId(@Param("userId") Long userId);
    
    /**
     * 通过用户ID查询权限ID列表
     *
     * @param userId 用户ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM user_permission WHERE user_id = #{userId}")
    List<Long> selectPermissionIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 批量新增用户权限关联
     *
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     * @return 影响行数
     */
    int batchInsert(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
    
    /**
     * 通过用户ID删除用户权限关联
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 通过用户ID和权限ID列表删除用户权限关联
     *
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     * @return 影响行数
     */
    @Delete("<script>DELETE FROM user_permission WHERE user_id = #{userId} AND permission_id IN <foreach collection='permissionIds' item='permissionId' open='(' separator=',' close=')'>#{permissionId}</foreach></script>")
    int deleteByUserIdAndPermissionIds(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
    
    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 是否拥有权限
     */
    @Select("SELECT COUNT(1) > 0 FROM user_permission WHERE user_id = #{userId} AND permission_id = #{permissionId}")
    boolean hasPermission(@Param("userId") Long userId, @Param("permissionId") Long permissionId);
} 