package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.dto.response.UserDetailVO;
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
} 