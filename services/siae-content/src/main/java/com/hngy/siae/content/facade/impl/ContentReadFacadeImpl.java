package com.hngy.siae.content.facade.impl;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.media.util.MediaUrlUtil;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.content.ContentQueryResultVO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.HotContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.dto.response.statistics.StatisticsVO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.facade.ContentReadFacade;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.content.strategy.content.ContentStrategyContext;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.enums.BaseEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容读操作外观实现类
 * 负责查询、热门内容、搜索等读操作
 * 仅依赖 Service 层，不直接依赖 Mapper
 *
 * @author KEYKB
 * @date 2025/11/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentReadFacadeImpl implements ContentReadFacade {

    private final ContentService contentService;
    private final StatisticsService statisticsService;
    private final ContentStrategyContext strategyContext;
    private final MediaFeignClient mediaFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    public ContentVO<ContentDetailVO> queryContent(@NotNull Long contentId) {
        // 1. 使用 Service 层获取内容详情（包含分类、标签、统计信息）
        ContentQueryResultVO contentQueryResultVO = contentService.getContentDetail(contentId);

        // 2. 获取内容详情（文章、笔记、问题等具体内容）
        ContentTypeEnum contentType = BaseEnum.fromCode(ContentTypeEnum.class, contentQueryResultVO.getType());
        ContentDetailVO detailVO = strategyContext.getStrategy(contentType)
                .getDetail(contentId);

        // 3. 处理文字类内容中的 media:// 协议，转换为实际URL
        if (detailVO != null && detailVO.getContent() != null) {
            detailVO.setContent(resolveMediaUrls(detailVO.getContent()));
        }

        // 4. 使用 BeanConvertUtil 转换基本属性
        @SuppressWarnings("unchecked")
        ContentVO<ContentDetailVO> vo = BeanConvertUtil.to(contentQueryResultVO, ContentVO.class);

        // 5. 手动设置需要特殊处理的字段
        vo.setType(contentType);
        vo.setStatus(BaseEnum.fromCode(ContentStatusEnum.class, contentQueryResultVO.getStatus()));
        vo.setDetail(detailVO);

        // 6. 解析标签名称列表
        if (StrUtil.isNotBlank(contentQueryResultVO.getTagNamesStr())) {
            List<String> tagNames = Arrays.asList(contentQueryResultVO.getTagNamesStr().split(","));
            vo.setTagNames(tagNames);
        }

        // 7. 设置统计信息
        vo.setStatistics(StatisticsVO.builder()
                .contentId(contentQueryResultVO.getId())
                .viewCount(contentQueryResultVO.getViewCount())
                .likeCount(contentQueryResultVO.getLikeCount())
                .favoriteCount(contentQueryResultVO.getFavoriteCount())
                .commentCount(contentQueryResultVO.getCommentCount())
                .build());

        // 8. 获取作者信息（昵称和头像）
        fillAuthorInfo(vo, contentQueryResultVO.getUploadedBy());

        // 9. 获取封面URL
        fillCoverUrl(vo, contentQueryResultVO.getCoverFileId());

        return vo;
    }

    /**
     * 填充作者信息（昵称和头像URL）
     */
    private void fillAuthorInfo(ContentVO<ContentDetailVO> vo, Long uploadedBy) {
        if (uploadedBy == null) {
            return;
        }
        try {
            Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(List.of(uploadedBy));
            if (userMap != null && !userMap.isEmpty()) {
                UserProfileSimpleVO userProfile = userMap.get(uploadedBy);
                if (userProfile != null) {
                    vo.setAuthorNickname(userProfile.getNickname());
                    // 获取头像URL
                    if (StrUtil.isNotBlank(userProfile.getAvatarFileId())) {
                        Map<String, String> urlMap = batchGetUrls(List.of(userProfile.getAvatarFileId()));
                        vo.setAuthorAvatarUrl(urlMap.get(userProfile.getAvatarFileId()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取作者信息失败, uploadedBy={}", uploadedBy, e);
        }
    }

    /**
     * 填充封面URL
     */
    private void fillCoverUrl(ContentVO<ContentDetailVO> vo, String coverFileId) {
        if (StrUtil.isBlank(coverFileId)) {
            return;
        }
        try {
            Map<String, String> urlMap = batchGetUrls(List.of(coverFileId));
            vo.setCoverUrl(urlMap.get(coverFileId));
        } catch (Exception e) {
            log.warn("获取封面URL失败, coverFileId={}", coverFileId, e);
        }
    }

    @Override
    public PageVO<HotContentVO> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO) {
        // 调用 StatisticsService 查询热门内容
        return statisticsService.queryHotContent(contentHotPageDTO);
    }

    @Override
    public PageVO<ContentVO<EmptyDetailVO>> searchContent(@NotNull PageDTO<ContentQueryDTO> contentPageDTO) {
        // 调用 ContentService 进行分页查询
        return contentService.getContentPage(contentPageDTO);
    }

    /**
     * 将内容中的 media://{fileId} 替换为实际URL
     */
    private String resolveMediaUrls(String content) {
        List<String> fileIds = MediaUrlUtil.extractFileIds(content);
        if (fileIds.isEmpty()) {
            return content;
        }

        Map<String, String> urlMap = batchGetUrls(fileIds);
        return urlMap.isEmpty() ? content : MediaUrlUtil.replaceWithUrls(content, urlMap);
    }

    /**
     * 批量获取文件URL
     */
    private Map<String, String> batchGetUrls(List<String> fileIds) {
        try {
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(fileIds);
            BatchUrlVO response = mediaFeignClient.batchGetFileUrls(request);
            return response != null && response.getUrls() != null ? response.getUrls() : Collections.emptyMap();
        } catch (Exception e) {
            log.error("批量获取媒体URL失败, fileIds={}", fileIds, e);
            return Collections.emptyMap();
        }
    }
}
