package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.dto.response.ContentDetailDTO;
import com.hngy.siae.content.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 内容表 Mapper
 *
 * @author KEYKB
 * @description 针对表【content(内容主表)】的数据库操作Mapper
 * @createDate 2025-05-15 16:48:34
 * @Entity com.hngy.siae.content.entity.Content
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 查询内容详情（包含分类、标签、统计信息）
     *
     * @param contentId 内容ID
     * @return 内容详情DTO
     */
    ContentDetailDTO selectContentDetailById(@Param("contentId") Long contentId);
}
