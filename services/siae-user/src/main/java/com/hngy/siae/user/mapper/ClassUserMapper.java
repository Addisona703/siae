package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.response.ClassUserVO;
import com.hngy.siae.user.entity.ClassUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 班级用户关联数据访问层
 *
 * @author KEYKB
 */
@Mapper
public interface ClassUserMapper extends BaseMapper<ClassUser> {

    /**
     * 分页查询班级用户关联信息（包含用户基本信息）
     *
     * @param page 分页参数
     * @param wrapper 查询条件
     * @return 分页班级用户关联信息
     */
    Page<ClassUserVO> selectClassUsersWithUserInfo(@Param("page") Page<ClassUser> page,
                                                   @Param("ew") QueryWrapper<ClassUser> wrapper);
}