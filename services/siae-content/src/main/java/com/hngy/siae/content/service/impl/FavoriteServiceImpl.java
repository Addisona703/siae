package com.hngy.siae.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderCreateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderUpdateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemAddDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemUpdateDTO;
import com.hngy.siae.content.dto.response.favorite.FavoriteFolderVO;
import com.hngy.siae.content.dto.response.favorite.FavoriteItemVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.entity.FavoriteFolder;
import com.hngy.siae.content.entity.FavoriteItem;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.FavoriteFolderMapper;
import com.hngy.siae.content.mapper.FavoriteItemMapper;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.FavoriteService;
import com.hngy.siae.content.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl
        extends ServiceImpl<FavoriteFolderMapper, FavoriteFolder>
        implements FavoriteService {

    private final FavoriteFolderMapper favoriteFolderMapper;
    private final FavoriteItemMapper favoriteItemMapper;
    private final ContentService contentService;
    private final ContentMapper contentMapper;
    private final StatisticsService statisticsService;
    private final UserFeignClient userFeignClient;
    private final MediaFeignClient mediaFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolderVO createFolder(FavoriteFolderCreateDTO createDTO) {
        // 检查收藏夹名称是否重复
        LambdaQueryWrapper<FavoriteFolder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteFolder::getUserId, createDTO.getUserId())
                .eq(FavoriteFolder::getName, createDTO.getName())
                .eq(FavoriteFolder::getStatus, 1);
        boolean exists = favoriteFolderMapper.selectCount(queryWrapper) > 0;
        AssertUtils.isFalse(exists, ContentResultCodeEnum.FAVORITE_FOLDER_NAME_EXISTS);

        // 创建收藏夹
        FavoriteFolder folder = BeanConvertUtil.to(createDTO, FavoriteFolder.class);
        folder.setIsDefault(0);
        folder.setItemCount(0);
        folder.setStatus(1);
        
        // 设置排序序号（获取当前用户最大序号+1）
        LambdaQueryWrapper<FavoriteFolder> maxSortWrapper = new LambdaQueryWrapper<>();
        maxSortWrapper.eq(FavoriteFolder::getUserId, createDTO.getUserId())
                .orderByDesc(FavoriteFolder::getSortOrder)
                .last("LIMIT 1");
        FavoriteFolder maxSortFolder = favoriteFolderMapper.selectOne(maxSortWrapper);
        folder.setSortOrder(maxSortFolder != null ? maxSortFolder.getSortOrder() + 1 : 0);

        AssertUtils.isTrue(favoriteFolderMapper.insert(folder) > 0, ContentResultCodeEnum.FAVORITE_FOLDER_CREATE_FAILED);

        return BeanConvertUtil.to(folder, FavoriteFolderVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolderVO updateFolder(FavoriteFolderUpdateDTO updateDTO) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(updateDTO.getId());
        AssertUtils.notNull(folder, ContentResultCodeEnum.FAVORITE_FOLDER_NOT_FOUND);
        AssertUtils.isTrue(folder.getStatus() == 1, ContentResultCodeEnum.FAVORITE_FOLDER_DELETED);

        // 如果修改名称，检查是否重复
        if (updateDTO.getName() != null && !updateDTO.getName().equals(folder.getName())) {
            LambdaQueryWrapper<FavoriteFolder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FavoriteFolder::getUserId, folder.getUserId())
                    .eq(FavoriteFolder::getName, updateDTO.getName())
                    .eq(FavoriteFolder::getStatus, 1)
                    .ne(FavoriteFolder::getId, updateDTO.getId());
            boolean exists = favoriteFolderMapper.selectCount(queryWrapper) > 0;
            AssertUtils.isFalse(exists, ContentResultCodeEnum.FAVORITE_FOLDER_NAME_EXISTS);
        }

        // 更新收藏夹信息
        if (updateDTO.getName() != null) {
            folder.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            folder.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getIsPublic() != null) {
            folder.setIsPublic(updateDTO.getIsPublic());
        }
        if (updateDTO.getSortOrder() != null) {
            folder.setSortOrder(updateDTO.getSortOrder());
        }

        AssertUtils.isTrue(favoriteFolderMapper.updateById(folder) > 0, ContentResultCodeEnum.FAVORITE_FOLDER_UPDATE_FAILED);

        return BeanConvertUtil.to(folder, FavoriteFolderVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        AssertUtils.notNull(folder, ContentResultCodeEnum.FAVORITE_FOLDER_NOT_FOUND);
        AssertUtils.isTrue(folder.getIsDefault() == 0, ContentResultCodeEnum.FAVORITE_FOLDER_DEFAULT_CANNOT_DELETE);

        // 软删除收藏夹
        folder.setStatus(0);
        AssertUtils.isTrue(favoriteFolderMapper.updateById(folder) > 0, ContentResultCodeEnum.FAVORITE_FOLDER_DELETE_FAILED);

        // 删除收藏夹中的所有收藏项
        LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteItem::getFolderId, folderId);
        favoriteItemMapper.delete(queryWrapper);
    }

    @Override
    public List<FavoriteFolderVO> getUserFolders(Long userId) {
        LambdaQueryWrapper<FavoriteFolder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteFolder::getUserId, userId)
                .eq(FavoriteFolder::getStatus, 1)
                .orderByAsc(FavoriteFolder::getSortOrder);

        List<FavoriteFolder> folders = favoriteFolderMapper.selectList(queryWrapper);
        return BeanConvertUtil.toList(folders, FavoriteFolderVO.class);
    }

    @Override
    public FavoriteFolderVO getFolderDetail(Long folderId) {
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        AssertUtils.notNull(folder, "收藏夹不存在");
        AssertUtils.isTrue(folder.getStatus() == 1, "收藏夹已被删除");

        return BeanConvertUtil.to(folder, FavoriteFolderVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteItemVO addFavorite(FavoriteItemAddDTO addDTO) {
        // 检查内容是否存在
        Content content = contentService.getById(addDTO.getContentId());
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 检查内容状态是否为已发布
        AssertUtils.isTrue(content.getStatus() == ContentStatusEnum.PUBLISHED,
                ContentResultCodeEnum.CONTENT_NOT_PUBLISHED);

        // 如果未指定收藏夹，使用默认收藏夹
        Long folderId = addDTO.getFolderId();
        if (folderId == null) {
            folderId = getOrCreateDefaultFolder(addDTO.getUserId());
        } else {
            // 验证收藏夹是否存在且属于该用户
            FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
            AssertUtils.notNull(folder, ContentResultCodeEnum.FAVORITE_FOLDER_NOT_FOUND);
            AssertUtils.isTrue(folder.getUserId().equals(addDTO.getUserId()), ContentResultCodeEnum.FAVORITE_FOLDER_NO_PERMISSION);
            AssertUtils.isTrue(folder.getStatus() == 1, ContentResultCodeEnum.FAVORITE_FOLDER_DELETED);
        }

        // 检查是否已收藏到该收藏夹
        LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteItem::getFolderId, folderId)
                .eq(FavoriteItem::getContentId, addDTO.getContentId());
        boolean exists = favoriteItemMapper.selectCount(queryWrapper) > 0;
        AssertUtils.isFalse(exists, ContentResultCodeEnum.FAVORITE_ITEM_ALREADY_EXISTS);

        // 创建收藏项
        FavoriteItem item = new FavoriteItem();
        item.setFolderId(folderId);
        item.setUserId(addDTO.getUserId());
        item.setContentId(addDTO.getContentId());
        item.setNote(addDTO.getNote());
        item.setSortOrder(0);
        item.setStatus(1);

        AssertUtils.isTrue(favoriteItemMapper.insert(item) > 0, ContentResultCodeEnum.FAVORITE_ITEM_ADD_FAILED);

        // 更新收藏夹的内容数量
        updateFolderItemCount(folderId);

        // 更新内容统计表的收藏数
        statisticsService.incrementStatistics(addDTO.getContentId(), ActionTypeEnum.FAVORITE);

        // 构建返回VO
        return buildFavoriteItemVO(item, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteItemVO updateFavorite(FavoriteItemUpdateDTO updateDTO) {
        FavoriteItem item = favoriteItemMapper.selectById(updateDTO.getId());
        AssertUtils.notNull(item, ContentResultCodeEnum.FAVORITE_ITEM_NOT_FOUND);

        Long oldFolderId = item.getFolderId();

        // 更新收藏信息
        if (updateDTO.getFolderId() != null && !updateDTO.getFolderId().equals(item.getFolderId())) {
            // 验证新收藏夹
            FavoriteFolder newFolder = favoriteFolderMapper.selectById(updateDTO.getFolderId());
            AssertUtils.notNull(newFolder, ContentResultCodeEnum.FAVORITE_FOLDER_NOT_FOUND);
            AssertUtils.isTrue(newFolder.getUserId().equals(item.getUserId()), ContentResultCodeEnum.FAVORITE_FOLDER_NO_PERMISSION);
            AssertUtils.isTrue(newFolder.getStatus() == 1, ContentResultCodeEnum.FAVORITE_FOLDER_DELETED);

            // 检查新收藏夹中是否已存在该内容
            LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FavoriteItem::getFolderId, updateDTO.getFolderId())
                    .eq(FavoriteItem::getContentId, item.getContentId());
            boolean exists = favoriteItemMapper.selectCount(queryWrapper) > 0;
            AssertUtils.isFalse(exists, ContentResultCodeEnum.FAVORITE_ITEM_ALREADY_EXISTS);

            item.setFolderId(updateDTO.getFolderId());
        }
        if (updateDTO.getNote() != null) {
            item.setNote(updateDTO.getNote());
        }
        if (updateDTO.getSortOrder() != null) {
            item.setSortOrder(updateDTO.getSortOrder());
        }

        AssertUtils.isTrue(favoriteItemMapper.updateById(item) > 0, ContentResultCodeEnum.FAVORITE_ITEM_UPDATE_FAILED);

        // 如果移动了收藏夹，更新两个收藏夹的内容数量
        if (updateDTO.getFolderId() != null && !updateDTO.getFolderId().equals(oldFolderId)) {
            updateFolderItemCount(oldFolderId);
            updateFolderItemCount(updateDTO.getFolderId());
        }

        // 构建返回VO
        Content content = contentService.getById(item.getContentId());
        return buildFavoriteItemVO(item, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long itemId) {
        FavoriteItem item = favoriteItemMapper.selectById(itemId);
        AssertUtils.notNull(item, ContentResultCodeEnum.FAVORITE_ITEM_NOT_FOUND);

        Long folderId = item.getFolderId();
        Long contentId = item.getContentId();

        // 删除收藏项
        AssertUtils.isTrue(favoriteItemMapper.deleteById(itemId) > 0, ContentResultCodeEnum.FAVORITE_ITEM_REMOVE_FAILED);

        // 更新收藏夹的内容数量
        updateFolderItemCount(folderId);

        // 更新内容统计表的收藏数
        statisticsService.decrementStatistics(contentId, ActionTypeEnum.FAVORITE);
    }

    @Override
    public PageVO<FavoriteItemVO> getFolderItems(Long folderId, PageDTO<Void> pageDTO) {
        // 验证收藏夹是否存在
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        AssertUtils.notNull(folder, ContentResultCodeEnum.FAVORITE_FOLDER_NOT_FOUND);
        AssertUtils.isTrue(folder.getStatus() == 1, ContentResultCodeEnum.FAVORITE_FOLDER_DELETED);

        // 计算分页参数
        int offset = (pageDTO.getPage() - 1) * pageDTO.getPageSize();
        int limit = pageDTO.getPageSize();

        // 使用 XML 联表查询
        List<FavoriteItemVO> items = favoriteItemMapper.selectFavoriteItemsWithContentByFolderId(
                folderId, offset, limit
        );

        // 批量填充作者昵称和封面URL
        fillAuthorNicknames(items);
        fillCoverUrls(items);

        // 查询总数
        Long total = favoriteItemMapper.countFavoriteItemsByFolderId(folderId);

        // 构建分页结果
        PageVO<FavoriteItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(items);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageDTO.getPage());
        pageVO.setPageSize(pageDTO.getPageSize());

        return pageVO;
    }

    @Override
    public PageVO<FavoriteItemVO> getUserFavorites(Long userId, PageDTO<Void> pageDTO) {
        // 计算分页参数
        int offset = (pageDTO.getPage() - 1) * pageDTO.getPageSize();
        int limit = pageDTO.getPageSize();

        // 使用 XML 联表查询
        List<FavoriteItemVO> items = favoriteItemMapper.selectFavoriteItemsWithContentByUserId(
                userId, offset, limit
        );

        // 批量填充作者昵称和封面URL
        fillAuthorNicknames(items);
        fillCoverUrls(items);

        // 查询总数
        Long total = favoriteItemMapper.countFavoriteItemsByUserId(userId);

        // 构建分页结果
        PageVO<FavoriteItemVO> pageVO = new PageVO<>();
        pageVO.setRecords(items);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageDTO.getPage());
        pageVO.setPageSize(pageDTO.getPageSize());

        return pageVO;
    }

    @Override
    public Long checkFavorite(Long userId, Long contentId) {
        LambdaQueryWrapper<FavoriteItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteItem::getUserId, userId)
                .eq(FavoriteItem::getContentId, contentId)
                .select(FavoriteItem::getId);

        FavoriteItem favoriteItem = favoriteItemMapper.selectOne(queryWrapper);
        return favoriteItem != null ? favoriteItem.getId() : null;
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

            AssertUtils.isTrue(favoriteFolderMapper.insert(defaultFolder) > 0, ContentResultCodeEnum.FAVORITE_DEFAULT_FOLDER_CREATE_FAILED);
        }

        return defaultFolder.getId();
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

    /**
     * 构建收藏项VO
     */
    private FavoriteItemVO buildFavoriteItemVO(FavoriteItem item, Content content) {
        FavoriteItemVO vo = BeanConvertUtil.to(item, FavoriteItemVO.class);
        if (content != null) {
            vo.setContentTitle(content.getTitle());
            vo.setContentType(content.getType());
            vo.setContentDescription(content.getDescription());
            vo.setCoverFileId(content.getCoverFileId());
            vo.setUploadedBy(content.getUploadedBy());
            vo.setContentStatus(content.getStatus());
            vo.setContentCreateTime(content.getCreateTime());
            vo.setContentUpdateTime(content.getUpdateTime());
        }
        return vo;
    }

    /**
     * 批量填充作者昵称
     */
    private void fillAuthorNicknames(List<FavoriteItemVO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }

        try {
            // 收集所有作者ID
            List<Long> userIds = items.stream()
                    .map(FavoriteItemVO::getUploadedBy)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();

            if (CollUtil.isEmpty(userIds)) {
                return;
            }

            // 批量查询用户信息
            Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(userIds);

            if (userMap != null && !userMap.isEmpty()) {
                // 填充昵称和头像
                items.forEach(item -> {
                    UserProfileSimpleVO userProfile = userMap.get(item.getUploadedBy());
                    if (userProfile != null) {
                        item.setAuthorNickname(userProfile.getNickname());
                        item.setAuthorAvatarUrl(userProfile.getAvatarUrl());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量查询用户信息失败，跳过填充作者昵称", e);
        }
    }

    /**
     * 批量填充封面URL
     */
    private void fillCoverUrls(List<FavoriteItemVO> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }

        try {
            // 收集所有封面文件ID
            List<String> coverFileIds = items.stream()
                    .map(FavoriteItemVO::getCoverFileId)
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
                items.forEach(item -> {
                    String coverFileId = item.getCoverFileId();
                    if (StrUtil.isNotBlank(coverFileId)) {
                        String coverUrl = urlMap.get(coverFileId);
                        if (StrUtil.isNotBlank(coverUrl)) {
                            item.setCoverUrl(coverUrl);
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量获取封面URL失败，跳过填充封面URL", e);
        }
    }
}
