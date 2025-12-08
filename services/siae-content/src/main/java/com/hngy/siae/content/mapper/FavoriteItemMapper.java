package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.dto.response.favorite.FavoriteItemVO;
import com.hngy.siae.content.entity.FavoriteItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏内容表 Mapper
 *
 * @author KEYKB
 * @description 针对表【favorite_item(收藏内容表)】的数据库操作Mapper
 */
@Mapper
public interface FavoriteItemMapper extends BaseMapper<FavoriteItem> {

    /**
     * 查询收藏夹中的内容列表（关联内容表）
     *
     * @param folderId 收藏夹ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏内容列表
     */
    List<FavoriteItemVO> selectFavoriteItemsWithContentByFolderId(
            @Param("folderId") Long folderId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 查询用户所有收藏（关联内容表）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏内容列表
     */
    List<FavoriteItemVO> selectFavoriteItemsWithContentByUserId(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计收藏夹中的内容数量
     *
     * @param folderId 收藏夹ID
     * @return 内容数量
     */
    Long countFavoriteItemsByFolderId(@Param("folderId") Long folderId);

    /**
     * 统计用户所有收藏数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    Long countFavoriteItemsByUserId(@Param("userId") Long userId);
}
