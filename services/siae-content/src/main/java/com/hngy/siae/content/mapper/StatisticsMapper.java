package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内容统计表 Mapper
 *
 * @author 31833
 */
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {

    /**
     * 查询热门内容列表
     */
    @Select({
            "<script>",
            "SELECT",
            "    c.id AS contentId,",
            "    c.title AS title,",
            "    c.description AS description,",
            "    COALESCE(art.cover_url, vid.cover_url) AS coverUrl,",
            "    s.view_count AS viewCount,",
            "    s.like_count AS likeCount,",
            "    s.favorite_count AS favoriteCount,",
            "    s.comment_count AS commentCount",
            "FROM content c",
            "JOIN statistics s ON s.content_id = c.id",
            "LEFT JOIN article art ON art.content_id = c.id",
            "LEFT JOIN video vid ON vid.content_id = c.id",
            "WHERE c.status = #{publishedStatus}",
            "<if test='categoryId != null'>",
            "    AND c.category_id = #{categoryId}",
            "</if>",
            "<if test='type != null'>",
            "    AND c.type = #{type}",
            "</if>",
            "<choose>",
            "    <when test='sortBy == \"likeCount\"'>",
            "        ORDER BY s.like_count DESC",
            "    </when>",
            "    <when test='sortBy == \"favoriteCount\"'>",
            "        ORDER BY s.favorite_count DESC",
            "    </when>",
            "    <when test='sortBy == \"commentCount\"'>",
            "        ORDER BY s.comment_count DESC",
            "    </when>",
            "    <when test='sortBy == \"createTime\"'>",
            "        ORDER BY c.create_time DESC",
            "    </when>",
            "    <otherwise>",
            "        ORDER BY s.view_count DESC",
            "    </otherwise>",
            "</choose>",
            "LIMIT #{pageSize} OFFSET #{offset}",
            "</script>"
    })
    List<HotContentVO> selectHotContent(@Param("categoryId") Long categoryId,
                                        @Param("type") Integer type,
                                        @Param("sortBy") String sortBy,
                                        @Param("pageSize") Integer pageSize,
                                        @Param("offset") Integer offset,
                                        @Param("publishedStatus") Integer publishedStatus);
}
