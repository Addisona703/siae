package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.MemberCandidateCreateDTO;
import com.hngy.siae.user.dto.request.MemberCandidateQueryDTO;
import com.hngy.siae.user.dto.request.MemberCandidateUpdateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.entity.MemberCandidate;

import java.util.List;

/**
 * 候选成员服务接口
 * <p>
 * 提供候选成员的增删改查功能，包括创建、更新、查询和删除候选成员信息。
 * 支持分页查询和条件查询，支持按部门、状态等条件筛选。
 *
 * @author KEYKB
 */
public interface MemberCandidateService extends IService<MemberCandidate> {
    /**
     * 创建候选成员
     *
     * @param memberCandidateCreateDTO 候选成员创建参数
     * @return 候选成员视图对象
     */
    MemberCandidateVO createMemberCandidate(MemberCandidateCreateDTO memberCandidateCreateDTO);

    /**
     * 添加候选成员
     *
     * @param candidateDTO 候选成员信息
     * @return 新增的候选成员信息
     */
    MemberCandidateVO addCandidate(MemberCandidateCreateDTO candidateDTO);

    /**
     * 更新候选成员信息
     *
     * @param candidateDTO 候选成员信息
     * @return 更新后的候选成员信息
     */
    MemberCandidateVO updateCandidate(MemberCandidateUpdateDTO candidateDTO);

    /**
     * 根据ID查询候选成员信息
     *
     * @param id 候选成员ID
     * @return 候选成员信息
     */
    MemberCandidateVO getCandidateById(Long id);

    /**
     * 根据用户ID查询候选成员信息
     *
     * @param userId 用户ID
     * @return 候选成员信息
     */
    MemberCandidateVO getCandidateByUserId(Long userId);

    /**
     * 根据学号查询候选成员信息
     *
     * @param studentId 学号
     * @return 候选成员信息
     */
    MemberCandidateVO getCandidateByStudentId(String studentId);

    /**
     * 根据部门ID查询候选成员列表
     *
     * @param departmentId 部门ID
     * @return 候选成员列表
     */
    List<MemberCandidateVO> listCandidatesByDepartment(Long departmentId);

    /**
     * 动态条件查询候选成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的候选成员列表
     */
    List<MemberCandidateVO> listCandidates(MemberCandidateQueryDTO queryDTO);

    /**
     * 分页查询候选成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    PageVO<MemberCandidateVO> listCandidatesByPage(PageDTO<MemberCandidateQueryDTO> pageDTO);

    /**
     * 删除候选成员（修改status字段为0）
     *
     * @param id 候选成员ID
     * @return 是否删除成功
     */
    boolean deleteCandidate(Long id);

    /**
     * 批量删除候选成员（修改status字段为0）
     *
     * @param ids 候选成员ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteCandidates(List<Long> ids);
}
