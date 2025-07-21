package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 * 
 * @author KEYKB
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
} 