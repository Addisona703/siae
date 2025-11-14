package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.dto.request.AuditQueryDTO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.content.enums.status.CommentStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.response.AuditVO;
import com.hngy.siae.content.entity.Audit;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.mapper.AuditMapper;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CommentsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内容审核服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Service
@RequiredArgsConstructor
public class AuditsServiceImpl
        extends ServiceImpl<AuditMapper, Audit>
        implements AuditsService {

    private final ContentService contentService;
    private final CommentsService commentsService;
    private final StatisticsService statisticsService;


    @Override
    public Long submitAudit(AuditDTO auditDTO) {
        // 1. 检查是否已有审核请求
        boolean exists = this.getOne(new LambdaQueryWrapper<Audit>()
                .eq(Audit::getTargetId, auditDTO.getTargetId())
                .eq(Audit::getTargetType, auditDTO.getTargetType())) != null;
        AssertUtils.isFalse(exists, ContentResultCodeEnum.AUDIT_ALREADY_EXISTS);

        // 2. 发起请求
        Audit audit = BeanConvertUtil.to(auditDTO, Audit.class);
        AssertUtils.isTrue(this.save(audit), ContentResultCodeEnum.AUDIT_SUBMIT_FAILED);
        return audit.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAudit(Long id, AuditDTO auditDTO) {
        Audit audit = this.getById(id);
        AssertUtils.notNull(audit, ContentResultCodeEnum.AUDIT_NOT_FOUND);
        AssertUtils.isTrue(audit.getAuditStatus() == AuditStatusEnum.PENDING, ContentResultCodeEnum.AUDIT_ALREADY_HANDLED);

        // TODO: 对于评论和内容要分开处理，评论的审核由机器审核，再由人工审查，对于某些内容，不需要人工审核
        updateStatus(audit.getTargetId(), audit.getTargetType(), auditDTO.getAuditStatus());

        // 通过审核则创建统计表，TODO: 后续添加评论的统计表暂时不加
        if(auditDTO.getAuditStatus() == AuditStatusEnum.APPROVED &&
                auditDTO.getTargetType() == TypeEnum.CONTENT) {
            statisticsService.addContentStatistics(auditDTO.getTargetId());
        }

        // 修改内容状态（使用乐观锁）
        audit.setAuditStatus(auditDTO.getAuditStatus());
        audit.setAuditReason(auditDTO.getAuditReason());
        audit.setAuditBy(auditDTO.getAuditBy());
        // version字段会由MyBatis-Plus自动处理
        boolean updated = this.updateById(audit);
        AssertUtils.isTrue(updated, ContentResultCodeEnum.AUDIT_HANDLE_FAILED);
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

        // 构造分页对象
        Page<Audit> auditPage = PageConvertUtil.toPage(pageDTO);

        // 获取查询条件
        AuditQueryDTO queryDTO = pageDTO.getParams();
        
        // 构造查询条件
        LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Audit::getId);
        
        if (queryDTO != null) {
            queryWrapper.eq(queryDTO.getTargetType() != null, Audit::getTargetType, queryDTO.getTargetType());
            queryWrapper.eq(queryDTO.getAuditStatus() != null, Audit::getAuditStatus, queryDTO.getAuditStatus());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Audit::getCreateTime);

        // 执行分页查询
        Page<Audit> result = this.page(auditPage, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果
        return PageConvertUtil.convert(result, AuditVO.class);
    }


    @Override
    public AuditVO getAuditRecord(AuditQueryDTO queryDTO) {
        AssertUtils.notNull(queryDTO, ContentResultCodeEnum.AUDIT_QUERY_PARAM_NULL);
        AssertUtils.notNull(queryDTO.getTargetId(), ContentResultCodeEnum.AUDIT_TARGET_ID_NULL);
        AssertUtils.notNull(queryDTO.getTargetType(), ContentResultCodeEnum.AUDIT_TARGET_TYPE_NULL);
        
        LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Audit::getTargetId, queryDTO.getTargetId())
                .eq(Audit::getTargetType, queryDTO.getTargetType())
                .orderByDesc(Audit::getCreateTime)
                .last("LIMIT 1");

        Audit audit = this.getOne(queryWrapper);
        AssertUtils.notNull(audit, ContentResultCodeEnum.AUDIT_RECORD_NOT_FOUND);

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
            Long creatorId = null;
            switch (queryDTO.getTargetType()) {
                case CONTENT:
                    Content content = contentService.getById(queryDTO.getTargetId());
                    if (content != null) {
                        creatorId = content.getUploadedBy();
                    }
                    break;
                case COMMENT:
                    Comment comment = commentsService.getById(queryDTO.getTargetId());
                    if (comment != null) {
                        creatorId = comment.getUserId();
                    }
                    break;
                default:
                    break;
            }
            
            AssertUtils.isTrue(creatorId != null && creatorId.equals(currentUserId), 
                ContentResultCodeEnum.AUDIT_VIEW_NO_PERMISSION);
        }

        return BeanConvertUtil.to(audit, AuditVO.class);
    }


    public void updateStatus(Long targetId, TypeEnum type, AuditStatusEnum status) {
        switch (type) {
            case CONTENT:
                Content content = contentService.getById(targetId);
                AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);
                content.setStatus(status == AuditStatusEnum.APPROVED ? ContentStatusEnum.PUBLISHED : ContentStatusEnum.DRAFT);
                AssertUtils.isTrue(contentService.updateById(content), ContentResultCodeEnum.CONTENT_UPDATE_STATUS_FAILED);
                break;
            case COMMENT:
                Comment comment = commentsService.getById(targetId);
                AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);
                comment.setStatus(status == AuditStatusEnum.APPROVED ? CommentStatusEnum.APPROVED : CommentStatusEnum.DELETED);
                AssertUtils.isTrue(commentsService.updateById(comment), ContentResultCodeEnum.COMMENT_UPDATE_STATUS_FAILED);
                break;
            default:
                throw new IllegalArgumentException(ContentResultCodeEnum.UNKNOWN_TYPE.getMessage() + ": " + type);
        }
    }
}
