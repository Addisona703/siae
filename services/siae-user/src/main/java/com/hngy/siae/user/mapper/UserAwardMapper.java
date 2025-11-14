package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.entity.UserAward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户获奖记录表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface UserAwardMapper extends BaseMapper<UserAward> {

    /**
     * 分页查询用户获奖记录
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 用户获奖记录分页列表
     */
    Page<UserAward> selectUserAwardPage(Page<UserAward> page, @Param("query") UserAwardQueryDTO query);
} 