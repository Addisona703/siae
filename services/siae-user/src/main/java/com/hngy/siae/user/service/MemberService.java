package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.MemberCreateDTO;
import com.hngy.siae.user.dto.request.MemberQueryDTO;
import com.hngy.siae.user.dto.request.MemberUpdateDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.entity.Member;

import java.util.List;

/**
 * 正式成员服务接口
 * <p>
 * 提供正式成员的增删改查功能，包括创建、更新、查询和删除正式成员信息。
 * 支持分页查询和条件查询，支持按部门、职位、状态等条件筛选。
 *
 * @author KEYKB
 */
public interface MemberService extends IService<Member> {
    /**
     * 创建正式成员
     *
     * @param memberCreateDTO 正式成员创建参数
     * @return 正式成员视图对象
     */
    MemberVO createMember(MemberCreateDTO memberCreateDTO);

    /**
     * 添加正式成员（从候选成员转过来，候选成员表逻辑删除）
     *
     * @param candidateId 候选成员ID
     * @param positionId 职位ID
     * @return 新增的正式成员信息
     */
    MemberVO addMemberFromCandidate(Long candidateId, Long positionId);

    /**
     * 更新正式成员信息
     *
     * @param memberUpdateDTO 正式成员更新参数
     * @return 更新后的正式成员信息
     */
    MemberVO updateMember(MemberUpdateDTO memberUpdateDTO);

    /**
     * 查询正式成员信息
     *
     * @param id 正式成员ID
     * @return 正式成员信息
     */
    MemberVO getMemberById(Long id);

    /**
     * 根据用户ID查询正式成员信息
     *
     * @param userId 用户ID
     * @return 正式成员信息
     */
    MemberVO getMemberByUserId(Long userId);

    /**
     * 动态条件查询正式成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的正式成员列表
     */
    List<MemberVO> listMembers(MemberQueryDTO queryDTO);

    /**
     * 分页查询正式成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    PageVO<MemberVO> listMembersByPage(PageDTO<MemberQueryDTO> pageDTO);

    /**
     * 删除正式成员（逻辑删除）
     *
     * @param id 正式成员ID
     * @return 是否删除成功
     */
    boolean deleteMember(Long id);
}
