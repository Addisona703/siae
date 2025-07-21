package com.hngy.siae.content.facade.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
import com.hngy.siae.content.common.utils.AssertUtil;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.facade.ContentFacade;
import com.hngy.siae.content.mapper.StatisticsMapper;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.content.strategy.ContentStrategyContext;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * 内容外观类
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentFacadeImpl implements ContentFacade {

    private final ContentService contentService;
    private final AuditsService auditsService;
    private final TagRelationService tagRelationService;
    private final ContentStrategyContext strategyContext;
    private final StatisticsMapper statisticsMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ContentVO<ContentDetailVO>> publishContent(@NotNull ContentDTO contentDTO) {
        // 1. 插入内容
        Content content = contentService.createContent(contentDTO);
        content.setStatus(ContentStatusEnum.PENDING);

        // 2. 插入详情内容，传入枚举类型
        ContentDetailVO detail = strategyContext.getStrategy(content.getType())
                .insert(content.getId(), contentDTO.getDetail());

        // 3. 插入审核记录
        auditsService.submitAudit(AuditDTO.builder()
                .targetId(content.getId())
                .targetType(TypeEnum.CONTENT)
                .auditStatus(AuditStatusEnum.PENDING)
                .build());

        // 4. 标签关联
        List<Long> tagIds = contentDTO.getTagIds();
        tagRelationService.createRelations(content.getId(), tagIds);

        // 5. 返回封装，忽略类型擦除警告
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanUtil.copyProperties(content, ContentVO.class);
        vo.setTagIds(tagIds);
        vo.setDetail(detail);

        return Result.success(vo);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ContentVO<ContentDetailVO>> editContent(@NotNull ContentDTO contentDTO) {
        // 1. 更新内容
        Content content = contentService.updateContent(contentDTO);

        // 3. 更新详情（如果策略存在并且传了 detail）
        ContentDetailVO detail = Optional.ofNullable(content.getType())
                .flatMap(strategyContext::tryGetStrategy)
                .filter(strategy -> contentDTO.getDetail() != null)
                .map(strategy -> strategy.update(content.getId(), contentDTO.getDetail()))
                .orElse(null);

        // 4. 更新标签关系表，如果有的话，这里采用先删在插的方式，后期数据量增大再使用差量更新
        tagRelationService.updateRelations(content.getId(), contentDTO.getTagIds());

        // 5. 返回封装
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanUtil.copyProperties(content, ContentVO.class);
        vo.setTagIds(contentDTO.getTagIds());
        vo.setDetail(detail);

        return Result.success(vo);
    }


    @Override
    public Result<ContentVO<ContentDetailVO>> queryContent(@NotNull Long contentId) {
        Content content = contentService.getBaseMapper().selectById(contentId);
        AssertUtil.notNull(content, "内容查询失败，内容不存在");

        ContentDetailVO detailVO = strategyContext.getStrategy(content.getType())
                .getDetail(contentId);

        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanUtil.copyProperties(content, ContentVO.class);
        vo.setDetail(detailVO);

        return Result.success(vo);
    }


//    @Override
//    public Result<List<HotContentVO>> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO) {
//        // 1. 参数校验
//        AssertUtil.notNull(contentHotPageDTO, "查询参数不能为空");
//        AssertUtil.isTrue(contentHotPageDTO.getPage() > 0 && contentHotPageDTO.getPageSize() > 0, "分页参数不正确");
//
//        // 2. 计算分页偏移量
//        int offset = (contentHotPageDTO.getPage() - 1) * contentHotPageDTO.getPageSize();
//
//        // 3. 查询热门内容
//        List<HotContentVO> hotContentList = statisticsMapper.selectHotContent(
//                contentHotPageDTO.getCategoryId(),
//                Optional.ofNullable(contentHotPageDTO.getType())
//                        .map(ContentTypeEnum::name)
//                        .orElse(null),
//                contentHotPageDTO.getSortBy(),
//                contentHotPageDTO.getPageSize(),
//                offset
//        );
//
//        return Result.success(hotContentList);
//    }
}

