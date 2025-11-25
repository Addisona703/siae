package com.hngy.siae.user.mapper;

import com.hngy.siae.user.dto.response.statistics.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 统计总用户数
     */
    Long countTotalUsers();

    /**
     * 统计启用用户数
     */
    Long countEnabledUsers();

    /**
     * 统计禁用用户数
     */
    Long countDisabledUsers();

    /**
     * 统计正式成员数
     */
    Long countFormalMembers();

    /**
     * 统计候选成员数
     */
    Long countCandidateMembers();

    /**
     * 统计本月新增用户数
     */
    Long countNewUsersThisMonth();

    /**
     * 统计本年新增用户数
     */
    Long countNewUsersThisYear();

    /**
     * 获取部门统计
     */
    List<DepartmentStatVO> getDepartmentStats();

    /**
     * 统计总获奖数
     */
    Long countTotalAwards();

    /**
     * 统计本年获奖数
     */
    Long countThisYearAwards();

    /**
     * 统计本月获奖数
     */
    Long countThisMonthAwards();

    /**
     * 统计获奖总人数
     */
    Long countTotalAwardedUsers();

    /**
     * 统计团队获奖数（成员数>1）
     */
    Long countTeamAwards();

    /**
     * 统计个人获奖数
     */
    Long countIndividualAwards();

    /**
     * 按奖项等级统计
     */
    List<AwardDistributionVO> getAwardsByLevel();

    /**
     * 按奖项类型统计
     */
    List<AwardDistributionVO> getAwardsByType();

    /**
     * 获取获奖趋势（按月）
     */
    List<TrendVO> getAwardTrendByMonth(@Param("limit") Integer limit);

    /**
     * 获取获奖趋势（按年）
     */
    List<TrendVO> getAwardTrendByYear(@Param("limit") Integer limit);

    /**
     * 获取获奖排行榜
     */
    List<AwardRankVO> getAwardRanking(@Param("limit") Integer limit);

    /**
     * 获取性别统计
     */
    List<GenderStatVO> getGenderStats();

    /**
     * 获取年级统计
     */
    List<GradeStatVO> getGradeStats();

    /**
     * 获取专业统计
     */
    List<MajorStatVO> getMajorStats();

    /**
     * 获取职位统计
     */
    List<PositionStatVO> getPositionStats();

    /**
     * 获取入会趋势（按月）
     */
    List<MembershipTrendVO> getMembershipTrendByMonth(@Param("limit") Integer limit);

    /**
     * 获取入会趋势（按年）
     */
    List<MembershipTrendVO> getMembershipTrendByYear(@Param("limit") Integer limit);

    /**
     * 获取获奖趋势明细数据（按月）
     */
    List<AwardLevelTrendItemVO> getAwardLevelTrendByMonth(
            @Param("startPeriod") String startPeriod,
            @Param("endPeriod") String endPeriod,
            @Param("limit") Integer limit);

    /**
     * 获取获奖趋势明细数据（按年）
     */
    List<AwardLevelTrendItemVO> getAwardLevelTrendByYear(
            @Param("startPeriod") String startPeriod,
            @Param("endPeriod") String endPeriod,
            @Param("limit") Integer limit);
}
