package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.enums.RuleTargetType;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import com.hngy.siae.attendance.config.TestConfig;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import net.jqwik.time.api.DateTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-Based Tests for Check-In Functionality
 * 
 * @author SIAE Team
 */
@JqwikSpringSupport
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class CheckInPropertyTest {

    @Autowired
    private IAttendanceService attendanceService;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceRuleMapper attendanceRuleMapper;

    /**
     * Feature: attendance-service, Property 1: Valid check-in creates record
     * Validates: Requirements 1.1
     * 
     * For any valid check-in request (with member ID, timestamp, and location),
     * the system should create a new attendance record containing all the provided information.
     */
    @Property(tries = 100)
    void validCheckInCreatesRecord(
            @ForAll("validCheckInRequests") CheckInDTO checkInDTO) {
        
        // Clean up before test - use selectList and delete individually to avoid MyBatis Plus block
        attendanceRecordMapper.selectList(null).forEach(record -> 
            attendanceRecordMapper.deleteById(record.getId()));
        attendanceRuleMapper.selectList(null).forEach(rule -> 
            attendanceRuleMapper.deleteById(rule.getId()));
        
        // Given: A valid attendance rule exists for the check-in time
        AttendanceRule rule = createValidRule(checkInDTO.getTimestamp());
        attendanceRuleMapper.insert(rule);

        // When: A member submits a valid check-in request
        AttendanceRecordVO result = attendanceService.checkIn(checkInDTO);

        // Then: A new attendance record should be created
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        
        // And: The record should contain all provided information
        assertThat(result.getUserId()).isEqualTo(checkInDTO.getUserId());
        assertThat(result.getCheckInTime()).isEqualTo(checkInDTO.getTimestamp());
        assertThat(result.getAttendanceDate()).isEqualTo(checkInDTO.getTimestamp().toLocalDate());
        // Status can be IN_PROGRESS or ABNORMAL depending on whether late arrival was detected
        assertThat(result.getStatus()).isIn(AttendanceStatus.IN_PROGRESS, AttendanceStatus.ABNORMAL);
        
        // And: The record should be persisted in the database
        AttendanceRecord savedRecord = attendanceRecordMapper.selectById(result.getId());
        assertThat(savedRecord).isNotNull();
        assertThat(savedRecord.getUserId()).isEqualTo(checkInDTO.getUserId());
        assertThat(savedRecord.getCheckInTime()).isEqualTo(checkInDTO.getTimestamp());
        assertThat(savedRecord.getCheckInLocation()).isEqualTo(checkInDTO.getLocation());
        assertThat(savedRecord.getAttendanceDate()).isEqualTo(checkInDTO.getTimestamp().toLocalDate());
        assertThat(savedRecord.getRuleId()).isEqualTo(rule.getId());
        // Status can be IN_PROGRESS or ABNORMAL depending on whether late arrival was detected
        assertThat(savedRecord.getStatus()).isIn(AttendanceStatus.IN_PROGRESS, AttendanceStatus.ABNORMAL);
    }

    /**
     * Provides valid check-in requests for property testing
     */
    @Provide
    Arbitrary<CheckInDTO> validCheckInRequests() {
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 10000L);
        
        // Generate timestamps within a reasonable range (today to 7 days from now)
        Arbitrary<LocalDateTime> timestamps = DateTimes.dateTimes()
                .between(
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(7)
                );
        
        // Generate valid locations (simple string format for testing)
        Arbitrary<String> locations = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofLength(20)
                .map(s -> {
                    // Format as "latitude,longitude" (simplified)
                    String lat = s.substring(0, 10);
                    String lon = s.substring(10, 20);
                    return lat + "," + lon;
                });
        
        return Combinators.combine(userIds, timestamps, locations)
                .as((userId, timestamp, location) -> {
                    CheckInDTO dto = new CheckInDTO();
                    dto.setUserId(userId);
                    // Adjust timestamp to be within check-in window (08:00-09:00)
                    LocalDateTime adjustedTime = timestamp.toLocalDate()
                            .atTime(8, 0)
                            .plusMinutes(timestamp.getMinute() % 60);
                    dto.setTimestamp(adjustedTime);
                    dto.setLocation(location);
                    dto.setAttendanceType(AttendanceType.DAILY.getCode());
                    dto.setRelatedId(null);
                    return dto;
                });
    }

    /**
     * Creates a valid attendance rule for the given timestamp
     */
    private AttendanceRule createValidRule(LocalDateTime timestamp) {
        AttendanceRule rule = new AttendanceRule();
        rule.setName("Test Rule");
        rule.setDescription("Rule for property testing");
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
        
        // Set effective date range to cover the test period
        rule.setEffectiveDate(LocalDate.now().minusDays(10));
        rule.setExpiryDate(LocalDate.now().plusDays(30));
        
        rule.setStatus(RuleStatus.ENABLED);
        rule.setPriority(1);
        rule.setCreatedBy(1L);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        rule.setDeleted(0);
        
        return rule;
    }
}
