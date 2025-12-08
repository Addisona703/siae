package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.interaction.StatisticsDTO;
import com.hngy.siae.content.dto.response.statistics.StatisticsVO;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.content.permissions.ContentPermissions.*;

/**
 * 内容统计管理控制器
 *
 * @author KEYKB
 */
@Slf4j
@Tag(name = "统计管理", description = "内容统计数据的查询和更新操作")
@RestController
@RequestMapping("/statistics")
@Validated
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;


    @Operation(summary = "创建内容统计记录", description = "为新创建的内容初始化统计记录")
    @PostMapping("/{contentId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_CREATE + "')")
    public Result<Void> createStatistics(
            @Parameter(description = "内容ID，用于唯一标识要创建统计记录的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId) {
        log.info("创建内容统计记录，内容ID: {}", contentId);
        statisticsService.addContentStatistics(contentId);
        return Result.success();
    }


    @Operation(summary = "获取内容统计信息", description = "根据内容ID获取统计数据，包括浏览量、点赞数、收藏数、评论数等")
    @GetMapping("/{contentId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<StatisticsVO> getStatistics(
            @Parameter(description = "内容ID，用于唯一标识要查询统计信息的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId) {
        StatisticsVO statisticsVO = statisticsService.getStatistics(contentId);
        return Result.success(statisticsVO);
    }


    @Operation(summary = "更新内容统计信息", description = "更新指定内容的统计数据")
    @PutMapping("/{contentId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_UPDATE + "')")
    public Result<StatisticsVO> updateStatistics(
            @Parameter(description = "内容ID，用于唯一标识要更新统计信息的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId,
            @Parameter(description = "统计信息更新请求数据，包含要更新的统计字段", required = true)
            @Valid @RequestBody StatisticsDTO statisticsDTO) {
        log.info("更新内容统计信息，内容ID: {}, 数据: {}", contentId, statisticsDTO);
        StatisticsVO statisticsVO = statisticsService.updateStatistics(contentId, statisticsDTO);
        return Result.success(statisticsVO);
    }


    @Operation(summary = "增量统计", description = "对指定内容的统计数据进行增量操作（浏览+1、点赞+1、收藏+1）")
    @PostMapping("/{contentId}/increment")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_UPDATE + "')")
    public Result<Void> incrementStatistics(
            @Parameter(description = "内容ID，用于唯一标识要增量统计的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId,
            @Parameter(description = "操作类型：0=浏览(VIEW), 1=点赞(LIKE), 2=收藏(FAVORITE)", required = true, example = "1")
            @NotNull @RequestParam Integer actionType) {
        ActionTypeEnum actionTypeEnum = ActionTypeEnum.values()[actionType];
        log.info("增量统计，内容ID: {}, 操作类型: {}", contentId, actionTypeEnum.getDescription());
        statisticsService.incrementStatistics(contentId, actionTypeEnum);
        return Result.success();
    }


    @Operation(summary = "递减统计", description = "对指定内容的统计数据进行递减操作（点赞-1、收藏-1），最小值为0")
    @PostMapping("/{contentId}/decrement")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_UPDATE + "')")
    public Result<Void> decrementStatistics(
            @Parameter(description = "内容ID，用于唯一标识要递减统计的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId,
            @Parameter(description = "操作类型：0=浏览(VIEW), 1=点赞(LIKE), 2=收藏(FAVORITE)", required = true, example = "1")
            @NotNull @RequestParam Integer actionType) {
        ActionTypeEnum actionTypeEnum = ActionTypeEnum.values()[actionType];
        log.info("递减统计，内容ID: {}, 操作类型: {}", contentId, actionTypeEnum.getDescription());
        statisticsService.decrementStatistics(contentId, actionTypeEnum);
        return Result.success();
    }

    // ==================== 聚合统计接口（用于前端图表） ====================

    /**
     * 获取统计汇总数据
     * 用于首页统计卡片展示
     *
     * @return 统计汇总数据
     */
    @Operation(summary = "获取统计汇总数据", description = "获取全站统计汇总数据，包括总浏览量、总点赞数、总收藏数、总评论数、今日数据等，用于首页统计卡片")
    @GetMapping("/summary")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<com.hngy.siae.content.dto.response.statistics.StatisticsSummaryVO> getStatisticsSummary() {
        log.info("获取统计汇总数据");
        com.hngy.siae.content.dto.response.statistics.StatisticsSummaryVO summary = statisticsService.getStatisticsSummary();
        return Result.success(summary);
    }

    /**
     * 按内容类型统计
     * 用于饼图展示各类型内容的分布
     *
     * @return 内容类型统计列表
     */
    @Operation(summary = "按内容类型统计", description = "统计各内容类型（文章、笔记、问答、文件、视频）的数量和互动数据，用于饼图或柱状图展示")
    @GetMapping("/by-type")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<java.util.List<com.hngy.siae.content.dto.response.statistics.ContentTypeStatisticsVO>> getContentTypeStatistics() {
        log.info("获取内容类型统计数据");
        java.util.List<com.hngy.siae.content.dto.response.statistics.ContentTypeStatisticsVO> statistics = statisticsService.getContentTypeStatistics();
        return Result.success(statistics);
    }

    /**
     * 按分类统计
     * 用于饼图、柱状图展示各分类的内容分布
     *
     * @return 分类统计列表
     */
    @Operation(summary = "按分类统计", description = "统计各分类的内容数量和互动数据，用于饼图或柱状图展示")
    @GetMapping("/by-category")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<java.util.List<com.hngy.siae.content.dto.response.category.CategoryStatisticsVO>> getCategoryStatistics() {
        log.info("获取分类统计数据");
        java.util.List<com.hngy.siae.content.dto.response.category.CategoryStatisticsVO> statistics = statisticsService.getCategoryStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取趋势数据
     * 用于折线图展示数据趋势
     *
     * @param days 天数（7、30、90等），默认7天
     * @return 趋势数据
     */
    @Operation(summary = "获取趋势数据", description = "获取指定天数内的统计趋势数据，用于折线图展示浏览量、点赞数、收藏数、评论数的变化趋势")
    @GetMapping("/trend")
    @SiaeAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<com.hngy.siae.content.dto.response.statistics.TrendDataVO> getTrendData(
            @Parameter(description = "统计天数，默认7天，可选7、30、90等", example = "7")
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取趋势数据，天数: {}", days);
        com.hngy.siae.content.dto.response.statistics.TrendDataVO trendData = statisticsService.getTrendData(days);
        return Result.success(trendData);
    }
}