package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.MemberDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.entity.Member;

import java.util.List;

/**
 * 正式成员服务接口
 *
 * @author KEYKB
 */
public interface MemberService extends IService<Member> {
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
     * @param memberDTO 正式成员信息
     * @return 更新后的正式成员信息
     */
    MemberVO updateMember(MemberDTO memberDTO);

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
    List<MemberVO> listMembers(MemberDTO queryDTO);

    /**
     * 分页查询正式成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    PageVO<MemberVO> pageMember(PageDTO<MemberDTO> pageDTO);

    /**
     * 删除正式成员（逻辑删除）
     *
     * @param id 正式成员ID
     * @return 是否删除成功
     */
    boolean deleteMember(Long id);
}
