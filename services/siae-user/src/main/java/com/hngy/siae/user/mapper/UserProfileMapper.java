package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户详情表 Mapper 接口
 *
 * @author KEYKB
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
