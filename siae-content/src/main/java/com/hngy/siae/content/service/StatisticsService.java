package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.common.enums.ActionTypeEnum;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.dto.response.StatisticsVO;
import com.hngy.siae.content.entity.Statistics;

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
     * @return {@link Result }<{@link StatisticsVO }>
     */
    Result<StatisticsVO> getStatistics(Long contentId);

    /**
     * 更新统计信息
     *
     * @param contentId     内容ID
     * @param statisticsDTO 统计数据
     * @return {@link Result }<{@link StatisticsVO }>
     */
    Result<StatisticsVO> updateStatistics(Long contentId, StatisticsDTO statisticsDTO);
}