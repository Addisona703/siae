package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.ReportGenerateDTO;
import com.hngy.siae.attendance.dto.response.ReportVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 报表生成基础测试
 *
 * @author SIAE Team
 */
@SpringBootTest
@Transactional
class ReportGenerationBasicTest {

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceAnomalyMapper attendanceAnomalyMapper;

    private Long testUserId1 = 1001L;
    private Long testUserId2 = 1002L;
    private LocalDate testDate = LocalDate.of(2024, 11, 20);

    @BeforeEach
    void setUp() {
        // 清理测试数据
        attendanceRecordMapper.selectList(null).forEach(r -> attendanceRecordMapper.deleteById(r.getId()));
        attendanceAnomalyMapper.selectList(null).forEach(a -> attendanceAnomalyMapper.deleteById(a.getId()));

        // 创建测试考勤记录
        createTestAttendanceRecord(testUserId1, testDate, AttendanceStatus.COMPLETED, 480);
        createTestAttendanceRecord(testUserId2, testDate, AttendanceStatus.COMPLETED, 450);
        createTestAttendanceRecord(testUserId1, testDate.plusDays(1), AttendanceStatus.COMPLETED, 500);

        // 创建测试异常记录
        createTestAnomaly(testUserId1, testDate, AnomalyType.LATE, 15);
        createTestAnomaly(testUserId2, testDate, AnomalyType.EARLY_DEPARTURE, 20);
        createTestAnomaly(testUserId1, testDate.plusDays(2), AnomalyType.ABSENCE, null);
    }

    @Test
    void testGenerateMonthlyReportCSV() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("monthly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");
        dto.setUserIds(Arrays.asList(testUserId1, testUserId2));

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("monthly", result.getReportType());
        assertEquals("csv", result.getFormat());
        assertNotNull(result.getFileUrl());
        assertNotNull(result.getGeneratedAt());
        assertTrue(result.getReportName().contains("月度考勤报表"));
    }

    @Test
    void testGenerateMonthlyReportPDF() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("monthly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("pdf");
        dto.setUserIds(Arrays.asList(testUserId1, testUserId2));

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("monthly", result.getReportType());
        assertEquals("pdf", result.getFormat());
        assertNotNull(result.getFileUrl());
        assertNotNull(result.getGeneratedAt());
    }

    @Test
    void testGenerateAnomalyReportCSV() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("anomaly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");
        dto.setUserIds(Arrays.asList(testUserId1, testUserId2));

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("anomaly", result.getReportType());
        assertEquals("csv", result.getFormat());
        assertNotNull(result.getFileUrl());
        assertTrue(result.getReportName().contains("考勤异常报表"));
    }

    @Test
    void testGenerateAnomalyReportPDF() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("anomaly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("pdf");

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("anomaly", result.getReportType());
        assertEquals("pdf", result.getFormat());
        assertNotNull(result.getFileUrl());
    }

    @Test
    void testGenerateReportWithDefaultFormat() {
        // 准备测试数据（不指定格式，应该默认为csv）
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("monthly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setUserIds(Arrays.asList(testUserId1));

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertEquals("csv", result.getFormat()); // 默认应该是csv
    }

    @Test
    void testGenerateReportWithInvalidFormat() {
        // 准备测试数据（使用不支持的格式）
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("monthly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("xlsx"); // 不支持的格式
        dto.setUserIds(Arrays.asList(testUserId1));

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果 - 应该回退到csv
        assertNotNull(result);
        assertEquals("csv", result.getFormat());
    }

    @Test
    void testGenerateReportWithInvalidType() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("invalid_type");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");

        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.generateReport(dto);
        });
    }

    @Test
    void testGenerateReportWithNullType() {
        // 准备测试数据
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType(null);
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");

        // 执行测试 - 应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.generateReport(dto);
        });
    }

    @Test
    void testGenerateMonthlyReportWithoutUserIds() {
        // 准备测试数据（不指定用户ID，应该查询所有用户）
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("monthly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("monthly", result.getReportType());
    }

    @Test
    void testGenerateAnomalyReportWithoutUserIds() {
        // 准备测试数据（不指定用户ID，应该查询所有异常）
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType("anomaly");
        dto.setStartDate(testDate);
        dto.setEndDate(testDate.plusDays(7));
        dto.setFormat("csv");

        // 执行测试
        ReportVO result = statisticsService.generateReport(dto);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getReportId());
        assertEquals("anomaly", result.getReportType());
    }

    // 辅助方法：创建测试考勤记录
    private void createTestAttendanceRecord(Long userId, LocalDate date, AttendanceStatus status, Integer duration) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setAttendanceDate(date);
        record.setCheckInTime(date.atTime(9, 0));
        record.setCheckOutTime(date.atTime(18, 0));
        record.setStatus(status);
        record.setDurationMinutes(duration);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setDeleted(0);
        attendanceRecordMapper.insert(record);
    }

    // 辅助方法：创建测试异常记录
    private void createTestAnomaly(Long userId, LocalDate date, AnomalyType type, Integer duration) {
        AttendanceAnomaly anomaly = new AttendanceAnomaly();
        anomaly.setUserId(userId);
        anomaly.setAnomalyDate(date);
        anomaly.setAnomalyType(type);
        anomaly.setDurationMinutes(duration);
        anomaly.setDescription("测试异常");
        anomaly.setResolved(false);
        anomaly.setCreatedAt(LocalDateTime.now());
        anomaly.setUpdatedAt(LocalDateTime.now());
        anomaly.setDeleted(0);
        attendanceAnomalyMapper.insert(anomaly);
    }
}
