package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.response.UserDetailVO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户主表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户ID查询用户详细信息（包含用户基本信息、详情信息和班级关联信息）
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetailVO selectUserDetailById(@Param("userId") Long userId);

    /**
     * 根据条件查询用户详细信息（包含用户基本信息、详情信息和班级关联信息）
     *
     * @param id 用户ID（可选）
     * @param username 用户名（可选）
     * @param studentId 学号（可选）
     * @return 用户详细信息
     */
    UserDetailVO selectUserDetail(@Param("id") Long id, @Param("username") String username, @Param("studentId") String studentId);

    /**
     * 分页查询用户列表（联表查询用户基本信息和详情信息）
     *
     * @param page 分页对象
     * @param username 用户名（模糊查询）
     * @param studentId 学号（模糊查询）
     * @param realName 真实姓名（模糊查询）
     * @param email 邮箱（模糊查询）
     * @param status 状态
     * @return 用户列表
     */
    IPage<UserVO> selectUsersByPage(
            Page<?> page, 
            @Param("username") String username, 
            @Param("studentId") String studentId,
            @Param("realName") String realName,
            @Param("email") String email,
            @Param("status") Integer status);
} 