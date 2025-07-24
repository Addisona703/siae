package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.MemberCandidateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.entity.MemberCandidate;

import java.util.List;

/**
 * 候选成员服务接口
 * 
 * @author KEYKB
 */
public interface MemberCandidateService extends IService<MemberCandidate> {
    /**
     * 添加候选成员
     *
     * @param candidateDTO 候选成员信息
     * @return 新增的候选成员信息
     */
    MemberCandidateVO addCandidate(MemberCandidateDTO candidateDTO);

    /**
     * 更新候选成员信息
     *
     * @param candidateDTO 候选成员信息
     * @return 更新后的候选成员信息
     */
    MemberCandidateVO updateCandidate(MemberCandidateDTO candidateDTO);

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
    List<MemberCandidateVO> listCandidates(MemberCandidateDTO queryDTO);

    /**
     * 分页查询候选成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    PageVO<MemberCandidateVO> pageCandidate(PageDTO<MemberCandidateDTO> pageDTO);

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
