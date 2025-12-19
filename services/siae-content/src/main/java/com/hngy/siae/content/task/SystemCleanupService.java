package com.hngy.siae.content.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.response.BatchDeleteVO;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.entity.AuditLog;
import com.hngy.siae.content.entity.Category;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.entity.FavoriteFolder;
import com.hngy.siae.content.entity.FavoriteItem;
import com.hngy.siae.content.entity.TagRelation;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.FavoriteFolderMapper;
import com.hngy.siae.content.mapper.FavoriteItemMapper;
import com.hngy.siae.content.service.AuditsService;
import com.hngy.siae.content.service.CategoriesService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.content.strategy.content.ContentStrategyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.hngy.siae.core.asserts.AssertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容清理任务
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemCleanupService {

    private final ContentService contentService;
    private final AuditsService auditService;
    private final CategoriesService categoriesService;
    private final TagRelationService tagRelationService;
    private final ContentStrategyContext strategyContext;
    private final MediaFeignClient mediaFeignClient;
    private final FavoriteItemMapper favoriteItemMapper;
    private final FavoriteFolderMapper favoriteFolderMapper;
    private final ContentMapper contentMapper;

    private static final int PAGE_SIZE = 500;

    /**
     * 清除已删除内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanDeletedContents() {
        log.info("【定时任务】开始执行内容清理任务...");

        int page = 1;
        while (true) {

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 查询所有过期的内容
            IPage<Content> pageData = contentService.page(
                    new Page<>(page, PAGE_SIZE),
                    new LambdaQueryWrapper<Content>()
                            .eq(Content::getStatus, ContentStatusEnum.DELETED)
//                            .lt(Content::getUpdateTime, LocalDateTime.now().minusDays(30))
            );
            System.out.println(TransactionSynchronizationManager.isActualTransactionActive());
            List<Long> contentIds = pageData.getRecords().stream()
                    .map(Content::getId)
                    .collect(Collectors.toList());

            if (contentIds.isEmpty()) {
                stopWatch.stop();
                log.info("【定时任务】第 {} 页无数据，耗时：{} ms，任务结束", page, stopWatch.getTotalTimeMillis());
                break;
            }

            // 批量删除审核历史记录
            boolean auditRemoved = auditService.remove(
                    new LambdaQueryWrapper<AuditLog>().in(AuditLog::getTargetId, contentIds));
            AssertUtils.isTrue(auditRemoved, "批量删除审核记录失败");

            // 批量删除标签关系表
            LambdaQueryWrapper<TagRelation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(TagRelation::getContentId, contentIds);

            long count = tagRelationService.count(queryWrapper);
            if (count > 0) {
                boolean tagsRemoved = tagRelationService.remove(queryWrapper);
                AssertUtils.isTrue(tagsRemoved, "批量删除标签关系失败");
            }

            // 批量删除收藏记录并更新收藏夹计数
            cleanFavoriteItems(contentIds);

            // 收集所有需要删除的媒体文件ID（封面 + 详情中的媒体文件）
            List<String> mediaFileIds = new ArrayList<>();
            
            // 1. 收集封面文件ID
            pageData.getRecords().stream()
                    .map(Content::getCoverFileId)
                    .filter(StrUtil::isNotBlank)
                    .forEach(mediaFileIds::add);

            // 批量删除详情，分组处理
            Map<ContentTypeEnum, List<Long>> typeIdMap = pageData.getRecords().stream()
                    .collect(Collectors.groupingBy(
                            Content::getType,
                            Collectors.mapping(Content::getId, Collectors.toList())
                    ));

            typeIdMap.forEach((type, idList) -> strategyContext.tryGetStrategy(type)
                    .ifPresent(strategy -> {
                        // 2. 收集详情中的媒体文件ID（视频、附件等）
                        List<String> detailMediaIds = strategy.getMediaFileIds(idList);
                        if (CollUtil.isNotEmpty(detailMediaIds)) {
                            mediaFileIds.addAll(detailMediaIds);
                        }
                        
                        // 删除详情记录
                        boolean deleted = strategy.batchDelete(idList);
                        AssertUtils.isTrue(deleted, "策略批量删除详情失败，类型：" + type);
                    }));

            // 3. 调用媒体服务批量删除媒体文件
            if (CollUtil.isNotEmpty(mediaFileIds)) {
                try {
                    log.info("【定时任务】开始删除关联媒体文件，数量：{}", mediaFileIds.size());
                    BatchDeleteVO deleteResult = mediaFeignClient.batchDeleteFiles(mediaFileIds);
                    log.info("【定时任务】媒体文件删除完成，成功：{}，失败：{}", 
                            deleteResult.getSuccessCount(), deleteResult.getFailedCount());
                    if (deleteResult.getFailedCount() > 0) {
                        log.warn("【定时任务】部分媒体文件删除失败：{}", deleteResult.getFailedIds());
                    }
                } catch (Exception e) {
                    // 媒体服务不可用时记录警告，但不影响内容删除
                    log.warn("【定时任务】调用媒体服务删除文件失败，将在下次清理时重试：{}", e.getMessage());
                }
            }

            // 批量删除主内容
            boolean contentRemoved = contentService.removeByIds(contentIds);
            AssertUtils.isTrue(contentRemoved, "批量删除主内容失败");

            stopWatch.stop();
            log.info("【定时任务】清理第 {} 页耗时：{} ms", page, stopWatch.getTotalTimeMillis());

            if (pageData.getRecords().size() < PAGE_SIZE) {
                break;
            }
            page++;
        }

        log.info("【定时任务】内容定时清理任务完成，共处理 {} 页", page);
    }

    /**
     * 清理内容回收站
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanTrashContents() {
        log.info("【定时任务】开始清理回收站...");

        int page = 1;
        while(true) {
            IPage<Content> pageData = contentService.page(
                    new Page<>(page, PAGE_SIZE),
                    new LambdaQueryWrapper<Content>()
                            .eq(Content::getStatus, ContentStatusEnum.TRASH)
//                            .lt(Content::getUpdateTime, LocalDateTime.now().minusDays(30))
            );

            List<Content> contentsToUpdate = pageData.getRecords();

            if (contentsToUpdate.isEmpty()) {
                log.info("【定时任务】回收站无内容需要清理");
                break;
            }

            // 更新状态
            contentsToUpdate.forEach(content -> content.setStatus(ContentStatusEnum.DELETED));

            boolean success = contentService.updateBatchById(contentsToUpdate);
            if (!success) {
                log.error("【定时任务】分页批量更新回收站内容状态失败，页码：{}", page);
                AssertUtils.isFalse(false, "分页批量更新回收站内容状态失败，页码：" + page);
            }

            log.info("【定时任务】成功将 {} 条内容从回收站状态更新为已删除", contentsToUpdate.size());

            if (contentsToUpdate.size() < PAGE_SIZE) {
                // 最后一页了，退出循环
                break;
            }

            page++;
        }

        log.info("【定时任务】回收站清理完成");
    }


    /**
     * 清除已删除分类标签
     */
    public void cleanDeletedCategory() {
        log.info("【定时任务】开始清理分类标签...");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 查询所有已删除状态的分类
        List<Category> categories = categoriesService.lambdaQuery()
                .eq(Category::getStatus, CategoryStatusEnum.DELETED)
                .list();

        if (categories.isEmpty()) {
            stopWatch.stop();
            log.info("【定时任务】无需清理的分类数据，耗时：{} ms", stopWatch.getTotalTimeMillis());
            return;
        }

        // 收集可以删除的分类ID
        List<Long> categoriesToDelete = categories.stream()
                .map(Category::getId)
                .filter(categoryId -> !contentService.lambdaQuery()
                        .eq(Content::getCategoryId, categoryId)
                        .exists())
                .collect(Collectors.toList());

        if (categoriesToDelete.isEmpty()) {
            log.warn("【定时任务】所有待删除分类都存在关联内容，跳过删除操作");
            return;
        }

        // 物理删除没有关联内容的分类
        boolean removed = categoriesService.removeByIds(categoriesToDelete);
        AssertUtils.isTrue(removed, "批量删除分类失败");

        stopWatch.stop();
        log.info("【定时任务】分类清理完成，共清理 {} 条数据，耗时：{} ms", categoriesToDelete.size(), stopWatch.getTotalTimeMillis());
    }

    /**
     * 清理收藏记录并更新收藏夹计数
     *
     * @param contentIds 要清理的内容ID列表
     */
    private void cleanFavoriteItems(List<Long> contentIds) {
        if (CollUtil.isEmpty(contentIds)) {
            return;
        }

        // 查询受影响的收藏记录，获取收藏夹ID
        LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(FavoriteItem::getContentId, contentIds);
        List<FavoriteItem> favoriteItems = favoriteItemMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(favoriteItems)) {
            log.info("【定时任务】无需清理的收藏记录");
            return;
        }

        // 收集受影响的收藏夹ID
        List<Long> affectedFolderIds = favoriteItems.stream()
                .map(FavoriteItem::getFolderId)
                .distinct()
                .collect(Collectors.toList());

        // 删除收藏记录
        int deletedCount = favoriteItemMapper.delete(queryWrapper);
        log.info("【定时任务】删除收藏记录 {} 条", deletedCount);

        // 更新受影响收藏夹的计数
        for (Long folderId : affectedFolderIds) {
            updateFolderItemCount(folderId);
        }
        log.info("【定时任务】更新收藏夹计数 {} 个", affectedFolderIds.size());
    }

    /**
     * 更新收藏夹的内容数量（只统计已发布的内容）
     */
    private void updateFolderItemCount(Long folderId) {
        // 只统计已发布状态的内容
        Long count = contentMapper.countPublishedFavoriteItems(folderId);

        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder != null) {
            folder.setItemCount(count.intValue());
            favoriteFolderMapper.updateById(folder);
        }
    }
}
