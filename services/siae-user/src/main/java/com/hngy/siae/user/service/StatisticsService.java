package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.response.statistics.*;

import java.util.List;

/**
 * 统计服务接口
 *
 * @author SIAE Team
 */
public interface StatisticsService {

    /**
     * 获取成员概览统计
     */
    MemberOverviewVO getMemberOverview();

    /**
     * 获取部门分布统计
     */
    List<DepartmentStatVO> getDepartmentStats();

    /**
     * 获取获奖概览统计
     */
    AwardOverviewVO getAwardOverview();

    /**
     * 获取按奖项等级分布
     */
    List<AwardDistributionVO> getAwardsByLevel();

    /**
     * 获取按奖项类型分布
     */
    List<AwardDistributionVO> getAwardsByType();

    /**
     * 获取获奖趋势
     *
     * @param period 统计周期：month或year
     * @param limit  返回最近N个周期
     */
    List<TrendVO> getAwardTrend(String period, Integer limit);

    /**
     * 获取获奖趋势详细统计（含各等级分布）
     *
     * @param period      统计周期：month或year
     * @param startPeriod 开始时间（YYYY-MM或YYYY）
     * @param endPeriod   结束时间（YYYY-MM或YYYY）
     */
    List<AwardTrendDetailVO> getAwardTrendDetail(String period, String startPeriod, String endPeriod);

    /**
     * 获取获奖排行榜
     *
     * @param limit TOP N
     */
    List<AwardRankVO> getAwardRanking(Integer limit);

    /**
     * 获取性别分布统计
     */
    List<GenderStatVO> getGenderStats();

    /**
     * 获取年级分布统计
     */
    List<GradeStatVO> getGradeStats();

    /**
     * 获取专业分布统计
     */
    List<MajorStatVO> getMajorStats();

    /**
     * 获取职位分布统计
     */
    List<PositionStatVO> getPositionStats();

    /**
     * 获取入会趋势统计
     *
     * @param period 统计周期：month或year
     * @param limit  返回最近N个周期
     */
    List<MembershipTrendVO> getMembershipTrend(String period, Integer limit);
}
