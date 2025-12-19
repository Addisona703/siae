package com.hngy.siae.content.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderCreateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderUpdateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemAddDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemUpdateDTO;
import com.hngy.siae.content.dto.response.favorite.FavoriteFolderVO;
import com.hngy.siae.content.dto.response.favorite.FavoriteItemVO;

import java.util.List;

/**
 * 收藏服务接口
 *
 * @author KEYKB
 */
public interface FavoriteService {

    /**
     * 创建收藏夹
     *
     * @param createDTO 创建收藏夹请求DTO
     * @return 创建的收藏夹信息
     */
    FavoriteFolderVO createFolder(FavoriteFolderCreateDTO createDTO);

    /**
     * 更新收藏夹
     *
     * @param updateDTO 更新收藏夹请求DTO
     * @return 更新后的收藏夹信息
     */
    FavoriteFolderVO updateFolder(FavoriteFolderUpdateDTO updateDTO);

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     */
    void deleteFolder(Long folderId);

    /**
     * 查询用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<FavoriteFolderVO> getUserFolders(Long userId);

    /**
     * 查询收藏夹详情
     *
     * @param folderId 收藏夹ID
     * @return 收藏夹详情
     */
    FavoriteFolderVO getFolderDetail(Long folderId);

    /**
     * 添加收藏
     *
     * @param addDTO 添加收藏请求DTO
     * @return 收藏信息
     */
    FavoriteItemVO addFavorite(FavoriteItemAddDTO addDTO);

    /**
     * 更新收藏
     *
     * @param updateDTO 更新收藏请求DTO
     * @return 更新后的收藏信息
     */
    FavoriteItemVO updateFavorite(FavoriteItemUpdateDTO updateDTO);

    /**
     * 取消收藏
     *
     * @param itemId 收藏ID
     */
    void removeFavorite(Long itemId);

    /**
     * 查询收藏夹中的内容列表
     *
     * @param folderId 收藏夹ID
     * @param pageDTO 分页参数
     * @return 收藏内容分页列表
     */
    PageVO<FavoriteItemVO> getFolderItems(Long folderId, PageDTO<Void> pageDTO);

    /**
     * 查询用户的所有收藏
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 收藏内容分页列表
     */
    PageVO<FavoriteItemVO> getUserFavorites(Long userId, PageDTO<Void> pageDTO);

    /**
     * 检查内容是否已收藏
     *
     * @param userId 用户ID
     * @param contentId 内容ID
     * @return 如果已收藏则返回收藏ID，否则返回null
     */
    Long checkFavorite(Long userId, Long contentId);
}
