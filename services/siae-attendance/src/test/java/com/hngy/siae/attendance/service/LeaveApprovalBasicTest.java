package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.config.TestConfig;
import com.hngy.siae.attendance.dto.request.LeaveApprovalDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestCreateDTO;
import com.hngy.siae.attendance.dto.response.LeaveRequestVO;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.LeaveType;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import com.hngy.siae.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Basic Unit Tests for Leave Approval Functionality
 * 
 * @author SIAE Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class LeaveApprovalBasicTest {

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        leaveRequestMapper.selectList(null).forEach(leave -> 
            leaveRequestMapper.deleteById(leave.getId()));
    }

    /**
     * Test: Approving a pending leave request should update status to APPROVED
     * Validates: Requirements 6.1
     */
    @Test
    void approveLeaveRequest_shouldUpdateStatusToApproved() {
        // Given: A pending leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(100L);
        createDTO.setLeaveType(LeaveType.SICK_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(1));
        createDTO.setEndDate(LocalDate.now().plusDays(3));
        createDTO.setReason("Sick");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        assertThat(created.getStatus()).isEqualTo(LeaveStatus.PENDING);

        // When: The approver approves the leave request
        LeaveApprovalDTO approvalDTO = new LeaveApprovalDTO();
        approvalDTO.setApproved(true);
        approvalDTO.setReason("Approved");
        
        LeaveRequestVO approved = leaveService.approveLeaveRequest(created.getId(), approvalDTO, 1L);

        // Then: The status should be updated to APPROVED
        assertThat(approved.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(approved.getApproverId()).isEqualTo(1L);
        assertThat(approved.getApprovalNote()).isEqualTo("Approved");
        assertThat(approved.getApprovedAt()).isNotNull();

        // And: The database should reflect the changes
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(savedLeave.getApproverId()).isEqualTo(1L);
        assertThat(savedLeave.getApprovalNote()).isEqualTo("Approved");
        assertThat(savedLeave.getApprovedAt()).isNotNull();
    }

    /**
     * Test: Rejecting a pending leave request should update status to REJECTED
     * Validates: Requirements 6.2
     */
    @Test
    void rejectLeaveRequest_shouldUpdateStatusToRejected() {
        // Given: A pending leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(101L);
        createDTO.setLeaveType(LeaveType.PERSONAL_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(5));
        createDTO.setEndDate(LocalDate.now().plusDays(7));
        createDTO.setReason("Personal matters");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        assertThat(created.getStatus()).isEqualTo(LeaveStatus.PENDING);

        // When: The approver rejects the leave request
        LeaveApprovalDTO approvalDTO = new LeaveApprovalDTO();
        approvalDTO.setApproved(false);
        approvalDTO.setReason("Not enough notice");
        
        LeaveRequestVO rejected = leaveService.approveLeaveRequest(created.getId(), approvalDTO, 1L);

        // Then: The status should be updated to REJECTED
        assertThat(rejected.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(rejected.getApproverId()).isEqualTo(1L);
        assertThat(rejected.getApprovalNote()).isEqualTo("Not enough notice");
        assertThat(rejected.getApprovedAt()).isNotNull();

        // And: The database should reflect the changes
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(savedLeave.getApprovalNote()).isEqualTo("Not enough notice");
    }

    /**
     * Test: Attempting to approve a non-pending leave request should fail
     * Validates: Requirements 6.3
     */
    @Test
    void approveLeaveRequest_withNonPendingStatus_shouldFail() {
        // Given: An already approved leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(102L);
        createDTO.setLeaveType(LeaveType.SICK_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(10));
        createDTO.setEndDate(LocalDate.now().plusDays(12));
        createDTO.setReason("Medical appointment");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        
        // First approval
        LeaveApprovalDTO firstApproval = new LeaveApprovalDTO();
        firstApproval.setApproved(true);
        firstApproval.setReason("Approved");
        leaveService.approveLeaveRequest(created.getId(), firstApproval, 1L);

        // When: Attempting to approve again
        LeaveApprovalDTO secondApproval = new LeaveApprovalDTO();
        secondApproval.setApproved(true);
        secondApproval.setReason("Approved again");

        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.approveLeaveRequest(created.getId(), secondApproval, 1L))
            .isInstanceOf(ServiceException.class);
    }

    /**
     * Test: Attempting to approve with wrong approver should fail
     * Validates: Requirements 6.4
     */
    @Test
    void approveLeaveRequest_withWrongApprover_shouldFail() {
        // Given: A pending leave request exists with approver ID = 1
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(103L);
        createDTO.setLeaveType(LeaveType.PERSONAL_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(15));
        createDTO.setEndDate(LocalDate.now().plusDays(17));
        createDTO.setReason("Family event");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        assertThat(created.getApproverId()).isEqualTo(1L);

        // When: A different user (ID = 999) attempts to approve
        LeaveApprovalDTO approvalDTO = new LeaveApprovalDTO();
        approvalDTO.setApproved(true);
        approvalDTO.setReason("Approved");

        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.approveLeaveRequest(created.getId(), approvalDTO, 999L))
            .isInstanceOf(ServiceException.class);
    }

    /**
     * Test: Using rejectLeaveRequest method should work correctly
     * Validates: Requirements 6.2
     */
    @Test
    void rejectLeaveRequest_usingRejectMethod_shouldWork() {
        // Given: A pending leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(104L);
        createDTO.setLeaveType(LeaveType.SICK_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(20));
        createDTO.setEndDate(LocalDate.now().plusDays(22));
        createDTO.setReason("Medical treatment");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);

        // When: Using the rejectLeaveRequest method
        LeaveApprovalDTO rejectDTO = new LeaveApprovalDTO();
        rejectDTO.setReason("Insufficient documentation");
        
        LeaveRequestVO rejected = leaveService.rejectLeaveRequest(created.getId(), rejectDTO, 1L);

        // Then: The status should be REJECTED
        assertThat(rejected.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(rejected.getApprovalNote()).isEqualTo("Insufficient documentation");
    }
}
