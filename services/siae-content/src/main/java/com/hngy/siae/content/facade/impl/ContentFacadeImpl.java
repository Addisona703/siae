package com.hngy.siae.content.facade.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.response.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.AuditStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.facade.ContentFacade;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.StatisticsMapper;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.content.strategy.ContentStrategyContext;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.enums.BaseEnum;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    private final ContentMapper contentMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentVO<ContentDetailVO> publishContent(@NotNull ContentCreateDTO contentCreateDTO) {
        // 1. 插入内容
        Content content = contentService.createContent(contentCreateDTO);
        content.setStatus(ContentStatusEnum.PENDING);

        // 2. 插入详情内容，传入枚举类型
        ContentDetailVO detail = strategyContext.getStrategy(content.getType())
                .insert(content.getId(), contentCreateDTO.getDetail());

        // 3. 插入审核记录
        auditsService.submitAudit(AuditDTO.builder()
                .targetId(content.getId())
                .targetType(TypeEnum.CONTENT)
                .auditStatus(AuditStatusEnum.PENDING)
                .build());

        // 4. 标签关联
        List<Long> tagIds = contentCreateDTO.getTagIds();
        tagRelationService.createRelations(content.getId(), tagIds);

        // 5. 返回封装，忽略类型擦除警告
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(content, ContentVO.class);
        vo.setTagIds(tagIds);
        vo.setDetail(detail);

        return vo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentVO<ContentDetailVO> editContent(@NotNull ContentUpdateDTO contentUpdateDTO) {
        // 1. 更新内容
        Content content = contentService.updateContent(contentUpdateDTO);

        // 3. 更新详情（如果策略存在并且传了 detail）
        ContentDetailVO detail = Optional.ofNullable(content.getType())
                .flatMap(strategyContext::tryGetStrategy)
                .filter(strategy -> contentUpdateDTO.getDetail() != null)
                .map(strategy -> strategy.update(content.getId(), contentUpdateDTO.getDetail()))
                .orElse(null);

        // 4. 更新标签关系表，如果有的话，这里采用先删在插的方式，后期数据量增大再使用差量更新
        tagRelationService.updateRelations(content.getId(), contentUpdateDTO.getTagIds());

        // 5. 返回封装
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(content, ContentVO.class);
        vo.setTagIds(contentUpdateDTO.getTagIds());
        vo.setDetail(detail);

        return vo;
    }


    @Override
    public ContentVO<ContentDetailVO> queryContent(@NotNull Long contentId) {
        // 使用 XML 联表查询获取内容基本信息、分类、标签、统计信息
        ContentDetailDTO contentDetailDTO = contentMapper.selectContentDetailById(contentId);
        AssertUtils.notNull(contentDetailDTO, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 获取内容详情（文章、笔记、问题等具体内容）
        ContentTypeEnum contentType = BaseEnum.fromCode(ContentTypeEnum.class, contentDetailDTO.getType());
        ContentDetailVO detailVO = strategyContext.getStrategy(contentType)
                .getDetail(contentId);

        // 使用 BeanConvertUtil 转换基本属性
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(contentDetailDTO, ContentVO.class);
        
        // 手动设置需要特殊处理的字段
        vo.setType(contentType);
        vo.setStatus(BaseEnum.fromCode(ContentStatusEnum.class, contentDetailDTO.getStatus()));
        vo.setDetail(detailVO);

        // 解析标签ID列表
        if (contentDetailDTO.getTagIdsStr() != null && !contentDetailDTO.getTagIdsStr().isEmpty()) {
            List<Long> tagIds = Arrays.stream(contentDetailDTO.getTagIdsStr().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            vo.setTagIds(tagIds);
        }

        return vo;
    }


    @Override
    public PageVO<HotContentVO> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO) {
        // 1. 参数校验
        AssertUtils.notNull(contentHotPageDTO, ContentResultCodeEnum.CONTENT_NOT_FOUND);
        AssertUtils.isTrue(contentHotPageDTO.getPageNum() > 0 && contentHotPageDTO.getPageSize() > 0, 
                ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 2. 准备查询参数
        Integer typeCode = Optional.ofNullable(contentHotPageDTO.getType())
                .map(ContentTypeEnum::getCode)
                .orElse(null);

        String sortBy = Optional.ofNullable(contentHotPageDTO.getSortBy())
                .map(value -> switch (value) {
                    case "likeCount", "favoriteCount", "commentCount", "createTime", "viewCount" -> value;
                    default -> "viewCount";
                })
                .orElse("viewCount");

        // 3. 使用 MyBatis-Plus 分页插件查询热门内容
        Page<HotContentVO> page = PageConvertUtil.toPage(contentHotPageDTO);
        IPage<HotContentVO> resultPage = statisticsMapper.selectHotContentPage(
                page,
                contentHotPageDTO.getCategoryId(),
                typeCode,
                sortBy,
                ContentStatusEnum.PUBLISHED.getCode()
        );

        // 4. 使用 PageConvertUtil 转换为 PageVO
        return PageConvertUtil.convert(resultPage);
    }
}

