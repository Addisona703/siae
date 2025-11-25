package com.hngy.siae.user.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.response.statistics.*;
import com.hngy.siae.user.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计控制器
 *
 * @author SIAE Team
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "统计管理", description = "用户统计分析接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "获取成员概览统计", description = "获取成员总体概况数据")
    public Result<MemberOverviewVO> getMemberOverview() {
        log.info("Get member overview statistics");
        MemberOverviewVO data = statisticsService.getMemberOverview();
        return Result.success(data);
    }

    @GetMapping("/departments")
    @Operation(summary = "获取部门分布统计", description = "获取各部门的成员分布情况")
    public Result<List<DepartmentStatVO>> getDepartmentStats() {
        log.info("Get department statistics");
        List<DepartmentStatVO> data = statisticsService.getDepartmentStats();
        return Result.success(data);
    }

    @GetMapping("/awards/overview")
    @Operation(summary = "获取获奖概览统计", description = "获取获奖情况的总体概况")
    public Result<AwardOverviewVO> getAwardOverview() {
        log.info("Get award overview statistics");
        AwardOverviewVO data = statisticsService.getAwardOverview();
        return Result.success(data);
    }

    @GetMapping("/awards/by-level")
    @Operation(summary = "获取按奖项等级分布", description = "获取按奖项等级分组的获奖统计")
    public Result<List<AwardDistributionVO>> getAwardsByLevel() {
        log.info("Get awards by level");
        List<AwardDistributionVO> data = statisticsService.getAwardsByLevel();
        return Result.success(data);
    }

    @GetMapping("/awards/by-type")
    @Operation(summary = "获取按奖项类型分布", description = "获取按奖项类型分组的获奖统计")
    public Result<List<AwardDistributionVO>> getAwardsByType() {
        log.info("Get awards by type");
        List<AwardDistributionVO> data = statisticsService.getAwardsByType();
        return Result.success(data);
    }

    @GetMapping("/awards/trend")
    @Operation(summary = "获取获奖趋势（含等级分布）", description = "获取指定时间区间内获奖数量的趋势，包含各等级获奖数量")
    public Result<List<AwardTrendDetailVO>> getAwardTrend(
            @Parameter(description = "统计周期：month(按月) 或 year(按年)", example = "month")
            @RequestParam(defaultValue = "month") String period,
            @Parameter(description = "开始时间（格式：YYYY-MM 或 YYYY，根据period决定）", example = "2024-01")
            @RequestParam(required = false) String startPeriod,
            @Parameter(description = "结束时间（格式：YYYY-MM 或 YYYY，根据period决定）", example = "2024-12")
            @RequestParam(required = false) String endPeriod) {
        log.info("Get award trend: period={}, startPeriod={}, endPeriod={}", period, startPeriod, endPeriod);
        List<AwardTrendDetailVO> data = statisticsService.getAwardTrendDetail(period, startPeriod, endPeriod);
        return Result.success(data);
    }

    @GetMapping("/awards/top")
    @Operation(summary = "获取获奖排行榜", description = "获取获奖最多的成员排行榜")
    public Result<List<AwardRankVO>> getAwardRanking(
            @Parameter(description = "返回TOP N，最大100", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Get award ranking: limit={}", limit);
        List<AwardRankVO> data = statisticsService.getAwardRanking(limit);
        return Result.success(data);
    }

    @GetMapping("/demographics/gender")
    @Operation(summary = "获取性别分布", description = "获取成员的性别分布统计")
    public Result<List<GenderStatVO>> getGenderStats() {
        log.info("Get gender statistics");
        List<GenderStatVO> data = statisticsService.getGenderStats();
        return Result.success(data);
    }

    @GetMapping("/demographics/grade")
    @Operation(summary = "获取年级分布", description = "获取成员的年级分布统计")
    public Result<List<GradeStatVO>> getGradeStats() {
        log.info("Get grade statistics");
        List<GradeStatVO> data = statisticsService.getGradeStats();
        return Result.success(data);
    }

    @GetMapping("/demographics/major")
    @Operation(summary = "获取专业分布", description = "获取成员的专业分布统计")
    public Result<List<MajorStatVO>> getMajorStats() {
        log.info("Get major statistics");
        List<MajorStatVO> data = statisticsService.getMajorStats();
        return Result.success(data);
    }

    @GetMapping("/positions")
    @Operation(summary = "获取职位分布", description = "获取各职位的人数分布")
    public Result<List<PositionStatVO>> getPositionStats() {
        log.info("Get position statistics");
        List<PositionStatVO> data = statisticsService.getPositionStats();
        return Result.success(data);
    }

    @GetMapping("/membership/trend")
    @Operation(summary = "获取入会趋势", description = "获取成员入会和转正的时间趋势")
    public Result<List<MembershipTrendVO>> getMembershipTrend(
            @Parameter(description = "统计周期：month(按月) 或 year(按年)", example = "month")
            @RequestParam(defaultValue = "month") String period,
            @Parameter(description = "返回最近N个周期的数据", example = "12")
            @RequestParam(defaultValue = "12") Integer limit) {
        log.info("Get membership trend: period={}, limit={}", period, limit);
        List<MembershipTrendVO> data = statisticsService.getMembershipTrend(period, limit);
        return Result.success(data);
    }
}
