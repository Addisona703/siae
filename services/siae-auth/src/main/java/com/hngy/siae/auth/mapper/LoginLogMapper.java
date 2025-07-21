package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper接口
 * 
 * @author KEYKB
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
} 