package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.content.strategy.audit.AuditHandler;
import com.hngy.siae.content.strategy.audit.AuditHandlerContext;
import com.hngy.siae.content.dto.request.audit.AuditQueryDTO;
import com.hngy.siae.content.entity.AuditLog;
import com.hngy.siae.content.mapper.AuditLogMapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.core.enums.BaseEnum;
import com.hngy.siae.content.dto.request.audit.AuditDTO;
import com.hngy.siae.content.dto.response.audit.AuditVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容审核服务impl
 * 使用策略模式处理不同类型的审核
 * 使用 AuditLog 记录审核历史（Requirements: 4.1）
 *
 * Requirements: 2.6, 4.1
 * 
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditsServiceImpl
        extends ServiceImpl<AuditLogMapper, AuditLog>
        implements AuditsService {

    private final ContentService contentService;
    private final CommentsService commentsService;
    private final AuditHandlerContext auditHandlerContext;
    private final MediaFeignClient mediaFeignClient;


    @Override
    public Long submitAudit(AuditDTO auditDTO) {
        // 1. 检查是否已有待处理的审核请求（查询最新记录的状态）
        AuditLog latestLog = this.getOne(new LambdaQueryWrapper<AuditLog>()
                .eq(AuditLog::getTargetId, auditDTO.getTargetId())
                .eq(AuditLog::getTargetType, auditDTO.getTargetType())
                .orderByDesc(AuditLog::getCreateTime)
                .last("LIMIT 1"));
        
        // 如果存在记录且状态为待审核，则不允许重复提交
        if (latestLog != null && latestLog.getToStatus() == AuditStatusEnum.PENDING.getCode()) {
            AssertUtils.fail(ContentResultCodeEnum.AUDIT_ALREADY_EXISTS);
        }

        // 2. 创建审核记录（使用传入的状态，默认为待审核）
        AuditLog auditLog = new AuditLog();
        auditLog.setTargetId(auditDTO.getTargetId());
        auditLog.setTargetType(auditDTO.getTargetType());
        auditLog.setFromStatus(null); // 新提交时无前置状态
        // 使用传入的状态，如果未指定则默认为待审核
        Integer toStatus = auditDTO.getAuditStatus() != null 
                ? auditDTO.getAuditStatus().getCode() 
                : AuditStatusEnum.PENDING.getCode();
        auditLog.setToStatus(toStatus);
        auditLog.setAuditReason(auditDTO.getAuditReason());
        auditLog.setAuditBy(auditDTO.getAuditBy());
        
        AssertUtils.isTrue(this.save(auditLog), ContentResultCodeEnum.AUDIT_SUBMIT_FAILED);
        return auditLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAudit(Long id, AuditDTO auditDTO) {
        // 1. 获取审核记录
        AuditLog auditLog = this.getById(id);
        AssertUtils.notNull(auditLog, ContentResultCodeEnum.AUDIT_NOT_FOUND);
        AssertUtils.isTrue(auditLog.getToStatus() == AuditStatusEnum.PENDING.getCode(), 
                ContentResultCodeEnum.AUDIT_ALREADY_HANDLED);

        // 使用策略模式获取对应类型的处理器
        AuditHandler handler = auditHandlerContext.getHandler(auditLog.getTargetType());
        
        // 2. 获取当前状态（审核前状态）
        Integer fromStatus = handler.getCurrentStatus(auditLog.getTargetId());
        
        // 3. 执行审核（内部使用乐观锁更新目标状态）
        boolean success;
        Integer toStatus;
        // 使用 code 比较，避免枚举反序列化问题
        boolean isApproved = auditDTO.getAuditStatus() != null 
                && auditDTO.getAuditStatus().getCode() == AuditStatusEnum.APPROVED.getCode();
        
        log.info("审核操作: targetId={}, auditStatus={}, isApproved={}", 
                auditLog.getTargetId(), auditDTO.getAuditStatus(), isApproved);
        
        if (isApproved) {
            success = handler.onApproved(auditLog.getTargetId());
            toStatus = AuditStatusEnum.APPROVED.getCode();
        } else {
            success = handler.onRejected(auditLog.getTargetId(), auditDTO.getAuditReason());
            toStatus = AuditStatusEnum.DELETED.getCode();
        }
        
        // 4. 检查更新是否成功（乐观锁冲突时返回 false）
        AssertUtils.isTrue(success, ContentResultCodeEnum.AUDIT_ALREADY_HANDLED);

        // 5. 插入新的审核历史记录（Requirements: 4.1 - 追加而非更新）
        AuditLog newAuditLog = new AuditLog();
        newAuditLog.setTargetId(auditLog.getTargetId());
        newAuditLog.setTargetType(auditLog.getTargetType());
        newAuditLog.setFromStatus(fromStatus);
        newAuditLog.setToStatus(toStatus);
        newAuditLog.setAuditReason(auditDTO.getAuditReason());
        newAuditLog.setAuditBy(auditDTO.getAuditBy());
        
        AssertUtils.isTrue(this.save(newAuditLog), ContentResultCodeEnum.AUDIT_LOG_INSERT_FAILED);
    }


    @Override
    public PageVO<AuditVO> getAuditPage(PageDTO<AuditQueryDTO> pageDTO) {
        // 从Security上下文获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        // 权限检查：必须是管理员才能查看审核列表
        AssertUtils.isTrue(isAdmin, ContentResultCodeEnum.AUDIT_VIEW_NO_PERMISSION);

        // 获取查询条件
        AuditQueryDTO queryDTO = pageDTO.getParams();
        
        // 构造查询参数
        Integer targetType = null;
        Integer auditStatus = null;
        if (queryDTO != null) {
            targetType = queryDTO.getTargetType() != null ? queryDTO.getTargetType().getCode() : null;
            auditStatus = queryDTO.getAuditStatus() != null ? queryDTO.getAuditStatus().getCode() : null;
        }

        // 构造分页对象
        Page<AuditVO> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());

        // 使用 XML 查询（关联内容/评论详情）
        IPage<AuditVO> result = baseMapper.selectAuditPageWithDetail(page, targetType, auditStatus);

        // 填充封面URL
        fillCoverUrls(result.getRecords());

        // 转换为 PageVO
        PageVO<AuditVO> pageVO = new PageVO<>();
        pageVO.setRecords(result.getRecords());
        pageVO.setTotal(result.getTotal());
        pageVO.setPageNum((int) result.getCurrent());
        pageVO.setPageSize((int) result.getSize());
        return pageVO;
    }

    /**
     * 批量填充封面URL
     */
    private void fillCoverUrls(List<AuditVO> auditList) {
        if (CollUtil.isEmpty(auditList)) {
            return;
        }

        try {
            // 收集所有封面文件ID
            List<String> coverFileIds = auditList.stream()
                    .map(AuditVO::getContentCoverFileId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());

            if (CollUtil.isEmpty(coverFileIds)) {
                return;
            }

            // 批量获取文件URL
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(coverFileIds);
            request.setExpirySeconds(86400); // 24小时过期

            BatchUrlVO response = mediaFeignClient.batchGetFileUrls(request);

            if (response != null && response.getUrls() != null && !response.getUrls().isEmpty()) {
                Map<String, String> urlMap = response.getUrls();
                // 填充封面URL
                auditList.forEach(audit -> {
                    String coverFileId = audit.getContentCoverFileId();
                    if (StrUtil.isNotBlank(coverFileId)) {
                        audit.setContentCoverUrl(urlMap.get(coverFileId));
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量获取封面URL失败，跳过填充封面URL", e);
        }
    }


    @Override
    public AuditVO getAuditRecord(AuditQueryDTO queryDTO) {
        AssertUtils.notNull(queryDTO, ContentResultCodeEnum.AUDIT_QUERY_PARAM_NULL);
        AssertUtils.notNull(queryDTO.getTargetId(), ContentResultCodeEnum.AUDIT_TARGET_ID_NULL);
        AssertUtils.notNull(queryDTO.getTargetType(), ContentResultCodeEnum.AUDIT_TARGET_TYPE_NULL);
        
        // 查询最新的审核记录（Requirements: 4.4）
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditLog::getTargetId, queryDTO.getTargetId())
                .eq(AuditLog::getTargetType, queryDTO.getTargetType())
                .orderByDesc(AuditLog::getCreateTime)
                .last("LIMIT 1");

        AuditLog auditLog = this.getOne(queryWrapper);
        AssertUtils.notNull(auditLog, ContentResultCodeEnum.AUDIT_RECORD_NOT_FOUND);

        // 从Security上下文获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户ID（从Details中获取，由ServiceAuthenticationFilter设置）
        Long currentUserId = (Long) authentication.getDetails();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        // 权限检查：非管理员只能查看自己的审核记录
        if (!isAdmin) {
            // 根据目标类型获取创建者ID
            Long creatorId = getCreatorId(queryDTO.getTargetType(), queryDTO.getTargetId());
            
            AssertUtils.isTrue(creatorId != null && creatorId.equals(currentUserId), 
                ContentResultCodeEnum.AUDIT_VIEW_NO_PERMISSION);
        }

        // 手动映射字段（toStatus -> auditStatus）
        AuditVO auditVO = new AuditVO();
        auditVO.setId(auditLog.getId());
        auditVO.setTargetId(auditLog.getTargetId());
        auditVO.setTargetType(auditLog.getTargetType() != null ? String.valueOf(auditLog.getTargetType().getCode()) : null);
        auditVO.setAuditStatus(BaseEnum.fromCode(AuditStatusEnum.class, auditLog.getToStatus()));
        auditVO.setAuditReason(auditLog.getAuditReason());
        auditVO.setAuditBy(auditLog.getAuditBy());
        auditVO.setCreateTime(auditLog.getCreateTime());
        
        return auditVO;
    }

    /**
     * 根据目标类型获取创建者ID
     * 
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 创建者ID
     */
    private Long getCreatorId(TypeEnum targetType, Long targetId) {
        switch (targetType) {
            case CONTENT:
                Content content = contentService.getById(targetId);
                return content != null ? content.getUploadedBy() : null;
            case COMMENT:
                Comment comment = commentsService.getById(targetId);
                return comment != null ? comment.getUserId() : null;
            default:
                return null;
        }
    }
}
