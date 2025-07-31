package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.MemberCreateDTO;
import com.hngy.siae.user.dto.request.MemberQueryDTO;
import com.hngy.siae.user.dto.request.MemberUpdateDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.entity.Member;
import com.hngy.siae.user.entity.MemberCandidate;
import com.hngy.siae.user.mapper.MemberMapper;
import com.hngy.siae.user.service.MemberCandidateService;
import com.hngy.siae.user.service.MemberService;
import com.hngy.siae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 正式成员服务实现类
 * <p>
 * 提供正式成员的增删改查功能，包括创建、更新、查询和删除正式成员信息。
 * 支持分页查询和条件查询，支持按部门、职位、状态等条件筛选。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl
        extends ServiceImpl<MemberMapper, Member>
        implements MemberService {

    private final MemberCandidateService memberCandidateService;
    private final UserService userService;

    /**
     * 创建正式成员
     *
     * @param memberCreateDTO 正式成员创建参数
     * @return 创建成功的正式成员信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberVO createMember(MemberCreateDTO memberCreateDTO) {
        // 检查用户是否存在
        userService.assertUserExists(memberCreateDTO.getUserId());

        // 检查学号是否已存在
        if (StrUtil.isNotBlank(memberCreateDTO.getStudentId())) {
            boolean exists = lambdaQuery()
                    .eq(Member::getStudentId, memberCreateDTO.getStudentId())
                    .eq(Member::getIsDeleted, 0)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 检查用户是否已经是正式成员
        boolean userExists = lambdaQuery()
                .eq(Member::getUserId, memberCreateDTO.getUserId())
                .eq(Member::getIsDeleted, 0)
                .exists();
        AssertUtils.isFalse(userExists, UserResultCodeEnum.MEMBER_ALREADY_EXISTS);

        // 创建正式成员
        Member member = BeanConvertUtil.to(memberCreateDTO, Member.class);
        member.setIsDeleted(0);
        save(member);

        return BeanConvertUtil.to(member, MemberVO.class);
    }

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

    /**
     * 更新正式成员信息
     *
     * @param memberUpdateDTO 正式成员更新参数
     * @return 更新后的正式成员信息
     */
    @Override
    public MemberVO updateMember(MemberUpdateDTO memberUpdateDTO) {
        // 检查成员是否存在
        Member member = getById(memberUpdateDTO.getId());
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);

        // 检查学号是否已被其他成员使用
        if (StrUtil.isNotBlank(memberUpdateDTO.getStudentId()) &&
                !memberUpdateDTO.getStudentId().equals(member.getStudentId())) {
            boolean exists = lambdaQuery()
                    .eq(Member::getStudentId, memberUpdateDTO.getStudentId())
                    .ne(Member::getId, memberUpdateDTO.getId())
                    .eq(Member::getIsDeleted, 0)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 保护字段不被修改
        BeanConvertUtil.to(memberUpdateDTO, member, "id", "userId", "isDeleted");
        updateById(member);

        return BeanConvertUtil.to(member, MemberVO.class);
    }

    /**
     * 根据ID获取正式成员信息
     *
     * @param id 正式成员ID
     * @return 正式成员详细信息
     */
    @Override
    public MemberVO getMemberById(Long id) {
        Member member = getById(id);
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    /**
     * 根据用户ID获取正式成员信息
     *
     * @param userId 用户ID
     * @return 正式成员详细信息
     */
    @Override
    public MemberVO getMemberByUserId(Long userId) {
        Member member = lambdaQuery()
                .eq(Member::getUserId, userId)
                .eq(Member::getIsDeleted, 0)
                .one();
        AssertUtils.notNull(member, UserResultCodeEnum.MEMBER_NOT_FOUND);
        return BeanConvertUtil.to(member, MemberVO.class);
    }

    /**
     * 动态条件查询正式成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的正式成员列表
     */
    @Override
    public List<MemberVO> listMembers(MemberQueryDTO queryDTO) {
        LambdaQueryWrapper<Member> wrapper = createQueryWrapper(queryDTO);
        List<Member> members = list(wrapper);
        return BeanConvertUtil.toList(members, MemberVO.class);
    }

    /**
     * 分页查询正式成员列表
     *
     * @param pageDTO 分页参数和查询条件
     * @return 分页结果
     */
    @Override
    public PageVO<MemberVO> listMembersByPage(PageDTO<MemberQueryDTO> pageDTO) {
        LambdaQueryWrapper<Member> wrapper = createQueryWrapper(pageDTO.getParams());
        Page<Member> page = page(PageConvertUtil.toPage(pageDTO), wrapper);
        return PageConvertUtil.convert(page, MemberVO.class);
    }

    /**
     * 根据ID删除正式成员（逻辑删除）
     *
     * @param id 正式成员ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
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
    private LambdaQueryWrapper<Member> createQueryWrapper(MemberQueryDTO queryDTO) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        // 默认只查询未删除的数据
        wrapper.eq(Member::getIsDeleted, 0);

        if (queryDTO == null) {
            return wrapper;
        }

        // 条件拼接
        wrapper.eq(queryDTO.getId() != null, Member::getId, queryDTO.getId())
                .eq(queryDTO.getUserId() != null, Member::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getDepartmentId() != null, Member::getDepartmentId, queryDTO.getDepartmentId())
                .eq(queryDTO.getPositionId() != null, Member::getPositionId, queryDTO.getPositionId())
                .eq(StrUtil.isNotBlank(queryDTO.getStudentId()), Member::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getStatus() != null, Member::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getJoinDateStart() != null, Member::getJoinDate, queryDTO.getJoinDateStart())
                .le(queryDTO.getJoinDateEnd() != null, Member::getJoinDate, queryDTO.getJoinDateEnd());

        return wrapper.orderByDesc(Member::getCreatedAt);
    }

} 