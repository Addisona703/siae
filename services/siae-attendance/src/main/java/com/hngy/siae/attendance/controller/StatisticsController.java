package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.ReportGenerateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import com.hngy.siae.attendance.dto.response.DepartmentStatisticsVO;
import com.hngy.siae.attendance.dto.response.ReportVO;
import com.hngy.siae.attendance.service.IStatisticsService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.hngy.siae.core.permissions.AttendancePermissions.Statistics;

/**
 * 考勤统计控制器
 *
 * @author SIAE Team
 */
@Tag(name = "考勤统计")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    /**
     * 查询个人考勤统计
     * 权限要求：拥有查看权限或查询本人数据
     */
    @Operation(summary = "查询个人考勤统计")
    @GetMapping("/personal/{userId}")
    @SiaeAuthorize("hasPermission('" + Statistics.VIEW + "') or isOwner(#userId)")
    public Result<AttendanceStatisticsVO> getPersonalStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceStatisticsVO result = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 查询部门考勤统计
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "查询部门考勤统计")
    @GetMapping("/department/{departmentId}")
    @SiaeAuthorize("hasPermission('" + Statistics.VIEW + "')")
    public Result<DepartmentStatisticsVO> getDepartmentStatistics(
            @PathVariable Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DepartmentStatisticsVO result = statisticsService.calculateDepartmentStatistics(departmentId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 查询个人当月统计
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人当月统计")
    @GetMapping("/my-statistics")
    @SiaeAuthorize("isAuthenticated()")
    public Result<AttendanceStatisticsVO> getMyStatistics() {
        AttendanceStatisticsVO result = statisticsService.getMyCurrentMonthStatistics();
        return Result.success(result);
    }

    /**
     * 查询个人出勤率
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人出勤率")
    @GetMapping("/my-attendance-rate")
    @SiaeAuthorize("isAuthenticated()")
    public Result<BigDecimal> getMyAttendanceRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal result = statisticsService.getMyAttendanceRate(startDate, endDate);
        return Result.success(result);
    }

    /**
     * 生成报表
     * 权限要求：拥有报表生成权限
     */
    @Operation(summary = "生成报表")
    @PostMapping("/report")
    @SiaeAuthorize("hasPermission('" + Statistics.REPORT_GENERATE + "')")
    @OperationLog(type = "GENERATE_REPORT", module = "STATISTICS", description = "生成报表")
    public Result<ReportVO> generateReport(@Valid @RequestBody ReportGenerateDTO dto) {
        ReportVO result = statisticsService.generateReport(dto);
        return Result.success(result);
    }

    /**
     * 导出报表
     * 权限要求：拥有导出权限
     */
    @Operation(summary = "导出报表")
    @GetMapping("/report/export")
    @SiaeAuthorize("hasPermission('" + Statistics.EXPORT + "')")
    @OperationLog(type = "EXPORT_REPORT", module = "STATISTICS", description = "导出报表")
    public void exportReport(
            @RequestParam String reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) {
        statisticsService.exportReport(reportType, startDate, endDate, format, response);
    }
}
