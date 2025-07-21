package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户认证Mapper接口
 * 
 * @author KEYKB
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {
} 