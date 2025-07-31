package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.MemberCandidateCreateDTO;
import com.hngy.siae.user.dto.request.MemberCandidateQueryDTO;
import com.hngy.siae.user.dto.request.MemberCandidateUpdateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.entity.MemberCandidate;
import com.hngy.siae.user.mapper.MemberCandidateMapper;
import com.hngy.siae.user.service.MemberCandidateService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;
import com.hngy.siae.user.service.UserService;

import java.util.List;

/**
 * 候选成员服务实现类
 * <p>
 * 提供候选成员的增删改查功能，包括创建、更新、查询和删除候选成员信息。
 * 支持分页查询和条件查询，支持按部门、状态等条件筛选。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class MemberCandidateServiceImpl
        extends ServiceImpl<MemberCandidateMapper, MemberCandidate>
        implements MemberCandidateService {

    private final UserService userService;

    /**
     * 创建候选成员
     *
     * @param memberCandidateCreateDTO 候选成员创建参数
     * @return 创建成功的候选成员信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberCandidateVO createMemberCandidate(MemberCandidateCreateDTO memberCandidateCreateDTO) {
        // 检查用户是否存在
        userService.assertUserExists(memberCandidateCreateDTO.getUserId());

        // 检查是否已经存在相同用户的候选成员
        boolean exists = lambdaQuery()
                .eq(MemberCandidate::getUserId, memberCandidateCreateDTO.getUserId())
                .eq(MemberCandidate::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.MEMBER_CANDIDATE_ALREADY_EXISTS);

        // 检查是否已经存在相同学号的候选成员
        if (StrUtil.isNotBlank(memberCandidateCreateDTO.getStudentId())) {
            boolean studentIdExists = lambdaQuery()
                    .eq(MemberCandidate::getStudentId, memberCandidateCreateDTO.getStudentId())
                    .eq(MemberCandidate::getIsDeleted, 0)
                    .exists();
            AssertUtils.isFalse(studentIdExists, UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 创建候选成员
        MemberCandidate memberCandidate = BeanConvertUtil.to(memberCandidateCreateDTO, MemberCandidate.class);
        memberCandidate.setIsDeleted(0);
        save(memberCandidate);

        return BeanConvertUtil.to(memberCandidate, MemberCandidateVO.class);
    }

    /**
     * 添加候选成员
     *
     * @param candidateDTO 候选成员信息
     * @return 新增的候选成员信息
     */
    @Override
    public MemberCandidateVO addCandidate(MemberCandidateCreateDTO candidateDTO) {
        // 检查用户是否存在
        userService.assertUserExists(candidateDTO.getUserId());

        // 检查是否已经存在相同用户的候选成员
        boolean exists = lambdaQuery()
                .eq(MemberCandidate::getUserId, candidateDTO.getUserId())
                .eq(MemberCandidate::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.MEMBER_CANDIDATE_ALREADY_EXISTS);

        // 检查是否已经存在相同学号的候选成员
        if (StrUtil.isNotBlank(candidateDTO.getStudentId())) {
            boolean studentIdExists = lambdaQuery()
                    .eq(MemberCandidate::getStudentId, candidateDTO.getStudentId())
                    .eq(MemberCandidate::getIsDeleted, 0)
                    .exists();
            AssertUtils.isFalse(studentIdExists, UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 创建候选成员
        MemberCandidate candidate = BeanConvertUtil.to(candidateDTO, MemberCandidate.class);
        candidate.setIsDeleted(0);
        candidate.setStatus(1);  // 默认状态为1，表示活跃状态
        save(candidate);

        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    /**
     * 更新候选成员信息
     *
     * @param candidateDTO 候选成员更新参数
     * @return 更新后的候选成员信息
     */
    @Override
    public MemberCandidateVO updateCandidate(MemberCandidateUpdateDTO candidateDTO) {
        // 检查候选成员是否存在
        MemberCandidate candidate = getById(candidateDTO.getId());
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);

        // 检查是否修改学号，如果修改了学号，需要检查新学号是否已被使用
        if (StrUtil.isNotBlank(candidateDTO.getStudentId()) &&
                !candidateDTO.getStudentId().equals(candidate.getStudentId())) {
            boolean studentIdExists = lambdaQuery()
                    .eq(MemberCandidate::getStudentId, candidateDTO.getStudentId())
                    .eq(MemberCandidate::getIsDeleted, 0)
                    .ne(MemberCandidate::getId, candidateDTO.getId())
                    .exists();
            AssertUtils.isFalse(studentIdExists, UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 保护字段不被修改
        BeanConvertUtil.to(candidateDTO, candidate, "id", "userId", "isDeleted");
        updateById(candidate);

        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    /**
     * 根据ID获取候选成员信息
     *
     * @param id 候选成员ID
     * @return 候选成员详细信息
     */
    @Override
    public MemberCandidateVO getCandidateById(Long id) {
        MemberCandidate candidate = getById(id);
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    /**
     * 根据用户ID获取候选成员信息
     *
     * @param userId 用户ID
     * @return 候选成员详细信息
     */
    @Override
    public MemberCandidateVO getCandidateByUserId(Long userId) {
        MemberCandidate candidate = lambdaQuery()
                .eq(MemberCandidate::getUserId, userId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    /**
     * 根据学号获取候选成员信息
     *
     * @param studentId 学号
     * @return 候选成员详细信息
     */
    @Override
    public MemberCandidateVO getCandidateByStudentId(String studentId) {
        MemberCandidate candidate = lambdaQuery()
                .eq(MemberCandidate::getStudentId, studentId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    /**
     * 根据部门ID获取候选成员列表
     *
     * @param departmentId 部门ID
     * @return 候选成员列表
     */
    @Override
    public List<MemberCandidateVO> listCandidatesByDepartment(Long departmentId) {
        List<MemberCandidate> candidates = lambdaQuery()
                .eq(MemberCandidate::getDepartmentId, departmentId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .list();
        return BeanConvertUtil.toList(candidates, MemberCandidateVO.class);
    }

    /**
     * 动态条件查询候选成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的候选成员列表
     */
    @Override
    public List<MemberCandidateVO> listCandidates(MemberCandidateQueryDTO queryDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = createQueryWrapper(queryDTO);
        List<MemberCandidate> candidates = list(wrapper);
        return BeanConvertUtil.toList(candidates, MemberCandidateVO.class);
    }

    /**
     * 分页查询候选成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    @Override
    public PageVO<MemberCandidateVO> listCandidatesByPage(PageDTO<MemberCandidateQueryDTO> pageDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = createQueryWrapper(pageDTO.getParams());
        Page<MemberCandidate> page = page(PageConvertUtil.toPage(pageDTO), wrapper);
        return PageConvertUtil.convert(page, MemberCandidateVO.class);
    }

    /**
     * 根据ID删除候选成员（逻辑删除）
     *
     * @param id 候选成员ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteCandidate(Long id) {
        MemberCandidate candidate = getById(id);
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);

        // 修改状态为0而不是直接删除
        candidate.setStatus(0);
        return updateById(candidate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteCandidates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 批量更新状态为 0
        return lambdaUpdate()
                .in(MemberCandidate::getId, ids)
                .set(MemberCandidate::getStatus, 0)
                .update();
    }

    /**
     * 构建候选成员的查询条件
     *
     * @param queryDTO 查询条件DTO
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<MemberCandidate> createQueryWrapper(MemberCandidateQueryDTO queryDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = new LambdaQueryWrapper<>();

        // 根据是否包含已删除记录决定查询条件
        if (queryDTO == null || !Boolean.TRUE.equals(queryDTO.getIncludeDeleted())) {
            wrapper.eq(MemberCandidate::getIsDeleted, 0);
        }

        if (queryDTO == null) {
            return wrapper.orderByDesc(MemberCandidate::getCreatedAt);
        }

        wrapper.eq(queryDTO.getId() != null, MemberCandidate::getId, queryDTO.getId())
                .eq(queryDTO.getUserId() != null, MemberCandidate::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getDepartmentId() != null, MemberCandidate::getDepartmentId, queryDTO.getDepartmentId())
                .eq(queryDTO.getStatus() != null, MemberCandidate::getStatus, queryDTO.getStatus())
                .eq(StrUtil.isNotBlank(queryDTO.getStudentId()), MemberCandidate::getStudentId, queryDTO.getStudentId())
                .ge(queryDTO.getApplyDateStart() != null, MemberCandidate::getCreatedAt, queryDTO.getApplyDateStart())
                .le(queryDTO.getApplyDateEnd() != null, MemberCandidate::getCreatedAt, queryDTO.getApplyDateEnd());

        return wrapper.orderByDesc(MemberCandidate::getCreatedAt);
    }

} 