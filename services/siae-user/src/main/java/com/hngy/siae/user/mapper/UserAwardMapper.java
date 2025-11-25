package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.UserAward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 分页查询用户获奖记录（带关联信息）
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 用户获奖记录VO分页列表（包含奖项等级名称、类型名称和团队成员信息）
     */
    Page<UserAwardVO> selectUserAwardPageWithDetails(Page<UserAwardVO> page, @Param("query") UserAwardQueryDTO query);

    /**
     * 根据用户ID列表批量查询用户基本信息
     *
     * @param userIds 用户ID列表
     * @return 用户基本信息列表
     */
    List<UserVO> selectUsersByIds(@Param("userIds") List<Long> userIds);
} 