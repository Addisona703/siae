package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import com.hngy.siae.attendance.service.ILeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 请假与考勤同步基础测试
 * 
 * 测试 Requirements: 7.1, 7.2, 7.4, 7.5
 *
 * @author SIAE Team
 */
@SpringBootTest
@Transactional
@DisplayName("请假与考勤同步基础测试")
class LeaveSyncBasicTest {

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceAnomalyMapper attendanceAnomalyMapper;

    private Long testUserId;
    private LocalDate testStartDate;
    private LocalDate testEndDate;

    @BeforeEach
    void setUp() {
        testUserId = 100L;
        testStartDate = LocalDate.now().plusDays(1);
        testEndDate = LocalDate.now().plusDays(3);
    }

    @Test
    @DisplayName("测试批准的请假标记考勤为准假")
    void testApprovedLeaveMarksExcusedAbsence() {
        // 1. 创建已批准的请假申请
        LeaveRequest leaveRequest = createApprovedLeaveRequest();

        // 2. 创建请假期间的考勤记录
        AttendanceRecord record1 = createAttendanceRecord(testUserId, testStartDate);
        AttendanceRecord record2 = createAttendanceRecord(testUserId, testStartDate.plusDays(1));

        // 3. 执行同步
        leaveService.syncLeaveWithAttendance(leaveRequest.getId());

        // 4. 验证考勤记录被标记为准假 (Requirement 7.1)
        AttendanceRecord updatedRecord1 = attendanceRecordMapper.selectById(record1.getId());
        AttendanceRecord updatedRecord2 = attendanceRecordMapper.selectById(record2.getId());

        assertNotNull(updatedRecord1.getRemark());
        assertTrue(updatedRecord1.getRemark().contains("准假"));
        assertTrue(updatedRecord1.getRemark().contains(leaveRequest.getId().toString()));

        assertNotNull(updatedRecord2.getRemark());
        assertTrue(updatedRecord2.getRemark().contains("准假"));
        assertTrue(updatedRecord2.getRemark().contains(leaveRequest.getId().toString()));
    }

    @Test
    @DisplayName("测试请假抑制缺勤异常")
    void testLeaveSuppressesAbsenceAnomaly() {
        // 1. 创建已批准的请假申请
        LeaveRequest leaveRequest = createApprovedLeaveRequest();

        // 2. 创建请假期间的缺勤异常
        AttendanceAnomaly anomaly1 = createAbsenceAnomaly(testUserId, testStartDate);
        AttendanceAnomaly anomaly2 = createAbsenceAnomaly(testUserId, testStartDate.plusDays(1));

        // 3. 执行同步
        leaveService.syncLeaveWithAttendance(leaveRequest.getId());

        // 4. 验证缺勤异常被抑制 (Requirement 7.2)
        AttendanceAnomaly updatedAnomaly1 = attendanceAnomalyMapper.selectById(anomaly1.getId());
        AttendanceAnomaly updatedAnomaly2 = attendanceAnomalyMapper.selectById(anomaly2.getId());

        assertEquals(leaveRequest.getId(), updatedAnomaly1.getSuppressedByLeave());
        assertTrue(updatedAnomaly1.getResolved());
        assertNotNull(updatedAnomaly1.getHandlerNote());

        assertEquals(leaveRequest.getId(), updatedAnomaly2.getSuppressedByLeave());
        assertTrue(updatedAnomaly2.getResolved());
        assertNotNull(updatedAnomaly2.getHandlerNote());
    }

    @Test
    @DisplayName("测试未批准的请假不同步考勤")
    void testPendingLeaveDoesNotSync() {
        // 1. 创建待审核的请假申请
        LeaveRequest leaveRequest = createPendingLeaveRequest();

        // 2. 创建请假期间的考勤记录
        AttendanceRecord record = createAttendanceRecord(testUserId, testStartDate);

        // 3. 执行同步
        leaveService.syncLeaveWithAttendance(leaveRequest.getId());

        // 4. 验证考勤记录未被修改
        AttendanceRecord updatedRecord = attendanceRecordMapper.selectById(record.getId());
        assertNull(updatedRecord.getRemark());
    }

