package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.user.dto.request.MembershipQueryDTO;
import com.hngy.siae.user.dto.response.MemberDepartmentVO;
import com.hngy.siae.user.dto.response.MemberPositionVO;
import com.hngy.siae.user.dto.response.MembershipDetailVO;
import com.hngy.siae.user.entity.Membership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成员统一表 Mapper 接口
 * 合并了原 MemberMapper 和 MemberCandidateMapper 的功能
 *
 * @author KEYKB
 */
@Mapper
public interface MembershipMapper extends BaseMapper<Membership> {

    /**
     * 根据用户ID查询成员信息
     *
     * @param userId 用户ID
     * @return 成员信息
     */
    Membership selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询成员列表
     *
     * @param page 分页对象
     * @param queryDTO 查询条件
     * @return 成员列表
     */
    Page<Membership> selectMembershipPage(Page<Membership> page, @Param("query") MembershipQueryDTO queryDTO);

    /**
     * 根据ID查询成员详情
     *
     * @param id 成员ID
     * @return 成员详情
     */
    MembershipDetailVO selectMembershipDetailById(@Param("id") Long id);

    /**
     * 根据用户ID查询荣誉成就列表
     *
     * @param userId 用户ID
     * @return 奖项名称列表
     */
    List<String> selectAwardsByUserId(@Param("userId") Long userId);

    /**
     * 根据成员ID查询部门列表
     *
     * @param membershipId 成员ID
     * @return 部门列表
     */
    List<MemberDepartmentVO> selectDepartmentsByMembershipId(@Param("membershipId") Long membershipId);

    /**
     * 根据成员ID查询职位列表
     *
     * @param membershipId 成员ID
     * @return 职位列表
     */
    List<MemberPositionVO> selectPositionsByMembershipId(@Param("membershipId") Long membershipId);

    /**
     * 根据用户ID查询成员详情
     *
     * @param userId 用户ID
     * @return 成员详情
     */
    MembershipDetailVO selectMembershipDetailByUserId(@Param("userId") Long userId);

    /**
     * AI服务查询成员信息
     *
     * @param name 成员姓名，支持模糊匹配
     * @param department 部门名称
     * @param position 职位名称
     * @return 成员信息列表
     */
    List<com.hngy.siae.api.ai.dto.response.MemberInfo> searchMembersForAi(
            @Param("name") String name,
            @Param("department") String department,
            @Param("position") String position);
}
