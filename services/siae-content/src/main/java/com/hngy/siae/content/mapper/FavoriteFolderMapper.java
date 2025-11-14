package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.entity.FavoriteFolder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏夹表 Mapper
 *
 * @author KEYKB
 * @description 针对表【favorite_folder(收藏夹表)】的数据库操作Mapper
 */
@Mapper
public interface FavoriteFolderMapper extends BaseMapper<FavoriteFolder> {
}
