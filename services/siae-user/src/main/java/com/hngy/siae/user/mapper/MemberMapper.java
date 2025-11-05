package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.request.MemberQueryDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成员表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 根据成员ID查询详情（包含部门/职位名称）
     *
     * @param id 成员ID
     * @return 成员视图对象
     */
    MemberVO selectMemberDetailById(@Param("id") Long id);

    /**
     * 根据用户ID查询成员详情（包含部门/职位名称）
     *
     * @param userId 用户ID
     * @return 成员视图对象
     */
    MemberVO selectMemberDetailByUserId(@Param("userId") Long userId);

    /**
     * 条件查询成员详情列表（包含部门/职位名称）
     *
     * @param query 查询条件
     * @return 成员视图对象列表
     */
    List<MemberVO> selectMemberDetails(@Param("query") MemberQueryDTO query);

    /**
     * 分页查询成员详情列表（包含部门/职位名称）
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<MemberVO> selectMemberDetailsPage(@Param("page") Page<MemberVO> page,
                                           @Param("query") MemberQueryDTO query);

} 
