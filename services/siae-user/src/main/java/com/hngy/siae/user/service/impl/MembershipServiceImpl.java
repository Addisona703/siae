package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.MembershipCreateDTO;
import com.hngy.siae.user.dto.request.MembershipPromoteDTO;
import com.hngy.siae.user.dto.request.MembershipQueryDTO;
import com.hngy.siae.user.dto.request.MembershipUpdateDTO;
import com.hngy.siae.user.dto.response.MembershipDetailVO;
import com.hngy.siae.user.dto.response.MembershipVO;
import com.hngy.siae.user.entity.Membership;
import com.hngy.siae.user.enums.LifecycleStatusEnum;
import com.hngy.siae.user.feign.MediaFeignClient;
import com.hngy.siae.user.feign.dto.BatchUrlRequest;
import com.hngy.siae.user.feign.dto.BatchUrlResponse;
import com.hngy.siae.user.mapper.MembershipMapper;
import com.hngy.siae.user.service.MembershipService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MediaFeignClient mediaFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCandidate(MembershipCreateDTO createDTO) {
        log.info("创建候选成员，用户ID: {}", createDTO.getUserId());

        // 检查用户是否已经是成员
        Membership existing = membershipMapper.selectByUserId(createDTO.getUserId());
        AssertUtils.isNull(existing, UserResultCodeEnum.MEMBERSHIP_ALREADY_EXISTS);

        // 创建候选成员
        Membership membership = new Membership();
        membership.setUserId(createDTO.getUserId());
        membership.setHeadshotFileId(createDTO.getHeadshotFileId());
        membership.setLifecycleStatus(LifecycleStatusEnum.CANDIDATE);
        membership.setJoinDate(null); // 候选成员暂无加入日期

        save(membership);
        log.info("候选成员创建成功，成员ID: {}", membership.getId());
        return membership.getId();
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
    public MembershipVO getMembershipById(Long id) {
        Membership membership = getById(id);
        MembershipVO membershipVO = BeanConvertUtil.to(membership, MembershipVO.class);
        
        // 填充大头照URL
        enrichMembershipWithHeadshotUrl(membershipVO);
        
        return membershipVO;
    }

    @Override
    public MembershipVO getMembershipByUserId(Long userId) {
        Membership membership = membershipMapper.selectByUserId(userId);
        MembershipVO membershipVO = BeanConvertUtil.to(membership, MembershipVO.class);
        
        // 填充大头照URL
        enrichMembershipWithHeadshotUrl(membershipVO);
        
        return membershipVO;
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
            Result<String> result = mediaFeignClient.getFileUrl(membershipVO.getHeadshotFileId(), 86400);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                membershipVO.setHeadshotUrl(result.getData());
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
            BatchUrlRequest request = BatchUrlRequest.builder()
                    .fileIds(fileIds)
                    .expirySeconds(86400) // 24小时
                    .build();

            Result<BatchUrlResponse> result = mediaFeignClient.batchGetFileUrls(request);
            
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData().getUrls();
            } else {
                log.warn("Media service returned unsuccessful result: {}", result);
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            log.error("Failed to call media service for batch URLs", e);
            return Collections.emptyMap();
        }
    }
}
