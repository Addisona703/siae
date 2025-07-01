package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联Mapper接口
 * 
 * @author KEYKB
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
} 