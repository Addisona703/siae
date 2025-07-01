package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.common.enums.ActionTypeEnum;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.dto.response.StatisticsVO;
import com.hngy.siae.content.entity.Statistics;
import com.hngy.siae.content.mapper.StatisticsMapper;
import com.hngy.siae.content.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        AssertUtil.isTrue(this.save(statistics), "创建统计表失败");
    }


    @Override
    public void incrementStatistics(Long contentId, ActionTypeEnum actionTypeEnum) {
        // 检查统计表是否存在
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtil.notNull(statistics, "统计表不存在");

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
        AssertUtil.notNull(statistics, "统计表不存在");

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
    public Result<StatisticsVO> getStatistics(Long contentId) {
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtil.notNull(statistics, "统计表不存在");

        StatisticsVO statisticsVO = BeanUtil.copyProperties(statistics, StatisticsVO.class);
        return Result.success(statisticsVO);
    }


    @Override
    public Result<StatisticsVO> updateStatistics(Long contentId, StatisticsDTO statisticsDTO) {
        Statistics statistics = this.lambdaQuery().eq(Statistics::getContentId, contentId).one();
        AssertUtil.notNull(statistics, "统计表不存在");

        BeanUtil.copyProperties(statisticsDTO, statistics);
        AssertUtil.isTrue(this.updateById(statistics), "修改统计表信息失败");

        StatisticsVO vo = BeanUtil.copyProperties(statistics, StatisticsVO.class);
        return Result.success(vo);
    }
}