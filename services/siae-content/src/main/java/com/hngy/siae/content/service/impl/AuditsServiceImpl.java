package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.content.common.enums.status.CommentStatusEnum;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
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
        AssertUtils.isFalse(exists, "审核请求已存在，不能重复请求");

        // 2. 发起请求
        Audit audit = BeanUtil.copyProperties(auditDTO, Audit.class);
        AssertUtils.isTrue(this.save(audit), "提交审核请求失败");
        return audit.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleAudit(Long id, AuditDTO auditDTO) {
        Audit audit = this.getById(id);
        AssertUtils.notNull(audit, "这条审核申请不存在");
        AssertUtils.isTrue(audit.getAuditStatus() == AuditStatusEnum.PENDING, "审核已被处理，请勿重复审核");

        // TODO: 对于评论和内容要分开处理，评论的审核由机器审核，再由人工审查，对于某些内容，不需要人工审核
        updateStatus(audit.getTargetId(), audit.getTargetType(), auditDTO.getAuditStatus());

        // 通过审核则创建统计表，TODO: 后续添加评论的统计表暂时不加
        if(auditDTO.getAuditStatus() == AuditStatusEnum.APPROVED &&
                auditDTO.getTargetType() == TypeEnum.CONTENT) {
            statisticsService.addContentStatistics(auditDTO.getTargetId());
        }

        // 修改内容状态
        audit = BeanUtil.copyProperties(auditDTO, Audit.class);
        audit.setId(id);
        AssertUtils.isTrue(this.updateById(audit), "处理审核失败");
        return Result.success();
    }


//    @Override
//    public Result<PageVO<AuditVO>> getAuditPage(Integer page, Integer pageSize, TypeEnum targetType, AuditStatusEnum auditStatus) {
//        // 构造分页对象
//        Page<Audit> auditPage = new Page<>(page, pageSize);
//
//        // 构造查询条件
//        LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(targetType != null, Audit::getTargetType, targetType);
//        queryWrapper.eq(auditStatus != null, Audit::getAuditStatus, auditStatus);
//
//        // 执行分页查询
//        Page<Audit> result = this.page(auditPage, queryWrapper);
//
//        // 转换成 VO
//        List<AuditVO> records = result.getRecords().stream()
//                .map(audit -> BeanUtil.copyProperties(audit, AuditVO.class))
//                .collect(Collectors.toList());
//
//        // 封装分页对象
//        PageVO<AuditVO> pageVO = new PageVO<>();
//        pageVO.setPage((int) result.getCurrent());
//        pageVO.setPageSize((int) result.getSize());
//        pageVO.setTotal((int) result.getTotal());
//        pageVO.setRecords(records);
//
//        return Result.success(pageVO);
//    }


    @Override
    public Result<AuditVO> getAuditRecord(Long targetId, TypeEnum targetType) {
        LambdaQueryWrapper<Audit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Audit::getTargetId, targetId)
                .eq(Audit::getTargetType, targetType)
                .orderByDesc(Audit::getCreateTime)
                .last("LIMIT 1");

        Audit audit = this.getOne(queryWrapper);
        AssertUtils.notNull(audit, "未找到相关审核记录");

        AuditVO auditVO = BeanUtil.copyProperties(audit, AuditVO.class);
        return Result.success(auditVO);
    }


    public void updateStatus(Long targetId, TypeEnum type, AuditStatusEnum status) {
        switch (type) {
            case CONTENT:
                Content content = contentService.getById(targetId);
                AssertUtils.notNull(content, "内容不存在");
                content.setStatus(status == AuditStatusEnum.APPROVED ? ContentStatusEnum.PUBLISHED : ContentStatusEnum.DRAFT);
                AssertUtils.isTrue(contentService.updateById(content), "更新内容状态失败");
                break;
            case COMMENT:
                Comment comment = commentsService.getById(targetId);
                AssertUtils.notNull(comment, "评论不存在");
                comment.setStatus(status == AuditStatusEnum.APPROVED ? CommentStatusEnum.APPROVED : CommentStatusEnum.DELETED);
                AssertUtils.isTrue(commentsService.updateById(comment), "更新评论状态失败");
                break;
            default:
                throw new IllegalArgumentException("未知的类型：" + type);
        }
    }
}