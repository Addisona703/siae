package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.MemberCandidateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.entity.MemberCandidate;
import com.hngy.siae.user.mapper.MemberCandidateMapper;
import com.hngy.siae.user.service.MemberCandidateService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 候选成员服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class MemberCandidateServiceImpl
        extends ServiceImpl<MemberCandidateMapper, MemberCandidate>
        implements MemberCandidateService {

    @Override
    public MemberCandidateVO addCandidate(MemberCandidateDTO candidateDTO) {
        // 检查是否已经存在相同用户的候选成员
        boolean exists = lambdaQuery()
                .eq(MemberCandidate::getUserId, candidateDTO.getUserId())
                .eq(MemberCandidate::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.MEMBER_CANDIDATE_ALREADY_EXISTS);
        
        // 检查是否已经存在相同学号的候选成员
        if (StringUtils.hasText(candidateDTO.getStudentId())) {
            boolean studentIdExists = lambdaQuery()
                    .eq(MemberCandidate::getStudentId, candidateDTO.getStudentId())
                    .eq(MemberCandidate::getIsDeleted, 0)
                    .exists();
            AssertUtils.isFalse(studentIdExists, UserResultCodeEnum.MEMBER_CANDIDATE_ALREADY_EXISTS);
        }
        
        // 创建候选成员
        MemberCandidate candidate = BeanConvertUtil.to(candidateDTO, MemberCandidate.class);
        candidate.setIsDeleted(0);
        candidate.setStatus(1);  // 默认状态为1，表示活跃状态
        save(candidate);
        
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    @Override
    public MemberCandidateVO updateCandidate(MemberCandidateDTO candidateDTO) {
        // 检查候选成员是否存在
        MemberCandidate candidate = getById(candidateDTO.getId());
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        
        // 检查是否修改学号，如果修改了学号，需要检查新学号是否已被使用
        if (StringUtils.hasText(candidateDTO.getStudentId()) && 
                !candidateDTO.getStudentId().equals(candidate.getStudentId())) {
            boolean studentIdExists = lambdaQuery()
                    .eq(MemberCandidate::getStudentId, candidateDTO.getStudentId())
                    .eq(MemberCandidate::getIsDeleted, 0)
                    .ne(MemberCandidate::getId, candidateDTO.getId())
                    .exists();
            AssertUtils.isFalse(studentIdExists, UserResultCodeEnum.MEMBER_CANDIDATE_ALREADY_EXISTS);
        }
        
        // 保护字段不被修改
        BeanConvertUtil.to(candidateDTO, candidate, "id", "userId", "isDeleted");
        updateById(candidate);
        
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    @Override
    public MemberCandidateVO getCandidateById(Long id) {
        MemberCandidate candidate = getById(id);
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    @Override
    public MemberCandidateVO getCandidateByUserId(Long userId) {
        MemberCandidate candidate = lambdaQuery()
                .eq(MemberCandidate::getUserId, userId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    @Override
    public MemberCandidateVO getCandidateByStudentId(String studentId) {
        MemberCandidate candidate = lambdaQuery()
                .eq(MemberCandidate::getStudentId, studentId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        return BeanConvertUtil.to(candidate, MemberCandidateVO.class);
    }

    @Override
    public List<MemberCandidateVO> listCandidatesByDepartment(Long departmentId) {
        List<MemberCandidate> candidates = lambdaQuery()
                .eq(MemberCandidate::getDepartmentId, departmentId)
                .eq(MemberCandidate::getIsDeleted, 0)
                .list();
        return BeanConvertUtil.toList(candidates, MemberCandidateVO.class);
    }

    @Override
    public List<MemberCandidateVO> listCandidates(MemberCandidateDTO queryDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = createQueryWrapper(queryDTO);
        List<MemberCandidate> candidates = list(wrapper);
        return BeanConvertUtil.toList(candidates, MemberCandidateVO.class);
    }

    @Override
    public PageVO<MemberCandidateVO> pageCandidate(PageDTO<MemberCandidateDTO> pageDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = createQueryWrapper(pageDTO.getParams());
        Page<MemberCandidate> page = page(PageConvertUtil.toPage(pageDTO), wrapper);
        return PageConvertUtil.convert(page, MemberCandidateVO.class);
    }

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
    private LambdaQueryWrapper<MemberCandidate> createQueryWrapper(MemberCandidateDTO queryDTO) {
        LambdaQueryWrapper<MemberCandidate> wrapper = new LambdaQueryWrapper<>();

        // 默认只查询未删除的
        wrapper.eq(MemberCandidate::getIsDeleted, 0);

        if (queryDTO == null) {
            return wrapper;
        }

        wrapper.eq(queryDTO.getUserId() != null, MemberCandidate::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getDepartmentId() != null, MemberCandidate::getDepartmentId, queryDTO.getDepartmentId())
                .eq(queryDTO.getStatus() != null, MemberCandidate::getStatus, queryDTO.getStatus())
                .eq(StringUtils.hasText(queryDTO.getStudentId()), MemberCandidate::getStudentId, queryDTO.getStudentId());

        return wrapper;
    }

} 