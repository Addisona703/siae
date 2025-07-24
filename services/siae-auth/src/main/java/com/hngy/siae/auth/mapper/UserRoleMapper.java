package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author KEYKB
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 通过用户ID查询用户角色代码列表（使用JOIN查询）
     *
     * @param userId 用户ID
     * @return 角色代码列表
     */
    @Select("SELECT r.code FROM user_role ur " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}