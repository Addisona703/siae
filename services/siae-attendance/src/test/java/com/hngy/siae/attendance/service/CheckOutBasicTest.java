package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.config.TestConfig;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.*;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import com.hngy.siae.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Basic unit tests for check-out functionality
 *
 * @author SIAE Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class CheckOutBasicTest {

    @Autowired
    private IAttendanceService attendanceService;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceRuleMapper attendanceRuleMapper;

    @Autowired
    private AttendanceAnomalyMapper attendanceAnomalyMapper;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        attendanceRecordMapper.selectList(null).forEach(record ->
                attendanceRecordMapper.deleteById(record.getId()));
        attendanceRuleMapper.selectList(null).forEach(rule ->
                attendanceRuleMapper.deleteById(rule.getId()));
        attendanceAnomalyMapper.selectList(null).forEach(anomaly ->
                attendanceAnomalyMapper.deleteById(anomaly.getId()));
    }

    @Test
    void testNormalCheckOut() {
        // Given: A user has checked in
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDateTime checkInTime = today.atTime(8, 30);
        LocalDateTime checkOutTime = today.atTime(17, 30);

        AttendanceRule rule = createTestRule(today);
        attendanceRuleMapper.insert(rule);

        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setUserId(userId);
        checkInDTO.setTimestamp(checkInTime);
        checkInDTO.setLocation("test-location");
        checkInDTO.setAttendanceType(AttendanceType.DAILY.getCode());

        AttendanceRecordVO checkInResult = attendanceService.checkIn(checkInDTO);

        // When: User checks out
        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setUserId(userId);
        checkOutDTO.setTimestamp(checkOutTime);
        checkOutDTO.setLocation("test-location");

        AttendanceRecordVO result = attendanceService.checkOut(checkOutDTO);

        // Then: Check-out should be successful
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(checkInResult.getId());
        assertThat(result.getCheckOutTime()).isEqualTo(checkOutTime);
        assertThat(result.getDurationMinutes()).isEqualTo(540); // 9 hours = 540 minutes
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.COMPLETED);

        // And: Record should be updated in database
        AttendanceRecord savedRecord = attendanceRecordMapper.selectById(result.getId());
        assertThat(savedRecord.getCheckOutTime()).isEqualTo(checkOutTime);
        assertThat(savedRecord.getDurationMinutes()).isEqualTo(540);
        assertThat(savedRecord.getStatus()).isEqualTo(AttendanceStatus.COMPLETED);
    }

    @Test
    void testCheckOutWithoutCheckIn() {
        // Given: No check-in record exists
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDateTime checkOutTime = today.atTime(17, 30);

        AttendanceRule rule = createTestRule(today);
        attendanceRuleMapper.insert(rule);

        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setUserId(userId);
        checkOutDTO.setTimestamp(checkOutTime);
        checkOutDTO.setLocation("test-location");

        // When/Then: Check-out should fail
        assertThatThrownBy(() -> attendanceService.checkOut(checkOutDTO))
                .hasMessageContaining("未找到签到记录");
    }

    @Test
    void testDuplicateCheckOut() {
        // Given: A user has checked in and checked out
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDateTime checkInTime = today.atTime(8, 30);
        LocalDateTime checkOutTime = today.atTime(17, 30);

        AttendanceRule rule = createTestRule(today);
        attendanceRuleMapper.insert(rule);

        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setUserId(userId);
        checkInDTO.setTimestamp(checkInTime);
        checkInDTO.setLocation("test-location");
        checkInDTO.setAttendanceType(AttendanceType.DAILY.getCode());

        attendanceService.checkIn(checkInDTO);

        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setUserId(userId);
        checkOutDTO.setTimestamp(checkOutTime);
        checkOutDTO.setLocation("test-location");

        AttendanceRecordVO firstCheckOut = attendanceService.checkOut(checkOutDTO);
        assertThat(firstCheckOut.getCheckOutTime()).isEqualTo(checkOutTime);

        // When/Then: Second check-out should fail (no IN_PROGRESS record found)
        assertThatThrownBy(() -> attendanceService.checkOut(checkOutDTO))
                .hasMessageContaining("未找到签到记录");
    }

    @Test
    void testEarlyDepartureDetection() {
        // Given: A user has checked in
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDateTime checkInTime = today.atTime(8, 30);
        LocalDateTime earlyCheckOutTime = today.atTime(17, 30); // Within check-out window but 30 minutes early

        AttendanceRule rule = createTestRule(today);
        rule.setEarlyThresholdMinutes(15); // 15 minutes threshold
        rule.setCheckOutStartTime(LocalTime.of(18, 0)); // Required check-out time is 18:00
        rule.setCheckOutEndTime(LocalTime.of(19, 0)); // Check-out window: 18:00-19:00
        attendanceRuleMapper.insert(rule);

        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setUserId(userId);
        checkInDTO.setTimestamp(checkInTime);
        checkInDTO.setLocation("test-location");
        checkInDTO.setAttendanceType(AttendanceType.DAILY.getCode());

        attendanceService.checkIn(checkInDTO);

        // When: User checks out early
        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setUserId(userId);
        checkOutDTO.setTimestamp(earlyCheckOutTime);
        checkOutDTO.setLocation("test-location");

        AttendanceRecordVO result = attendanceService.checkOut(checkOutDTO);

        // Then: Status should be ABNORMAL
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.ABNORMAL);

        // And: An early departure anomaly should be created
        List<AttendanceAnomaly> anomalies = attendanceAnomalyMapper.selectList(null);
        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.get(0).getAnomalyType()).isEqualTo(AnomalyType.EARLY_DEPARTURE);
        assertThat(anomalies.get(0).getDurationMinutes()).isEqualTo(30);
        assertThat(anomalies.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void testDurationCalculation() {
        // Given: A user has checked in
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDateTime checkInTime = today.atTime(8, 0);
        LocalDateTime checkOutTime = today.atTime(17, 45);

        AttendanceRule rule = createTestRule(today);
        attendanceRuleMapper.insert(rule);

        CheckInDTO checkInDTO = new CheckInDTO();
        checkInDTO.setUserId(userId);
        checkInDTO.setTimestamp(checkInTime);
        checkInDTO.setLocation("test-location");
        checkInDTO.setAttendanceType(AttendanceType.DAILY.getCode());

        attendanceService.checkIn(checkInDTO);

        // When: User checks out
        CheckOutDTO checkOutDTO = new CheckOutDTO();
        checkOutDTO.setUserId(userId);
        checkOutDTO.setTimestamp(checkOutTime);
        checkOutDTO.setLocation("test-location");

        AttendanceRecordVO result = attendanceService.checkOut(checkOutDTO);

        // Then: Duration should be calculated correctly
        assertThat(result.getDurationMinutes()).isEqualTo(585); // 9 hours 45 minutes = 585 minutes
    }

    private AttendanceRule createTestRule(LocalDate date) {
        AttendanceRule rule = new AttendanceRule();
        rule.setName("Test Rule");
        rule.setDescription("Rule for testing");
        rule.setAttendanceType(AttendanceType.DAILY);
        rule.setRelatedId(null);
        rule.setTargetType(RuleTargetType.ALL);
        rule.setTargetIds(Collections.emptyList());

        // Set check-in window: 08:00 - 09:00
        rule.setCheckInStartTime(LocalTime.of(8, 0));
        rule.setCheckInEndTime(LocalTime.of(9, 0));

        // Set check-out window: 17:00 - 18:00
        rule.setCheckOutStartTime(LocalTime.of(17, 0));
        rule.setCheckOutEndTime(LocalTime.of(18, 0));

        rule.setLateThresholdMinutes(0);
        rule.setEarlyThresholdMinutes(0);
        rule.setLocationRequired(false);
        rule.setAllowedLocations(Collections.emptyList());
        rule.setLocationRadiusMeters(100);

        rule.setEffectiveDate(date.minusDays(1));
        rule.setExpiryDate(date.plusDays(1));

        rule.setStatus(RuleStatus.ENABLED);
        rule.setPriority(1);
        rule.setCreatedBy(1L);

        return rule;
    }
}
