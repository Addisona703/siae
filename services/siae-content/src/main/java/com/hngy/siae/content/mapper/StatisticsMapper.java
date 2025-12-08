package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.content.dto.response.category.CategoryStatisticsVO;
import com.hngy.siae.content.dto.response.statistics.ContentTypeStatisticsVO;
import com.hngy.siae.content.dto.response.content.HotContentVO;
import com.hngy.siae.content.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 内容统计表 Mapper
 *
 * @author 31833
 */
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {

    /**
     * 分页查询热门内容
     *
     * @param page 分页对象
     * @param categoryId 分类ID
     * @param type 内容类型
     * @param sortBy 排序字段
     * @param publishedStatus 发布状态
     * @return 热门内容分页结果
     */
    IPage<HotContentVO> selectHotContentPage(Page<HotContentVO> page,
                                             @Param("categoryId") Long categoryId,
                                             @Param("type") Integer type,
                                             @Param("sortBy") String sortBy,
                                             @Param("publishedStatus") Integer publishedStatus);

    /**
     * 查询统计汇总数据
     *
     * @return 汇总统计数据
     */
    @Select("SELECT " +
            "SUM(view_count) as totalViews, " +
            "SUM(like_count) as totalLikes, " +
            "SUM(favorite_count) as totalFavorites, " +
            "SUM(comment_count) as totalComments, " +
            "COUNT(DISTINCT content_id) as totalContents " +
            "FROM statistics")
    Map<String, Object> selectStatisticsSummary();

    /**
     * 查询今日统计数据
     *
     * @return 今日统计数据
     */
    @Select("SELECT " +
            "SUM(view_count) as todayViews, " +
            "SUM(like_count) as todayLikes, " +
            "SUM(favorite_count) as todayFavorites, " +
            "SUM(comment_count) as todayComments " +
            "FROM statistics " +
            "WHERE DATE(update_time) = CURDATE()")
    Map<String, Object> selectTodayStatistics();

    /**
     * 按内容类型统计
     *
     * @return 内容类型统计列表
     */
    @Select("SELECT " +
            "c.type as contentType, " +
            "COUNT(c.id) as contentCount, " +
            "COALESCE(SUM(s.view_count), 0) as totalViews, " +
            "COALESCE(SUM(s.like_count), 0) as totalLikes " +
            "FROM content c " +
            "LEFT JOIN statistics s ON c.id = s.content_id " +
            "WHERE c.status = 2 " +
            "GROUP BY c.type")
    List<ContentTypeStatisticsVO> selectContentTypeStatistics();

    /**
     * 按分类统计
     *
     * @return 分类统计列表
     */
    @Select("SELECT " +
            "c.category_id as categoryId, " +
            "cat.name as categoryName, " +
            "COUNT(c.id) as contentCount, " +
            "COALESCE(SUM(s.view_count), 0) as totalViews, " +
            "COALESCE(SUM(s.like_count), 0) as totalLikes, " +
            "COALESCE(SUM(s.favorite_count), 0) as totalFavorites, " +
            "COALESCE(SUM(s.comment_count), 0) as totalComments " +
            "FROM content c " +
            "LEFT JOIN statistics s ON c.id = s.content_id " +
            "LEFT JOIN category cat ON c.category_id = cat.id " +
            "WHERE c.status = 2 AND c.category_id IS NOT NULL " +
            "GROUP BY c.category_id, cat.name " +
            "ORDER BY contentCount DESC")
    List<CategoryStatisticsVO> selectCategoryStatistics();

    /**
     * 查询指定天数内的趋势数据
     *
     * @param days 天数
     * @return 趋势数据列表
     */
    @Select("SELECT " +
            "DATE(update_time) as date, " +
            "SUM(view_count) as viewCount, " +
            "SUM(like_count) as likeCount, " +
            "SUM(favorite_count) as favoriteCount, " +
            "SUM(comment_count) as commentCount " +
            "FROM statistics " +
            "WHERE update_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(update_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectTrendData(@Param("days") Integer days);
}
