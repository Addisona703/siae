package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.config.TestConfig;
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
 * Basic Unit Tests for Leave Cancellation Functionality
 * 
 * @author SIAE Team
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class LeaveCancellationBasicTest {

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
     * Test: Member can cancel their own pending leave request
     * Validates: Requirements 5.1
     */
    @Test
    void cancelLeaveRequest_withPendingStatus_shouldSucceed() {
        // Given: A pending leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(200L);
        createDTO.setLeaveType(LeaveType.SICK_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(1));
        createDTO.setEndDate(LocalDate.now().plusDays(3));
        createDTO.setReason("Sick");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        assertThat(created.getStatus()).isEqualTo(LeaveStatus.PENDING);

        // When: The member cancels their leave request
        Boolean result = leaveService.cancelLeaveRequest(created.getId(), 200L);

        // Then: The cancellation should succeed
        assertThat(result).isTrue();

        // And: The database should reflect the CANCELLED status
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.CANCELLED);
    }

    /**
     * Test: Member cannot cancel another member's leave request
     * Validates: Requirements 5.1 (permission check)
     */
    @Test
    void cancelLeaveRequest_byDifferentUser_shouldFail() {
        // Given: A pending leave request exists for user 201
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(201L);
        createDTO.setLeaveType(LeaveType.PERSONAL_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(5));
        createDTO.setEndDate(LocalDate.now().plusDays(7));
        createDTO.setReason("Personal matters");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);

        // When: A different user (202) attempts to cancel
        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest(created.getId(), 202L))
            .isInstanceOf(ServiceException.class);

        // And: The status should remain PENDING
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.PENDING);
    }

    /**
     * Test: Cannot cancel an approved leave request
     * Validates: Requirements 5.1 (status validation)
     */
    @Test
    void cancelLeaveRequest_withApprovedStatus_shouldFail() {
        // Given: An approved leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(203L);
        createDTO.setLeaveType(LeaveType.SICK_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(10));
        createDTO.setEndDate(LocalDate.now().plusDays(12));
        createDTO.setReason("Medical appointment");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        
        // Approve the leave request
        com.hngy.siae.attendance.dto.request.LeaveApprovalDTO approvalDTO = 
            new com.hngy.siae.attendance.dto.request.LeaveApprovalDTO();
        approvalDTO.setApproved(true);
        approvalDTO.setReason("Approved");
        leaveService.approveLeaveRequest(created.getId(), approvalDTO, 1L);

        // When: Attempting to cancel an approved leave
        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest(created.getId(), 203L))
            .isInstanceOf(ServiceException.class);

        // And: The status should remain APPROVED
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.APPROVED);
    }

    /**
     * Test: Cannot cancel a rejected leave request
     * Validates: Requirements 5.1 (status validation)
     */
    @Test
    void cancelLeaveRequest_withRejectedStatus_shouldFail() {
        // Given: A rejected leave request exists
        LeaveRequestCreateDTO createDTO = new LeaveRequestCreateDTO();
        createDTO.setUserId(204L);
        createDTO.setLeaveType(LeaveType.PERSONAL_LEAVE);
        createDTO.setStartDate(LocalDate.now().plusDays(15));
        createDTO.setEndDate(LocalDate.now().plusDays(17));
        createDTO.setReason("Family event");
        
        LeaveRequestVO created = leaveService.createLeaveRequest(createDTO);
        
        // Reject the leave request
        com.hngy.siae.attendance.dto.request.LeaveApprovalDTO approvalDTO = 
            new com.hngy.siae.attendance.dto.request.LeaveApprovalDTO();
        approvalDTO.setApproved(false);
        approvalDTO.setReason("Not enough notice");
        leaveService.approveLeaveRequest(created.getId(), approvalDTO, 1L);

        // When: Attempting to cancel a rejected leave
        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest(created.getId(), 204L))
            .isInstanceOf(ServiceException.class);

        // And: The status should remain REJECTED
        LeaveRequest savedLeave = leaveRequestMapper.selectById(created.getId());
        assertThat(savedLeave.getStatus()).isEqualTo(LeaveStatus.REJECTED);
    }

    /**
     * Test: Cannot cancel a non-existent leave request
     * Validates: Requirements 5.1 (error handling)
     */
    @Test
    void cancelLeaveRequest_withNonExistentId_shouldFail() {
        // When: Attempting to cancel a non-existent leave request
        // Then: Should throw ServiceException
        assertThatThrownBy(() -> 
            leaveService.cancelLeaveRequest(99999L, 205L))
            .isInstanceOf(ServiceException.class);
    }
}
