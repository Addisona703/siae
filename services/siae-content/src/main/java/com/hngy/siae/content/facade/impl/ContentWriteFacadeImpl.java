package com.hngy.siae.content.facade.impl;

import com.hngy.siae.content.dto.request.audit.AuditDTO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.facade.ContentWriteFacade;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.content.strategy.content.ContentStrategyContext;
import com.hngy.siae.core.utils.BeanConvertUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 内容写操作外观实现类
 * 负责发布、编辑、删除等写操作
 * 仅依赖 Service 层，不直接依赖 Mapper
 *
 * @author KEYKB
 * @date 2025/11/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentWriteFacadeImpl implements ContentWriteFacade {

    private final ContentService contentService;
    private final AuditsService auditsService;
    private final TagRelationService tagRelationService;
    private final ContentStrategyContext strategyContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentVO<ContentDetailVO> publishContent(@NotNull ContentCreateDTO contentCreateDTO) {
        // 1. 插入内容（状态已在 createContent 中根据传入的 status 设置）
        Content content = contentService.createContent(contentCreateDTO);

        // 2. 插入详情内容，传入枚举类型
        ContentDetailVO detail = strategyContext.getStrategy(content.getType())
                .insert(content.getId(), contentCreateDTO.getDetail());

        // 3. 如果是发布状态（待审核），插入审核记录
        if (content.getStatus() == ContentStatusEnum.PENDING) {
            auditsService.submitAudit(AuditDTO.builder()
                    .targetId(content.getId())
                    .targetType(TypeEnum.CONTENT)
                    .auditStatus(AuditStatusEnum.PENDING)
                    .build());
        }

        // 4. 标签关联
        List<Long> tagIds = contentCreateDTO.getTagIds();
        tagRelationService.createRelations(content.getId(), tagIds);

        // 5. 返回封装
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(content, ContentVO.class);
        vo.setDetail(detail);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentVO<ContentDetailVO> editContent(@NotNull ContentUpdateDTO contentUpdateDTO) {
        // 1. 更新内容
        Content content = contentService.updateContent(contentUpdateDTO);

        // 2. 更新详情（如果策略存在并且传了 detail）
        ContentDetailVO detail = Optional.ofNullable(content.getType())
                .flatMap(strategyContext::tryGetStrategy)
                .filter(strategy -> contentUpdateDTO.getDetail() != null)
                .map(strategy -> strategy.update(content.getId(), contentUpdateDTO.getDetail()))
                .orElse(null);

        // 3. 更新标签关系表
        tagRelationService.updateRelations(content.getId(), contentUpdateDTO.getTagIds());

        // 4. 返回封装
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(content, ContentVO.class);
        vo.setDetail(detail);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(@NotNull Long contentId, @NotNull Integer isTrash) {
        contentService.deleteContent(contentId.intValue(), isTrash);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreContent(@NotNull Long contentId) {
        contentService.restoreContent(contentId);
    }
}
