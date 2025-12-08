package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.LeaveApprovalDTO;
import com.hngy.siae.attendance.dto.request.LeaveQueryDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestCreateDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestUpdateDTO;
import com.hngy.siae.attendance.dto.response.LeaveRequestDetailVO;
import com.hngy.siae.attendance.dto.response.LeaveRequestVO;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.time.LocalDateTime;

/**
 * 请假服务接口
 *
 * @author SIAE Team
 */
public interface ILeaveService {

    /**
     * 创建请假申请
     *
     * @param dto 请假申请创建DTO
     * @return 请假申请VO
     */
    LeaveRequestVO createLeaveRequest(LeaveRequestCreateDTO dto);

    /**
     * 更新请假申请
     *
     * @param id 请假申请ID
     * @param dto 更新DTO
     * @param currentUserId 当前用户ID
     * @return 请假申请VO
     */
    LeaveRequestVO updateLeaveRequest(Long id, LeaveRequestUpdateDTO dto, Long currentUserId);

    /**
     * 审批请假申请
     *
     * @param leaveRequestId 请假申请ID
     * @param dto 审批DTO
     * @param approverId 审批人ID
     * @return 请假申请VO
     */
    LeaveRequestVO approveLeaveRequest(Long leaveRequestId, LeaveApprovalDTO dto, Long approverId);

    /**
     * 拒绝请假申请
     *
     * @param leaveRequestId 请假申请ID
     * @param dto 审批DTO
     * @param approverId 审批人ID
     * @return 请假申请VO
     */
    LeaveRequestVO rejectLeaveRequest(Long leaveRequestId, LeaveApprovalDTO dto, Long approverId);

    /**
     * 撤销请假申请
     *
     * @param leaveRequestId 请假申请ID
     * @param userId 申请人ID
     * @return 是否撤销成功
     */
    Boolean cancelLeaveRequest(Long leaveRequestId, Long userId);

    /**
     * 查询请假申请详情
     *
     * @param id 请假申请ID
     * @param currentUserId 当前用户ID
     * @return 请假申请详细信息
     */
    LeaveRequestDetailVO getLeaveRequest(Long id, Long currentUserId);

    /**
     * 分页查询请假申请
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<LeaveRequestVO> pageQuery(PageDTO<LeaveQueryDTO> pageDTO);

    /**
     * 查询待审核请假列表
     *
     * @param approverId 审批人ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageVO<LeaveRequestVO> getPendingLeaves(Long approverId, Integer pageNum, Integer pageSize);

    /**
     * 查询个人请假历史
     *
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param status 请假状态
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageVO<LeaveRequestVO> getMyLeaves(Long userId, LocalDateTime startDate, LocalDateTime endDate, 
                                        LeaveStatus status, Integer pageNum, Integer pageSize);

    /**
     * 同步请假与考勤
     * 当请假被批准时，标记考勤期间为准假，并抑制相关异常
     *
     * @param leaveRequestId 请假申请ID
     */
    void syncLeaveWithAttendance(Long leaveRequestId);

    /**
     * 更新请假申请（自动获取当前用户）
     *
     * @param id 请假申请ID
     * @param dto 更新DTO
     * @return 请假申请VO
     */
    LeaveRequestVO updateLeaveRequest(Long id, LeaveRequestUpdateDTO dto);

    /**
     * 撤销请假申请（自动获取当前用户）
     *
     * @param id 请假申请ID
     * @return 是否撤销成功
     */
    Boolean cancelLeaveRequest(Long id);

    /**
     * 审批请假申请（自动获取当前用户）
     *
     * @param id 请假申请ID
     * @param dto 审批DTO
     * @return 请假申请VO
     */
    LeaveRequestVO approveLeaveRequest(Long id, LeaveApprovalDTO dto);

    /**
     * 查询请假申请详情（自动获取当前用户）
     *
     * @param id 请假申请ID
     * @return 请假申请详细信息
     */
    LeaveRequestDetailVO getLeaveRequestDetail(Long id);

    /**
     * 查询待审核请假列表（自动获取当前用户）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<LeaveRequestVO> getPendingLeaves(PageDTO<Void> pageDTO);

    /**
     * 查询个人请假历史（自动获取当前用户）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<LeaveRequestVO> getMyLeaves(PageDTO<LeaveQueryDTO> pageDTO);
}
