package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.config.TestConfig;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import com.hngy.siae.attendance.dto.response.DepartmentStatisticsVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceStatistics;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceStatisticsMapper;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 统计计算基础测试
 *
 * @author SIAE Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class StatisticsCalculationBasicTest {

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceAnomalyMapper attendanceAnomalyMapper;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private AttendanceStatisticsMapper attendanceStatisticsMapper;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        attendanceRecordMapper.selectList(null).forEach(record ->
                attendanceRecordMapper.deleteById(record.getId()));
        attendanceAnomalyMapper.selectList(null).forEach(anomaly ->
                attendanceAnomalyMapper.deleteById(anomaly.getId()));
        leaveRequestMapper.selectList(null).forEach(leave ->
                leaveRequestMapper.deleteById(leave.getId()));
        attendanceStatisticsMapper.selectList(null).forEach(stats ->
                attendanceStatisticsMapper.deleteById(stats.getId()));
    }

    @Test
    void testCalculatePersonalStatistics_WithCompleteData() {
        // Given: 用户有完整的考勤数据
        Long userId = 1001L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 5);

        // 创建5天的考勤记录
        for (int i = 0; i < 5; i++) {
            LocalDate date = startDate.plusDays(i);
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(userId);
            record.setCheckInTime(date.atTime(8, 30));
            record.setCheckOutTime(date.atTime(17, 30));
            record.setAttendanceDate(date);
            record.setDurationMinutes(540); // 9小时
            record.setStatus(AttendanceStatus.COMPLETED);
            record.setDeleted(0);
            attendanceRecordMapper.insert(record);
        }

        // 创建2次迟到异常
        for (int i = 0; i < 2; i++) {
            AttendanceAnomaly anomaly = new AttendanceAnomaly();
            anomaly.setUserId(userId);
            anomaly.setAnomalyType(AnomalyType.LATE);
            anomaly.setAnomalyDate(startDate.plusDays(i));
            anomaly.setDurationMinutes(30);
            anomaly.setResolved(false);
            anomaly.setDeleted(0);
            attendanceAnomalyMapper.insert(anomaly);
        }

        // When: 计算个人统计
        AttendanceStatisticsVO result = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);

        // Then: 统计数据应该正确
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalDays()).isEqualTo(5);
        assertThat(result.getActualDays()).isEqualTo(5);
        assertThat(result.getLateCount()).isEqualTo(2);
        assertThat(result.getEarlyCount()).isEqualTo(0);
        assertThat(result.getAbsenceCount()).isEqualTo(0);
        assertThat(result.getTotalDurationMinutes()).isEqualTo(2700); // 5天 * 540分钟
        assertThat(result.getAttendanceRate()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void testCalculatePersonalStatistics_WithLeave() {
        // Given: 用户有请假记录
        Long userId = 1002L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 5);

        // 创建3天的考勤记录
        for (int i = 0; i < 3; i++) {
            LocalDate date = startDate.plusDays(i);
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(userId);
            record.setCheckInTime(date.atTime(8, 30));
            record.setCheckOutTime(date.atTime(17, 30));
            record.setAttendanceDate(date);
            record.setDurationMinutes(540);
            record.setStatus(AttendanceStatus.COMPLETED);
            record.setDeleted(0);
            attendanceRecordMapper.insert(record);
        }

        // 创建2天的请假
        LeaveRequest leave = new LeaveRequest();
        leave.setUserId(userId);
        leave.setLeaveType(LeaveType.SICK_LEAVE);
        leave.setStartDate(startDate.plusDays(3));
        leave.setEndDate(startDate.plusDays(4));
        leave.setDays(BigDecimal.valueOf(2));
        leave.setReason("生病");
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setDeleted(0);
        leaveRequestMapper.insert(leave);

        // When: 计算个人统计
        AttendanceStatisticsVO result = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);

        // Then: 统计数据应该正确
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTotalDays()).isEqualTo(5);
        assertThat(result.getActualDays()).isEqualTo(3);
        assertThat(result.getLeaveDays()).isEqualByComparingTo(BigDecimal.valueOf(2));
        // 出勤率 = 3 / (5 - 2) * 100 = 100%
        assertThat(result.getAttendanceRate()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void testCalculateAttendanceRate_BasicCalculation() {
        // Given: 用户有部分出勤记录
        Long userId = 1003L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);

        // 创建8天的考勤记录（10天中出勤8天）
        for (int i = 0; i < 8; i++) {
            LocalDate date = startDate.plusDays(i);
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(userId);
            record.setCheckInTime(date.atTime(8, 30));
            record.setCheckOutTime(date.atTime(17, 30));
            record.setAttendanceDate(date);
            record.setStatus(AttendanceStatus.COMPLETED);
            record.setDeleted(0);
            attendanceRecordMapper.insert(record);
        }

        // When: 计算出勤率
        BigDecimal rate = statisticsService.calculateAttendanceRate(userId, startDate, endDate);

        // Then: 出勤率应该是 80%
        assertThat(rate).isEqualByComparingTo(BigDecimal.valueOf(80.00));
    }

    @Test
    void testCalculateAttendanceRate_WithLeave() {
        // Given: 用户有请假记录
        Long userId = 1004L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);

        // 创建7天的考勤记录
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(userId);
            record.setCheckInTime(date.atTime(8, 30));
            record.setCheckOutTime(date.atTime(17, 30));
            record.setAttendanceDate(date);
            record.setStatus(AttendanceStatus.COMPLETED);
            record.setDeleted(0);
            attendanceRecordMapper.insert(record);
        }

        // 创建3天的请假
        LeaveRequest leave = new LeaveRequest();
        leave.setUserId(userId);
        leave.setLeaveType(LeaveType.PERSONAL_LEAVE);
        leave.setStartDate(startDate.plusDays(7));
        leave.setEndDate(startDate.plusDays(9));
        leave.setDays(BigDecimal.valueOf(3));
        leave.setReason("事假");
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setDeleted(0);
        leaveRequestMapper.insert(leave);

        // When: 计算出勤率
        BigDecimal rate = statisticsService.calculateAttendanceRate(userId, startDate, endDate);

        // Then: 出勤率 = 7 / (10 - 3) * 100 = 100%
        assertThat(rate).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void testGenerateMonthlyStatistics() {
        // Given: 用户有考勤数据
        Long userId = 1005L;
        String month = "2024-11";
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // 创建考勤记录
        for (int i = 0; i < 20; i++) {
            LocalDate date = startDate.plusDays(i);
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(userId);
            record.setCheckInTime(date.atTime(8, 30));
            record.setCheckOutTime(date.atTime(17, 30));
            record.setAttendanceDate(date);
            record.setDurationMinutes(540);
            record.setStatus(AttendanceStatus.COMPLETED);
            record.setDeleted(0);
            attendanceRecordMapper.insert(record);
        }

        // When: 生成月度统计
        statisticsService.generateMonthlyStatistics(month);

        // Then: 应该创建统计记录
        AttendanceStatistics stats = attendanceStatisticsMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceStatistics>()
                        .eq(AttendanceStatistics::getUserId, userId)
                        .eq(AttendanceStatistics::getStatMonth, month)
        );

        assertThat(stats).isNotNull();
        assertThat(stats.getUserId()).isEqualTo(userId);
        assertThat(stats.getStatMonth()).isEqualTo(month);
        assertThat(stats.getActualDays()).isEqualTo(20);
        assertThat(stats.getTotalDurationMinutes()).isEqualTo(10800); // 20天 * 540分钟
    }

    @Test
    void testCalculateDepartmentStatistics_EmptyDepartment() {
        // Given: 空部门
        Long departmentId = 9999L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 5);

        // When: 计算部门统计
        DepartmentStatisticsVO result = statisticsService.calculateDepartmentStatistics(departmentId, startDate, endDate);

        // Then: 应该返回空统计
        assertThat(result).isNotNull();
        assertThat(result.getDepartmentId()).isEqualTo(departmentId);
        assertThat(result.getTotalMembers()).isEqualTo(0);
        assertThat(result.getAvgAttendanceRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMemberStatistics()).isEmpty();
    }
}
