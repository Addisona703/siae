package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.MembershipCreateDTO;
import com.hngy.siae.user.dto.request.MembershipPromoteDTO;
import com.hngy.siae.user.dto.request.MembershipQueryDTO;
import com.hngy.siae.user.dto.request.MembershipUpdateDTO;
import com.hngy.siae.user.dto.response.MembershipDetailVO;
import com.hngy.siae.user.dto.response.MembershipVO;
import com.hngy.siae.user.entity.Membership;

/**
 * 成员统一服务接口
 * 合并了原 MemberService 和 MemberCandidateService 的功能
 *
 * @author KEYKB
 */
public interface MembershipService extends IService<Membership> {

    /**
     * 创建候选成员
     * 用户申请加入协会时调用
     *
     * @param createDTO 创建DTO
     * @return 成员ID
     */
    Long createCandidate(MembershipCreateDTO createDTO);

    /**
     * 候选成员转正
     * 管理员审核通过后调用
     *
     * @param promoteDTO 转正DTO
     * @return 是否成功
     */
    boolean promoteToOfficial(MembershipPromoteDTO promoteDTO);

    /**
     * 更新成员信息
     *
     * @param updateDTO 更新DTO
     * @return 是否成功
     */
    boolean updateMembership(MembershipUpdateDTO updateDTO);

    /**
     * 根据ID删除成员（逻辑删除）
     *
     * @param id 成员ID
     * @return 是否成功
     */
    boolean deleteMembership(Long id);

    /**
     * 根据ID查询成员信息（含用户、部门、职位信息）
     *
     * @param id 成员ID
     * @return 成员详情
     */
    MembershipDetailVO getMembershipById(Long id);

    /**
     * 根据用户ID查询成员信息（含用户、部门、职位信息）
     *
     * @param userId 用户ID
     * @return 成员详情
     */
    MembershipDetailVO getMembershipByUserId(Long userId);

    /**
     * 根据ID查询成员详情（含用户、部门、职位信息）
     *
     * @param id 成员ID
     * @return 成员详情
     */
    MembershipDetailVO getMembershipDetailById(Long id);

    /**
     * 分页查询成员列表
     *
     * @param pageDTO 分页查询参数（包含分页信息、查询条件和关键字）
     * @return 分页结果
     */
    PageVO<MembershipVO> pageMemberships(PageDTO<MembershipQueryDTO> pageDTO);

    /**
     * 判断用户是否为成员（候选或正式）
     *
     * @param userId 用户ID
     * @return 是否为成员
     */
    boolean isMember(Long userId);

    /**
     * 判断用户是否为候选成员
     *
     * @param userId 用户ID
     * @return 是否为候选成员
     */
    boolean isCandidate(Long userId);

    /**
     * 判断用户是否为正式成员
     *
     * @param userId 用户ID
     * @return 是否为正式成员
     */
    boolean isOfficialMember(Long userId);

    /**
     * 审核通过：待审核 -> 候选成员
     *
     * @param id 成员ID
     * @return 是否成功
     */
    boolean approveCandidate(Long id);

    /**
     * 审核拒绝：待审核 -> 已拒绝
     *
     * @param id 成员ID
     * @return 是否成功
     */
    boolean rejectCandidate(Long id);
}
