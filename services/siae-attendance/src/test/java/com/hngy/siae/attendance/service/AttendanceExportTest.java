package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.AttendanceExportDTO;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.enums.RuleTargetType;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 考勤记录导出功能测试
 *
 * @author SIAE Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceExportTest {

    @Autowired
    private IAttendanceService attendanceService;

    @Autowired
    private AttendanceRuleMapper attendanceRuleMapper;

    private Long testUserId1 = 1001L;
    private Long testUserId2 = 1002L;

    @BeforeEach
    void setUp() {
        // 创建测试用的考勤规则
        AttendanceRule rule = new AttendanceRule();
        rule.setName("测试考勤规则");
        rule.setDescription("用于测试的考勤规则");
        rule.setAttendanceType(AttendanceType.DAILY);
        rule.setTargetType(RuleTargetType.ALL);
        rule.setCheckInStartTime(LocalTime.of(8, 0));
        rule.setCheckInEndTime(LocalTime.of(9, 30));
        rule.setCheckOutStartTime(LocalTime.of(17, 0));
        rule.setCheckOutEndTime(LocalTime.of(20, 0));
        rule.setLateThresholdMinutes(0);
        rule.setEarlyThresholdMinutes(0);
        rule.setLocationRequired(false);
        rule.setEffectiveDate(LocalDate.now().minusDays(10));
        rule.setStatus(RuleStatus.ENABLED);
        rule.setPriority(1);
        rule.setCreatedBy(1L);
        attendanceRuleMapper.insert(rule);

        // 创建一些测试考勤记录
        createTestAttendanceRecords();
    }

    private void createTestAttendanceRecords() {
        LocalDate today = LocalDate.now();

        // 用户1的考勤记录
        CheckInDTO checkIn1 = new CheckInDTO();
        checkIn1.setUserId(testUserId1);
        checkIn1.setTimestamp(LocalDateTime.of(today, LocalTime.of(8, 30)));
        checkIn1.setLocation("120.0,30.0");
        attendanceService.checkIn(checkIn1);

        CheckOutDTO checkOut1 = new CheckOutDTO();
        checkOut1.setUserId(testUserId1);
        checkOut1.setTimestamp(LocalDateTime.of(today, LocalTime.of(18, 0)));
        attendanceService.checkOut(checkOut1);

        // 用户2的考勤记录
        CheckInDTO checkIn2 = new CheckInDTO();
        checkIn2.setUserId(testUserId2);
        checkIn2.setTimestamp(LocalDateTime.of(today, LocalTime.of(9, 0)));
        checkIn2.setLocation("120.0,30.0");
        attendanceService.checkIn(checkIn2);

        CheckOutDTO checkOut2 = new CheckOutDTO();
        checkOut2.setUserId(testUserId2);
        checkOut2.setTimestamp(LocalDateTime.of(today, LocalTime.of(17, 30)));
        attendanceService.checkOut(checkOut2);
    }

    @Test
    void testExportToCsv() {
        // 准备导出参数
        AttendanceExportDTO exportDTO = new AttendanceExportDTO();
        exportDTO.setStartDate(LocalDate.now().minusDays(1));
        exportDTO.setEndDate(LocalDate.now().plusDays(1));
        exportDTO.setFormat("csv");

        // 执行导出
        byte[] result = attendanceService.exportRecords(exportDTO);

        // 验证结果
        assertNotNull(result, "导出结果不应为空");
        assertTrue(result.length > 0, "导出文件应有内容");

        // 验证CSV内容包含表头
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("考勤记录ID"), "CSV应包含表头");
        assertTrue(csvContent.contains("用户ID"), "CSV应包含用户ID列");
        assertTrue(csvContent.contains("考勤日期"), "CSV应包含考勤日期列");
    }

    @Test
    void testExportToExcel() {
        // 准备导出参数
        AttendanceExportDTO exportDTO = new AttendanceExportDTO();
        exportDTO.setStartDate(LocalDate.now().minusDays(1));
        exportDTO.setEndDate(LocalDate.now().plusDays(1));
        exportDTO.setFormat("excel");

        // 执行导出
        byte[] result = attendanceService.exportRecords(exportDTO);

        // 验证结果
        assertNotNull(result, "导出结果不应为空");
        assertTrue(result.length > 0, "导出文件应有内容");

        // 验证是否是有效的Excel文件（检查文件头）
        // Excel文件以PK开头（ZIP格式）
        assertTrue(result[0] == 0x50 && result[1] == 0x4B, "应该是有效的Excel文件格式");
    }

    @Test
    void testExportWithMemberFilter() {
        // 准备导出参数 - 只导出用户1的记录
        AttendanceExportDTO exportDTO = new AttendanceExportDTO();
        exportDTO.setStartDate(LocalDate.now().minusDays(1));
        exportDTO.setEndDate(LocalDate.now().plusDays(1));
        exportDTO.setMemberIds(Arrays.asList(testUserId1));
        exportDTO.setFormat("csv");

        // 执行导出
        byte[] result = attendanceService.exportRecords(exportDTO);

        // 验证结果
        assertNotNull(result, "导出结果不应为空");
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        // 应该包含用户1的记录
        assertTrue(csvContent.contains(testUserId1.toString()), "应包含用户1的记录");

        // 不应该包含用户2的记录
        assertFalse(csvContent.contains(testUserId2.toString()), "不应包含用户2的记录");
    }

    @Test
    void testExportWithDateRange() {
        // 准备导出参数 - 使用未来日期范围（应该没有记录）
        AttendanceExportDTO exportDTO = new AttendanceExportDTO();
        exportDTO.setStartDate(LocalDate.now().plusDays(10));
        exportDTO.setEndDate(LocalDate.now().plusDays(20));
        exportDTO.setFormat("csv");

        // 执行导出
        byte[] result = attendanceService.exportRecords(exportDTO);

        // 验证结果
        assertNotNull(result, "导出结果不应为空");
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        // 应该只有表头，没有数据行
        String[] lines = csvContent.split("\n");
        // 考虑BOM和空行，实际数据行应该很少
        assertTrue(lines.length <= 2, "未来日期范围应该没有考勤记录");
    }

    @Test
    void testExportDefaultFormat() {
        // 准备导出参数 - 不指定格式（应默认为CSV）
        AttendanceExportDTO exportDTO = new AttendanceExportDTO();
        exportDTO.setStartDate(LocalDate.now().minusDays(1));
        exportDTO.setEndDate(LocalDate.now().plusDays(1));

        // 执行导出
        byte[] result = attendanceService.exportRecords(exportDTO);

        // 验证结果
        assertNotNull(result, "导出结果不应为空");
        assertTrue(result.length > 0, "导出文件应有内容");

        // 验证是CSV格式
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("考勤记录ID"), "默认应导出为CSV格式");
    }
}
