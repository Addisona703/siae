package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.content.dto.response.*;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.entity.Statistics;

import java.util.List;

/**
 * 统计服务接口
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface StatisticsService extends IService<Statistics> {

    /**
     * 添加内容的统计信息表
     *
     * @param contentId 内容ID
     */
    void addContentStatistics(Long contentId);

    /**
     * 增量统计
     *
     * @param contentId      内容ID
     * @param actionTypeEnum 动作类型枚举
     */
    void incrementStatistics(Long contentId, ActionTypeEnum actionTypeEnum);

    /**
     * 递减统计
     *
     * @param contentId      内容ID
     * @param actionTypeEnum 动作类型枚举
     */
    void decrementStatistics(Long contentId, ActionTypeEnum actionTypeEnum);

    /**
     * 获取统计数据
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    StatisticsVO getStatistics(Long contentId);

    /**
     * 更新统计信息
     *
     * @param contentId     内容ID
     * @param statisticsDTO 统计数据
     * @return 更新后的统计信息
     */
    StatisticsVO updateStatistics(Long contentId, StatisticsDTO statisticsDTO);

    /**
     * 获取统计汇总数据
     *
     * @return 统计汇总数据
     */
    StatisticsSummaryVO getStatisticsSummary();

    /**
     * 按内容类型统计
     *
     * @return 内容类型统计列表
     */
    List<ContentTypeStatisticsVO> getContentTypeStatistics();

    /**
     * 按分类统计
     *
     * @return 分类统计列表
     */
    List<CategoryStatisticsVO> getCategoryStatistics();

    /**
     * 获取趋势数据
     *
     * @param days 天数（7、30、90等）
     * @return 趋势数据
     */
    TrendDataVO getTrendData(Integer days);
}