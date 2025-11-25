package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.ReportGenerateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import com.hngy.siae.attendance.dto.response.DepartmentStatisticsVO;
import com.hngy.siae.attendance.dto.response.ReportVO;

import java.time.LocalDate;

/**
 * 考勤统计服务接口
 *
 * @author SIAE Team
 */
public interface IStatisticsService {

    /**
     * 计算个人考勤统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期（为null时默认当月第一天）
     * @param endDate 结束日期（为null时默认当月最后一天）
     * @return 考勤统计信息
     */
    AttendanceStatisticsVO calculatePersonalStatistics(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 计算部门考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期（为null时默认当月第一天）
     * @param endDate 结束日期（为null时默认当月最后一天）
     * @return 部门统计信息
     */
    DepartmentStatisticsVO calculateDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 计算出勤率
     *
     * @param userId 用户ID
     * @param startDate 开始日期（为null时默认当月第一天）
     * @param endDate 结束日期（为null时默认当月最后一天）
     * @return 出勤率(%)
     */
    java.math.BigDecimal calculateAttendanceRate(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 生成月度统计（定时任务调用）
     *
     * @param month 统计月份(YYYY-MM)
     */
    void generateMonthlyStatistics(String month);

    /**
     * 生成报表
     *
     * @param dto 报表生成请求
     * @return 报表信息
     */
    ReportVO generateReport(ReportGenerateDTO dto);

    /**
     * 计算活动考勤统计
     *
     * @param activityId 活动ID
     * @return 活动考勤统计信息
     */
    com.hngy.siae.attendance.dto.response.ActivityAttendanceStatisticsVO calculateActivityStatistics(Long activityId);

    /**
     * 查询当前用户当月考勤统计
     *
     * @return 考勤统计信息
     */
    AttendanceStatisticsVO getMyCurrentMonthStatistics();

    /**
     * 查询当前用户出勤率
     *
     * @param startDate 开始日期（为null时默认当月第一天）
     * @param endDate 结束日期（为null时默认当月最后一天）
     * @return 出勤率(%)
     */
    java.math.BigDecimal getMyAttendanceRate(LocalDate startDate, LocalDate endDate);

    /**
     * 导出考勤报表
     *
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param format 导出格式
     * @param response HTTP响应
     */
    void exportReport(String reportType, LocalDate startDate, LocalDate endDate, String format, 
                     jakarta.servlet.http.HttpServletResponse response);
}
