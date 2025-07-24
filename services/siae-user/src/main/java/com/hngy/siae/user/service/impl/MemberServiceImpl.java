package com.hngy.siae.user.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.MemberDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.entity.Member;
import com.hngy.siae.user.entity.MemberCandidate;
import com.hngy.siae.user.mapper.MemberMapper;
import com.hngy.siae.user.service.MemberCandidateService;
import com.hngy.siae.user.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 正式成员服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl
        extends ServiceImpl<MemberMapper, Member>
        implements MemberService {

    private final MemberCandidateService memberCandidateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberVO addMemberFromCandidate(Long candidateId, Long positionId) {
        // 获取候选成员信息
        MemberCandidate candidate = memberCandidateService.getById(candidateId);
        AssertUtils.notNull(candidate, UserResultCodeEnum.MEMBER_CANDIDATE_NOT_FOUND);
        
        // 检查候选成员状态
        AssertUtils.isTrue(candidate.getStatus() == 1, UserResultCodeEnum.MEMBER_CANDIDATE_INACTIVE);
        
        // 检查是否已经存在相同用户的正式成员
        boolean exists = lambdaQuery()
                .eq(Member::getUserId, candidate.getUserId())
                .eq(Member::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.MEMBER_ALREADY_EXISTS);

        // 创建正式成员
        Member member = Member.builder()
                .userId(candidate.getUserId())
                .departmentId(candidate.getDepartmentId())
                .studentId(candidate.getStudentId())
                .positionId(positionId) // 职位的添加
                .status(1) // 默认设置为“在校”
                .isDeleted(0)
                .joinDate(candidate.getCreatedAt().toLocalDate()) // 设置加入日期为候选成员创建日期（后期如需改为当前日期或其他来源，请修改此处）
                .build();

        save(member);

        // 逻辑删除候选成员
        candidate.setIsDeleted(1);
        memberCandidateService.updateById(candidate);
        
        // 返回成员信息
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    @Override
    public MemberVO updateMember(MemberDTO memberDTO) {
        // 检查成员是否存在
        Member member = getById(memberDTO.getId());
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        
        // 保护字段不被修改
        BeanConvertUtil.to(memberDTO, member, "id", "userId", "isDeleted");
        updateById(member);
        
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    @Override
    public MemberVO getMemberById(Long id) {
        Member member = getById(id);
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    @Override
    public MemberVO getMemberByUserId(Long userId) {
        Member member = lambdaQuery()
                .eq(Member::getUserId, userId)
                .eq(Member::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    @Override
    public List<MemberVO> listMembers(MemberDTO queryDTO) {
        LambdaQueryWrapper<Member> wrapper = createQueryWrapper(queryDTO);
        List<Member> members = list(wrapper);
        return BeanConvertUtil.toList(members, MemberVO.class);
    }

    @Override
    public PageVO<MemberVO> pageMember(PageDTO<MemberDTO> pageDTO) {
        LambdaQueryWrapper<Member> wrapper = createQueryWrapper(pageDTO.getParams());
        Page<Member> page = page(PageConvertUtil.toPage(pageDTO), wrapper);
        return PageConvertUtil.convert(page, MemberVO.class);
    }

    @Override
    public boolean deleteMember(Long id) {
        Member member = getById(id);
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        
        member.setIsDeleted(1);
        return updateById(member);
    }

    /**
     * 构建查询条件
     *
     * @param queryDTO 查询条件DTO
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<Member> createQueryWrapper(MemberDTO queryDTO) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        // 默认只查询未删除的数据
        wrapper.eq(Member::getIsDeleted, 0);

        if (queryDTO == null) {
            return wrapper;
        }

        // 条件拼接，使用 Optional + Lambda 精简写法
        wrapper.eq(queryDTO.getUserId() != null, Member::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getDepartmentId() != null, Member::getDepartmentId, queryDTO.getDepartmentId())
                .eq(queryDTO.getPositionId() != null, Member::getPositionId, queryDTO.getPositionId())
                .eq(StringUtils.hasText(queryDTO.getStudentId()), Member::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getStatus() != null, Member::getStatus, queryDTO.getStatus());

        return wrapper;
    }

} 