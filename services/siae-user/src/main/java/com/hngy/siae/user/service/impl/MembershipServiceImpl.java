package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.user.dto.request.MembershipCreateDTO;
import com.hngy.siae.user.dto.request.MembershipPromoteDTO;
import com.hngy.siae.user.dto.request.MembershipQueryDTO;
import com.hngy.siae.user.dto.request.MembershipUpdateDTO;
import com.hngy.siae.user.dto.response.MembershipDetailVO;
import com.hngy.siae.user.dto.response.MembershipVO;
import com.hngy.siae.user.entity.MemberDepartment;
import com.hngy.siae.user.entity.MemberPosition;
import com.hngy.siae.user.entity.Membership;
import com.hngy.siae.user.enums.LifecycleStatusEnum;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.user.mapper.MemberDepartmentMapper;
import com.hngy.siae.user.mapper.MemberPositionMapper;
import com.hngy.siae.user.mapper.MembershipMapper;
import com.hngy.siae.user.service.MembershipService;
import com.hngy.siae.core.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成员统一服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl
        extends ServiceImpl<MembershipMapper, Membership>
        implements MembershipService {

    private final MembershipMapper membershipMapper;
    private final MemberDepartmentMapper memberDepartmentMapper;
    private final MemberPositionMapper memberPositionMapper;
    private final MediaFeignClient mediaFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCandidate(MembershipCreateDTO createDTO) {
        log.info("创建候选成员，用户ID: {}, 部门ID: {}, 职位ID: {}", 
                createDTO.getUserId(), createDTO.getDepartmentId(), createDTO.getPositionId());

        // 检查用户是否已经是成员（包含已删除的记录）
        Membership existing = membershipMapper.selectByUserId(createDTO.getUserId());
        
        Membership membership;
        if (existing != null) {
            // 检查是否被开除过，被开除的用户不允许再次申请
            AssertUtils.isFalse(LifecycleStatusEnum.isExpelled(existing.getLifecycleStatus().getCode()),
                    UserResultCodeEnum.MEMBERSHIP_EXPELLED);
            
            // 如果记录存在且未删除，则不允许重复申请
            AssertUtils.isTrue(existing.getIsDeleted() == 1, UserResultCodeEnum.MEMBERSHIP_ALREADY_EXISTS);
            
            // 如果是已删除的记录，恢复并更新
            membership = existing;
            membership.setIsDeleted(0);
            membership.setHeadshotFileId(createDTO.getHeadshotFileId());
            membership.setLifecycleStatus(LifecycleStatusEnum.PENDING);
            membership.setJoinDate(null); // 待审核状态暂无加入日期
            updateById(membership);
            log.info("恢复已删除的成员记录，成员ID: {}", membership.getId());
        } else {
            // 创建新的待审核成员
            membership = new Membership();
            membership.setUserId(createDTO.getUserId());
            membership.setHeadshotFileId(createDTO.getHeadshotFileId());
            membership.setLifecycleStatus(LifecycleStatusEnum.PENDING);
            membership.setJoinDate(null); // 待审核状态暂无加入日期
            save(membership);
        }

        // 检查部门关联是否已存在
        MemberDepartment existingDept = memberDepartmentMapper.selectOne(
                new LambdaQueryWrapper<MemberDepartment>()
                        .eq(MemberDepartment::getMembershipId, membership.getId())
                        .eq(MemberDepartment::getDepartmentId, createDTO.getDepartmentId())
        );
        
        if (existingDept == null) {
            // 创建部门关联
            MemberDepartment memberDepartment = new MemberDepartment();
            memberDepartment.setMembershipId(membership.getId());
            memberDepartment.setDepartmentId(createDTO.getDepartmentId());
            memberDepartment.setJoinDate(LocalDate.now());
            memberDepartment.setHasPosition(1); // 有职位
            memberDepartmentMapper.insert(memberDepartment);
        } else {
            // 更新部门关联
            existingDept.setJoinDate(LocalDate.now());
            existingDept.setHasPosition(1);
            memberDepartmentMapper.updateById(existingDept);
        }

        // 检查职位关联是否已存在
        MemberPosition existingPos = memberPositionMapper.selectOne(
                new LambdaQueryWrapper<MemberPosition>()
                        .eq(MemberPosition::getMembershipId, membership.getId())
                        .eq(MemberPosition::getPositionId, createDTO.getPositionId())
                        .isNull(MemberPosition::getEndDate) // 只检查未结束的职位
        );
        
        if (existingPos == null) {
            // 创建职位关联
            MemberPosition memberPosition = new MemberPosition();
            memberPosition.setMembershipId(membership.getId());
            memberPosition.setPositionId(createDTO.getPositionId());
            memberPosition.setDepartmentId(createDTO.getDepartmentId());
            memberPosition.setStartDate(LocalDate.now());
            memberPositionMapper.insert(memberPosition);
        } else {
            // 更新职位关联
            existingPos.setDepartmentId(createDTO.getDepartmentId());
            existingPos.setStartDate(LocalDate.now());
            memberPositionMapper.updateById(existingPos);
        }

        log.info("成员申请创建成功（待审核），成员ID: {}", membership.getId());
        return membership.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveCandidate(Long id) {
        log.info("审核通过，成员ID: {}", id);

        Membership membership = getById(id);
        AssertUtils.notNull(membership, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);
        AssertUtils.isTrue(LifecycleStatusEnum.isPending(membership.getLifecycleStatus().getCode()),
                UserResultCodeEnum.MEMBERSHIP_STATUS_INVALID);

        // 更新为候选成员
        membership.setLifecycleStatus(LifecycleStatusEnum.CANDIDATE);
        membership.setJoinDate(LocalDate.now());

        boolean success = updateById(membership);
        if (success) {
            log.info("审核通过成功，成员ID: {}", id);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectCandidate(Long id) {
        log.info("审核拒绝，成员ID: {}", id);

        Membership membership = getById(id);
        AssertUtils.notNull(membership, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);
        AssertUtils.isTrue(LifecycleStatusEnum.isPending(membership.getLifecycleStatus().getCode()),
                UserResultCodeEnum.MEMBERSHIP_STATUS_INVALID);

        // 删除关联的部门记录
        memberDepartmentMapper.delete(
                new LambdaQueryWrapper<MemberDepartment>()
                        .eq(MemberDepartment::getMembershipId, id)
        );
        
        // 删除关联的职位记录
        memberPositionMapper.delete(
                new LambdaQueryWrapper<MemberPosition>()
                        .eq(MemberPosition::getMembershipId, id)
        );
        
        // 直接删除成员记录，允许用户下次重新申请
        boolean success = removeById(id);
        if (success) {
            log.info("审核拒绝成功，已删除成员记录，成员ID: {}", id);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean expelMember(Long id) {
        log.info("强制退会，成员ID: {}", id);

        Membership membership = getById(id);
        AssertUtils.notNull(membership, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);

        // 更新为已开除
        membership.setLifecycleStatus(LifecycleStatusEnum.EXPELLED);

        boolean success = updateById(membership);
        if (success) {
            log.info("强制退会成功，成员ID: {}", id);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean promoteToOfficial(MembershipPromoteDTO promoteDTO) {
        log.info("候选成员转正，成员ID: {}", promoteDTO.getId());

        Membership membership = getById(promoteDTO.getId());
        AssertUtils.notNull(membership, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);
        AssertUtils.isTrue(LifecycleStatusEnum.isCandidate(membership.getLifecycleStatus().getCode()), 
                UserResultCodeEnum.MEMBERSHIP_NOT_CANDIDATE);

        // 更新为正式成员
        membership.setLifecycleStatus(LifecycleStatusEnum.OFFICIAL);
        membership.setJoinDate(promoteDTO.getJoinDate());
        if (promoteDTO.getHeadshotFileId() != null) {
            membership.setHeadshotFileId(promoteDTO.getHeadshotFileId());
        }

        boolean success = updateById(membership);
        if (success) {
            log.info("候选成员转正成功，成员ID: {}, 加入日期: {}", 
                    promoteDTO.getId(), promoteDTO.getJoinDate());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMembership(MembershipUpdateDTO updateDTO) {
        log.info("更新成员信息，成员ID: {}", updateDTO.getId());

        Membership membership = getById(updateDTO.getId());
        AssertUtils.notNull(membership, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);

        if (updateDTO.getHeadshotFileId() != null) {
            membership.setHeadshotFileId(updateDTO.getHeadshotFileId());
        }

        return updateById(membership);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMembership(Long id) {
        log.info("删除成员，成员ID: {}", id);
        return removeById(id);
    }

    @Override
    public MembershipDetailVO getMembershipById(Long id) {
        // 复用getMembershipDetailById的逻辑，返回完整的成员详情
        return getMembershipDetailById(id);
    }

    @Override
    public MembershipDetailVO getMembershipByUserId(Long userId) {
        log.info("根据用户ID查询成员详情，用户ID: {}", userId);
        
        // 查询成员基本信息
        MembershipDetailVO detail = membershipMapper.selectMembershipDetailByUserId(userId);
        AssertUtils.notNull(detail, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);
        
        // 查询部门列表
        detail.setDepartments(membershipMapper.selectDepartmentsByMembershipId(detail.getId()));
        
        // 查询职位列表
        detail.setPositions(membershipMapper.selectPositionsByMembershipId(detail.getId()));
        
        // 查询荣誉成就列表
        detail.setAwards(membershipMapper.selectAwardsByUserId(userId));
        
        // 填充头像和大头照URL
        enrichMembershipDetailWithMediaUrls(detail);
        
        return detail;
    }

    @Override
    public MembershipDetailVO getMembershipDetailById(Long id) {
        log.info("查询成员详情，成员ID: {}", id);
        
        // 查询成员基本信息
        MembershipDetailVO detail = membershipMapper.selectMembershipDetailById(id);
        AssertUtils.notNull(detail, UserResultCodeEnum.MEMBERSHIP_NOT_FOUND);
        
        // 查询部门列表
        detail.setDepartments(membershipMapper.selectDepartmentsByMembershipId(id));
        
        // 查询职位列表
        detail.setPositions(membershipMapper.selectPositionsByMembershipId(id));
        
        // 查询荣誉成就列表
        detail.setAwards(membershipMapper.selectAwardsByUserId(detail.getUserId()));
        
        // 填充头像和大头照URL
        enrichMembershipDetailWithMediaUrls(detail);
        
        return detail;
    }

    @Override
    public PageVO<MembershipVO> pageMemberships(PageDTO<MembershipQueryDTO> pageDTO) {
        // 确保查询参数不为空
        MembershipQueryDTO query = pageDTO.getParams();
        if (query == null) {
            query = new MembershipQueryDTO();
            pageDTO.setParams(query);
        }
        
        // 默认排除待审核成员（PENDING=0），除非显式指定了lifecycleStatus
        if (query.getLifecycleStatus() == null) {
            query.setExcludePending(true);
        }
        
        Page<Membership> page = PageConvertUtil.toPage(pageDTO);
        Page<Membership> resultPage = membershipMapper.selectMembershipPage(page, pageDTO.getParams());
        PageVO<MembershipVO> pageVO = PageConvertUtil.convert(resultPage, MembershipVO.class);
        
        // 批量填充大头照URL
        enrichMembershipsWithHeadshotUrls(pageVO.getRecords());
        
        return pageVO;
    }

    @Override
    public boolean isMember(Long userId) {
        Membership membership = membershipMapper.selectByUserId(userId);
        return membership != null;
    }

    @Override
    public boolean isCandidate(Long userId) {
        Membership membership = membershipMapper.selectByUserId(userId);
        return membership != null && 
               LifecycleStatusEnum.isCandidate(membership.getLifecycleStatus().getCode());
    }

    @Override
    public boolean isOfficialMember(Long userId) {
        Membership membership = membershipMapper.selectByUserId(userId);
        return membership != null && 
               LifecycleStatusEnum.isOfficial(membership.getLifecycleStatus().getCode());
    }

    // ==================== 私有辅助方法：Media服务集成 ====================

    /**
     * 为单个成员填充大头照URL
     */
    private void enrichMembershipWithHeadshotUrl(MembershipVO membershipVO) {
        if (membershipVO == null || StrUtil.isBlank(membershipVO.getHeadshotFileId())) {
            return;
        }

        try {
            String url = mediaFeignClient.getFileUrl(membershipVO.getHeadshotFileId(), 86400);
            if (url != null) {
                membershipVO.setHeadshotUrl(url);
            }
        } catch (Exception e) {
            log.warn("Failed to get headshot URL for membership: {}, error: {}", 
                    membershipVO.getId(), e.getMessage());
        }
    }

    /**
     * 为成员列表批量填充大头照URL
     */
    private void enrichMembershipsWithHeadshotUrls(List<MembershipVO> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            return;
        }

        // 收集所有大头照ID
        List<String> headshotIds = memberships.stream()
                .map(MembershipVO::getHeadshotFileId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        if (headshotIds.isEmpty()) {
            return;
        }

        try {
            // 批量获取URL
            Map<String, String> urls = batchGetMediaUrls(headshotIds);

            // 填充到成员对象
            memberships.forEach(membership -> {
                if (StrUtil.isNotBlank(membership.getHeadshotFileId())) {
                    membership.setHeadshotUrl(urls.get(membership.getHeadshotFileId()));
                }
            });

            log.info("Enriched {} memberships with headshot URLs, success: {}/{}", 
                    memberships.size(), urls.size(), headshotIds.size());
        } catch (Exception e) {
            log.error("Failed to batch get headshot URLs", e);
        }
    }

    /**
     * 为成员详情填充媒体URL（头像和大头照）
     */
    private void enrichMembershipDetailWithMediaUrls(MembershipDetailVO detail) {
        List<String> fileIds = new ArrayList<>();
        
        if (StrUtil.isNotBlank(detail.getAvatarFileId())) {
            fileIds.add(detail.getAvatarFileId());
        }
        if (StrUtil.isNotBlank(detail.getHeadshotFileId())) {
            fileIds.add(detail.getHeadshotFileId());
        }

        if (fileIds.isEmpty()) {
            return;
        }

        try {
            Map<String, String> urls = batchGetMediaUrls(fileIds);
            
            if (StrUtil.isNotBlank(detail.getAvatarFileId())) {
                detail.setAvatarUrl(urls.get(detail.getAvatarFileId()));
            }
            if (StrUtil.isNotBlank(detail.getHeadshotFileId())) {
                detail.setHeadshotUrl(urls.get(detail.getHeadshotFileId()));
            }
        } catch (Exception e) {
            log.warn("Failed to get media URLs for membership detail: {}, error: {}", 
                    detail.getId(), e.getMessage());
        }
    }

    /**
     * 批量获取媒体文件URL
     */
    private Map<String, String> batchGetMediaUrls(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(fileIds);
            request.setExpirySeconds(86400); // 24小时

            BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
            
            if (result != null && result.getUrls() != null) {
                return result.getUrls();
            } else {
                log.warn("Media service returned empty result");
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            log.error("Failed to call media service for batch URLs", e);
            return Collections.emptyMap();
        }
    }
}