    @Test
    @DisplayName("测试只抑制缺勤类型的异常")
    void testOnlyAbsenceAnomaliesAreSuppressed() {
        // 1. 创建已批准的请假申请
        LeaveRequest leaveRequest = createApprovedLeaveRequest();

        // 2. 创建不同类型的异常
        AttendanceAnomaly absenceAnomaly = createAbsenceAnomaly(testUserId, testStartDate);
        AttendanceAnomaly lateAnomaly = createLateAnomaly(testUserId, testStartDate);

        // 3. 执行同步
        leaveService.syncLeaveWithAttendance(leaveRequest.getId());

        // 4. 验证只有缺勤异常被抑制
        AttendanceAnomaly updatedAbsence = attendanceAnomalyMapper.selectById(absenceAnomaly.getId());
        AttendanceAnomaly updatedLate = attendanceAnomalyMapper.selectById(lateAnomaly.getId());

        assertEquals(leaveRequest.getId(), updatedAbsence.getSuppressedByLeave());
        assertTrue(updatedAbsence.getResolved());

        assertNull(updatedLate.getSuppressedByLeave());
        assertFalse(updatedLate.getResolved());
    }

    @Test
    @DisplayName("测试引用完整性 - 请假不存在时抛出异常")
    void testReferentialIntegrity() {
        // 验证 Requirement 7.5: 维护引用完整性
        Long nonExistentLeaveId = 99999L;

        assertThrows(Exception.class, () -> {
            leaveService.syncLeaveWithAttendance(nonExistentLeaveId);
        });
    }

    // ========== 辅助方法 ==========

    private LeaveRequest createApprovedLeaveRequest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUserId(testUserId);
        leaveRequest.setLeaveType(LeaveType.PERSONAL_LEAVE);
        leaveRequest.setStartDate(testStartDate);
        leaveRequest.setEndDate(testEndDate);
        leaveRequest.setDays(BigDecimal.valueOf(3));
        leaveRequest.setReason("测试请假");
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApproverId(1L);
        leaveRequest.setApprovedAt(LocalDateTime.now());
        leaveRequestMapper.insert(leaveRequest);
        return leaveRequest;
    }

    private LeaveRequest createPendingLeaveRequest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUserId(testUserId);
        leaveRequest.setLeaveType(LeaveType.PERSONAL_LEAVE);
        leaveRequest.setStartDate(testStartDate);
        leaveRequest.setEndDate(testEndDate);
        leaveRequest.setDays(BigDecimal.valueOf(3));
        leaveRequest.setReason("测试请假");
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setApproverId(1L);
        leaveRequestMapper.insert(leaveRequest);
        return leaveRequest;
    }

    private AttendanceRecord createAttendanceRecord(Long userId, LocalDate date) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setAttendanceType(AttendanceType.DAILY);
        record.setCheckInTime(date.atTime(9, 0));
        record.setAttendanceDate(date);
        record.setStatus(AttendanceStatus.IN_PROGRESS);
        attendanceRecordMapper.insert(record);
        return record;
    }

    private AttendanceAnomaly createAbsenceAnomaly(Long userId, LocalDate date) {
        AttendanceAnomaly anomaly = new AttendanceAnomaly();
        anomaly.setUserId(userId);
        anomaly.setAnomalyType(AnomalyType.ABSENCE);
        anomaly.setAnomalyDate(date);
        anomaly.setDescription("缺勤");
        anomaly.setResolved(false);
        attendanceAnomalyMapper.insert(anomaly);
        return anomaly;
    }

    private AttendanceAnomaly createLateAnomaly(Long userId, LocalDate date) {
        AttendanceAnomaly anomaly = new AttendanceAnomaly();
        anomaly.setUserId(userId);
        anomaly.setAnomalyType(AnomalyType.LATE);
        anomaly.setAnomalyDate(date);
        anomaly.setDurationMinutes(30);
        anomaly.setDescription("迟到30分钟");
        anomaly.setResolved(false);
        attendanceAnomalyMapper.insert(anomaly);
        return anomaly;
    }
}
