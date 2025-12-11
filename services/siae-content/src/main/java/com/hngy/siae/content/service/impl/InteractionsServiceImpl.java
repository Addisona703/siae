package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.enums.status.ActionStatusEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.interaction.ActionDTO;
import com.hngy.siae.content.entity.UserAction;
import com.hngy.siae.content.mapper.CommentMapper;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.FavoriteItemMapper;
import com.hngy.siae.content.mapper.FavoriteFolderMapper;
import com.hngy.siae.content.mapper.UserActionMapper;
import com.hngy.siae.content.entity.FavoriteItem;
import com.hngy.siae.content.entity.FavoriteFolder;
import com.hngy.siae.content.service.InteractionsService;
import com.hngy.siae.content.service.StatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交互服务实现类
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionsServiceImpl
        extends ServiceImpl<UserActionMapper, UserAction>
        implements InteractionsService {

    private final StatisticsService statisticsService;
    private final CommentMapper commentMapper;
    private final ContentMapper contentMapper;
    private final FavoriteItemMapper favoriteItemMapper;
    private final FavoriteFolderMapper favoriteFolderMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAction(ActionDTO actionDTO) {
        ActionTypeEnum actionType = actionDTO.getActionType();
        TypeEnum targetType = actionDTO.getTargetType();
        Long userId = actionDTO.getUserId();
        Long targetId = actionDTO.getTargetId();

        // 检查交互目标的内容状态
        checkInteractionAllowed(targetId, targetType);

        UserAction existing = getExistingAction(userId, targetId, actionType);

        if (existing != null) {
            if (existing.getStatus() == ActionStatusEnum.ACTIVATED) {
                return;
            }
            // 恢复为激活状态
            updateStatus(existing, ActionStatusEnum.ACTIVATED);
            
            // 如果是恢复收藏行为，同步恢复收藏夹记录
            if (actionType == ActionTypeEnum.FAVORITE && targetType == TypeEnum.CONTENT) {
                restoreFavoriteItem(userId, targetId);
            }
        } else {
            // 新建记录
            createUserAction(actionDTO);
        }

        // 异步更新统计，不阻塞主流程
        updateStatisticsAsync(targetId, targetType, actionType, true);
    }

    /**
     * 检查是否允许交互操作
     * - 内容操作：检查内容是否已发布
     * - 评论操作：检查评论所属内容是否已发布
     */
    private void checkInteractionAllowed(Long targetId, TypeEnum targetType) {
        if (targetType == TypeEnum.CONTENT) {
            // 直接检查内容状态
            checkContentStatus(targetId);
        } else if (targetType == TypeEnum.COMMENT) {
            // 评论点赞：需要检查评论所属内容的状态
            Comment comment = commentMapper.selectById(targetId);
            AssertUtils.notNull(comment, ContentResultCodeEnum.COMMENT_NOT_FOUND);
            checkContentStatus(comment.getContentId());
        }
    }

    /**
     * 检查内容状态，只有已发布的内容才能进行交互
     */
    private void checkContentStatus(Long contentId) {
        Content content = contentMapper.selectById(contentId);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);
        AssertUtils.isTrue(content.getStatus() == ContentStatusEnum.PUBLISHED,
                ContentResultCodeEnum.CONTENT_NOT_PUBLISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAction(ActionDTO actionDTO) {
        ActionTypeEnum actionType = actionDTO.getActionType();
        TypeEnum targetType = actionDTO.getTargetType();
        Long userId = actionDTO.getUserId();
        Long targetId = actionDTO.getTargetId();

        // 检查交互目标的内容状态
        checkInteractionAllowed(targetId, targetType);

        UserAction existing = getExistingAction(userId, targetId, actionType);

        if (existing == null || existing.getStatus() == ActionStatusEnum.CANCELLED) {
            return;
        }

        updateStatus(existing, ActionStatusEnum.CANCELLED);

        // 如果是取消收藏行为，同步删除收藏夹中的记录
        if (actionType == ActionTypeEnum.FAVORITE && targetType == TypeEnum.CONTENT) {
            removeFavoriteItems(userId, targetId);
        }

        // 异步更新统计，不阻塞主流程
        updateStatisticsAsync(targetId, targetType, actionType, false);
    }

    /**
     * 物理删除用户对指定内容的所有收藏记录，并更新相关收藏夹的计数
     */
    private void removeFavoriteItems(Long userId, Long contentId) {
        // 查询所有收藏记录
        LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteItem::getUserId, userId)
                .eq(FavoriteItem::getContentId, contentId);
        
        java.util.List<FavoriteItem> items = favoriteItemMapper.selectList(queryWrapper);
        
        if (!items.isEmpty()) {
            java.util.Set<Long> affectedFolderIds = items.stream()
                    .map(FavoriteItem::getFolderId)
                    .collect(java.util.stream.Collectors.toSet());
            
            // 物理删除收藏记录
            favoriteItemMapper.delete(queryWrapper);
            
            // 更新受影响收藏夹的计数
            for (Long folderId : affectedFolderIds) {
                updateFolderItemCount(folderId);
            }
        }
    }

    /**
     * 恢复收藏项到默认收藏夹
     */
    private void restoreFavoriteItem(Long userId, Long contentId) {
        // 检查是否已存在收藏记录
        LambdaQueryWrapper<FavoriteItem> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FavoriteItem::getUserId, userId)
                .eq(FavoriteItem::getContentId, contentId);
        
        if (favoriteItemMapper.selectCount(checkWrapper) > 0) {
            // 已存在，无需创建
            return;
        }
        
        // 获取或创建默认收藏夹
        Long defaultFolderId = getOrCreateDefaultFolder(userId);
        
        // 创建收藏项
        FavoriteItem item = new FavoriteItem();
        item.setFolderId(defaultFolderId);
        item.setUserId(userId);
        item.setContentId(contentId);
        item.setSortOrder(0);
        item.setStatus(1);
        
        favoriteItemMapper.insert(item);
        
        // 更新收藏夹计数
        updateFolderItemCount(defaultFolderId);
    }

    /**
     * 获取或创建默认收藏夹
     */
    private Long getOrCreateDefaultFolder(Long userId) {
        LambdaQueryWrapper<FavoriteFolder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteFolder::getUserId, userId)
                .eq(FavoriteFolder::getIsDefault, 1)
                .eq(FavoriteFolder::getStatus, 1);

        FavoriteFolder defaultFolder = favoriteFolderMapper.selectOne(queryWrapper);

        if (defaultFolder == null) {
            // 创建默认收藏夹
            defaultFolder = new FavoriteFolder();
            defaultFolder.setUserId(userId);
            defaultFolder.setName("默认收藏夹");
            defaultFolder.setDescription("系统自动创建的默认收藏夹");
            defaultFolder.setIsDefault(1);
            defaultFolder.setIsPublic(0);
            defaultFolder.setSortOrder(0);
            defaultFolder.setItemCount(0);
            defaultFolder.setStatus(1);

            favoriteFolderMapper.insert(defaultFolder);
        }

        return defaultFolder.getId();
    }

    /**
     * 更新收藏夹的内容数量
     */
    private void updateFolderItemCount(Long folderId) {
        LambdaQueryWrapper<FavoriteItem> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FavoriteItem::getFolderId, folderId);
        Long count = favoriteItemMapper.selectCount(countWrapper);

        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder != null) {
            folder.setItemCount(count.intValue());
            favoriteFolderMapper.updateById(folder);
        }
    }

    /**
     * 异步更新统计数据
     */
    @Async
    public void updateStatisticsAsync(Long targetId, TypeEnum targetType, ActionTypeEnum actionType, boolean increment) {
        try {
            if (targetType == TypeEnum.COMMENT) {
                // 评论点赞
                if (increment) {
                    incrementCommentLikeCount(targetId);
                } else {
                    decrementCommentLikeCount(targetId);
                }
            } else {
                // 内容点赞/收藏/浏览
                if (increment) {
                    statisticsService.incrementStatistics(targetId, actionType);
                } else {
                    statisticsService.decrementStatistics(targetId, actionType);
                }
            }
        } catch (Exception e) {
            log.error("异步更新统计失败: targetId={}, actionType={}, increment={}", targetId, actionType, increment, e);
        }
    }

    /**
     * 增加评论点赞数
     */
    private void incrementCommentLikeCount(Long commentId) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .setSql("like_count = like_count + 1");
        commentMapper.update(null, updateWrapper);
    }

    /**
     * 减少评论点赞数
     */
    private void decrementCommentLikeCount(Long commentId) {
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .setSql("like_count = GREATEST(like_count - 1, 0)");
        commentMapper.update(null, updateWrapper);
    }


    /**
     * 查询现有行为记录
     */
    private UserAction getExistingAction(Long userId, Long targetId, ActionTypeEnum actionType) {
        return lambdaQuery()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetId, targetId)
                .eq(UserAction::getActionType, actionType)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 创建新的行为记录
     */
    private void createUserAction(ActionDTO actionDTO) {
        UserAction userAction = BeanConvertUtil.to(actionDTO, UserAction.class);
        userAction.setStatus(ActionStatusEnum.ACTIVATED);
        AssertUtils.isTrue(this.save(userAction),
                ActionStatusEnum.ACTIVATED.getDescription() +
                        actionDTO.getActionType().getDescription() + "失败");
    }

    /**
     * 更新行为状态
     */
    private void updateStatus(UserAction userAction, ActionStatusEnum newStatus) {
        userAction.setStatus(newStatus);
        AssertUtils.isTrue(this.updateById(userAction), ContentResultCodeEnum.INTERACTION_UPDATE_STATUS_FAILED);
    }

    @Override
    public java.util.Set<Long> getActivatedTargetIds(Long userId, java.util.List<Long> targetIds, TypeEnum targetType, ActionTypeEnum actionType) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return java.util.Collections.emptySet();
        }

        return lambdaQuery()
                .eq(UserAction::getUserId, userId)
                .in(UserAction::getTargetId, targetIds)
                .eq(UserAction::getTargetType, targetType)
                .eq(UserAction::getActionType, actionType)
                .eq(UserAction::getStatus, ActionStatusEnum.ACTIVATED)
                .list()
                .stream()
                .map(UserAction::getTargetId)
                .collect(java.util.stream.Collectors.toSet());
    }
}