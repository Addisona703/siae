package com.hngy.siae.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hngy.siae.content.dto.response.*;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.entity.Statistics;
import com.hngy.siae.content.mapper.StatisticsMapper;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.hngy.siae.core.asserts.AssertUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl
        extends ServiceImpl<StatisticsMapper, Statistics>
        implements StatisticsService {


    @Override
    public void addContentStatistics(Long contentId) {
        Statistics statistics = new Statistics();
        statistics.setContentId(contentId);

        AssertUtils.isTrue(this.save(statistics), ContentResultCodeEnum.STATISTICS_CREATE_FAILED);
    }


    @Override
    public void incrementStatistics(Long contentId, ActionTypeEnum actionTypeEnum) {
        // 检查统计表是否存在
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtils.notNull(statistics, ContentResultCodeEnum.STATISTICS_NOT_FOUND);

        // 构建更新SQL
        UpdateWrapper<Statistics> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("content_id", contentId);
    
        switch (actionTypeEnum) {
            case VIEW -> updateWrapper.setSql("view_count = view_count + 1");
            case LIKE -> updateWrapper.setSql("like_count = like_count + 1");
            case FAVORITE -> updateWrapper.setSql("favorite_count = favorite_count + 1");
            default -> throw new IllegalArgumentException("不支持的操作类型: " + actionTypeEnum);
        }
    
        this.update(updateWrapper);
    }


    @Override
    public void decrementStatistics(Long contentId, ActionTypeEnum actionTypeEnum) {
        // 检查统计表是否存在
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtils.notNull(statistics, ContentResultCodeEnum.STATISTICS_NOT_FOUND);

        // 构建更新SQL
        UpdateWrapper<Statistics> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("content_id", contentId);
    
        switch (actionTypeEnum) {
            case VIEW -> updateWrapper.setSql("view_count = GREATEST(view_count - 1, 0)");
            case LIKE -> updateWrapper.setSql("like_count = GREATEST(like_count - 1, 0)");
            case FAVORITE -> updateWrapper.setSql("favorite_count = GREATEST(favorite_count - 1, 0)");
            default -> throw new IllegalArgumentException("不支持的操作类型: " + actionTypeEnum);
        }
    
        this.update(updateWrapper);
    }


    @Override
    public StatisticsVO getStatistics(Long contentId) {
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtils.notNull(statistics, ContentResultCodeEnum.STATISTICS_NOT_FOUND);

        return BeanConvertUtil.to(statistics, StatisticsVO.class);
    }


    @Override
    public StatisticsVO updateStatistics(Long contentId, StatisticsDTO statisticsDTO) {
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtils.notNull(statistics, ContentResultCodeEnum.STATISTICS_NOT_FOUND);

        BeanConvertUtil.to(statisticsDTO, statistics);
        AssertUtils.isTrue(this.updateById(statistics), ContentResultCodeEnum.STATISTICS_UPDATE_FAILED);

        return BeanConvertUtil.to(statistics, StatisticsVO.class);
    }

    @Override
    public StatisticsSummaryVO getStatisticsSummary() {
        // 查询总体统计
        Map<String, Object> summary = baseMapper.selectStatisticsSummary();
        
        // 查询今日统计
        Map<String, Object> todayStats = baseMapper.selectTodayStatistics();

        return StatisticsSummaryVO.builder()
                .totalViews(summary != null ? getLongValue(summary, "totalViews") : 0L)
                .totalLikes(summary != null ? getLongValue(summary, "totalLikes") : 0L)
                .totalFavorites(summary != null ? getLongValue(summary, "totalFavorites") : 0L)
                .totalComments(summary != null ? getLongValue(summary, "totalComments") : 0L)
                .totalContents(summary != null ? getLongValue(summary, "totalContents") : 0L)
                .todayViews(todayStats != null ? getLongValue(todayStats, "todayViews") : 0L)
                .todayLikes(todayStats != null ? getLongValue(todayStats, "todayLikes") : 0L)
                .todayFavorites(todayStats != null ? getLongValue(todayStats, "todayFavorites") : 0L)
                .todayComments(todayStats != null ? getLongValue(todayStats, "todayComments") : 0L)
                .build();
    }

    @Override
    public List<ContentTypeStatisticsVO> getContentTypeStatistics() {
        List<ContentTypeStatisticsVO> statistics = baseMapper.selectContentTypeStatistics();
        
        // 为每个类型添加中文名称，使用枚举映射
        statistics.forEach(stat -> {
            String typeName = "未知类型";
            try {
                // contentType 是数字字符串，需要转换为int
                int typeCode = Integer.parseInt(stat.getContentType());
                for (com.hngy.siae.content.enums.ContentTypeEnum type : com.hngy.siae.content.enums.ContentTypeEnum.values()) {
                    if (type.getCode() == typeCode) {
                        typeName = switch (type) {
                            case ARTICLE -> "文章";
                            case NOTE -> "笔记";
                            case QUESTION -> "问答";
                            case FILE -> "文件";
                            case VIDEO -> "视频";
                        };
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，保持原值
                typeName = stat.getContentType();
            }
            stat.setTypeName(typeName);
        });
        
        return statistics;
    }

    @Override
    public List<CategoryStatisticsVO> getCategoryStatistics() {
        return baseMapper.selectCategoryStatistics();
    }

    @Override
    public TrendDataVO getTrendData(Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }

        List<Map<String, Object>> trendData = baseMapper.selectTrendData(days);
        
        List<String> dates = new ArrayList<>();
        List<Long> viewCounts = new ArrayList<>();
        List<Long> likeCounts = new ArrayList<>();
        List<Long> favoriteCounts = new ArrayList<>();
        List<Long> commentCounts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 填充数据
        for (Map<String, Object> data : trendData) {
            Object dateObj = data.get("date");
            String dateStr;
            if (dateObj instanceof LocalDate) {
                dateStr = ((LocalDate) dateObj).format(formatter);
            } else {
                dateStr = dateObj.toString();
            }
            
            dates.add(dateStr);
            viewCounts.add(getLongValue(data, "viewCount"));
            likeCounts.add(getLongValue(data, "likeCount"));
            favoriteCounts.add(getLongValue(data, "favoriteCount"));
            commentCounts.add(getLongValue(data, "commentCount"));
        }

        return TrendDataVO.builder()
                .dates(dates)
                .viewCounts(viewCounts)
                .likeCounts(likeCounts)
                .favoriteCounts(favoriteCounts)
                .commentCounts(commentCounts)
                .build();
    }

    /**
     * 从Map中安全获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}