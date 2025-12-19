package com.hngy.siae.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.attendance.dto.request.LeaveApprovalDTO;
import com.hngy.siae.attendance.dto.request.LeaveQueryDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestCreateDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestUpdateDTO;
import com.hngy.siae.attendance.dto.response.LeaveRequestDetailVO;
import com.hngy.siae.attendance.dto.response.LeaveRequestVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceResultCodeEnum;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import com.hngy.siae.attendance.service.ILeaveService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.security.utils.SecurityUtil;
import com.hngy.siae.core.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请假服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements ILeaveService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceAnomalyMapper attendanceAnomalyMapper;
    private final SecurityUtil securityUtil;
    private final UserFeignClient userFeignClient;
    private final MediaFeignClient mediaFeignClient;

    /**
     * 创建请假申请
     * 
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
     *
     * @param dto 请假申请创建DTO
     * @return 请假申请VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO createLeaveRequest(LeaveRequestCreateDTO dto) {
        log.info("开始创建请假申请: userId={}, leaveType={}, startDate={}, endDate={}", 
                dto.getUserId(), dto.getLeaveType(), dto.getStartDate(), dto.getEndDate());

        // 1. 验证日期范围 (Requirement 5.2, 5.4)
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        // 2. 检查请假冲突 (Requirement 5.3)
        checkLeaveConflict(dto.getUserId(), dto.getStartDate(), dto.getEndDate());

        // 3. 计算请假天数
        BigDecimal days = calculateLeaveDays(dto.getStartDate(), dto.getEndDate());

        // 4. 分配审批人 (Requirement 5.5)
        Long approverId = assignApprover(dto.getUserId());

        // 5. 创建请假申请实体 (Requirement 5.1)
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUserId(dto.getUserId());
        leaveRequest.setLeaveType(dto.getLeaveType());
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setDays(days);
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setAttachmentFileIds(dto.getAttachmentFileIds());

        // 6. 保存到数据库
        leaveRequestMapper.insert(leaveRequest);

        log.info("请假申请创建成功: leaveRequestId={}, userId={}, days={}, approverId={}", 
                leaveRequest.getId(), dto.getUserId(), days, approverId);

        // 7. 转换为 VO 返回
        return BeanConvertUtil.to(leaveRequest, LeaveRequestVO.class);
    }

    /**
     * 验证日期范围
     * 
     * Requirement 5.2, 5.4: 结束时间必须晚于或等于开始时间
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        AssertUtils.notNull(startDate, AttendanceResultCodeEnum.LEAVE_DATE_INVALID);
        AssertUtils.notNull(endDate, AttendanceResultCodeEnum.LEAVE_DATE_INVALID);
        
        // 结束时间必须晚于或等于开始时间
        boolean isValidRange = !endDate.isBefore(startDate);
        AssertUtils.isTrue(isValidRange, AttendanceResultCodeEnum.LEAVE_DATE_INVALID);
        
        log.debug("日期范围验证通过: startDate={}, endDate={}", startDate, endDate);
    }

    /**
     * 检查请假冲突
     * 
     * Requirement 5.3: 检查是否与已批准的请假冲突
     *
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    private void checkLeaveConflict(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 查询该用户所有已批准的请假申请
        LambdaQueryWrapper<LeaveRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveRequest::getUserId, userId)
                .eq(LeaveRequest::getStatus, LeaveStatus.APPROVED);

        List<LeaveRequest> approvedLeaves = leaveRequestMapper.selectList(queryWrapper);

        // 检查日期是否有重叠
        for (LeaveRequest existingLeave : approvedLeaves) {
            boolean hasOverlap = isDateRangeOverlap(
                    startDate, endDate,
                    existingLeave.getStartDate(), existingLeave.getEndDate()
            );

            if (hasOverlap) {
                log.warn("请假冲突: userId={}, 新请假=[{}, {}], 已有请假=[{}, {}]", 
                        userId, startDate, endDate, 
                        existingLeave.getStartDate(), existingLeave.getEndDate());
                AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_CONFLICT);
            }
        }

        log.debug("请假冲突检查通过: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
    }

    /**
     * 判断两个日期时间范围是否重叠
     *
     * @param start1 范围1开始时间
     * @param end1 范围1结束时间
     * @param start2 范围2开始时间
     * @param end2 范围2结束时间
     * @return 是否重叠
     */
    private boolean isDateRangeOverlap(LocalDateTime start1, LocalDateTime end1, 
                                      LocalDateTime start2, LocalDateTime end2) {
        // 两个时间范围重叠的条件：
        // start1 <= end2 && start2 <= end1
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * 计算请假天数（按小时计算，精确到小数）
     * 
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 请假天数（小时数/24）
     */
    private BigDecimal calculateLeaveDays(LocalDateTime startDate, LocalDateTime endDate) {
        long hours = ChronoUnit.HOURS.between(startDate, endDate);
        // 转换为天数，保留一位小数
        BigDecimal days = BigDecimal.valueOf(hours).divide(BigDecimal.valueOf(24), 1, BigDecimal.ROUND_HALF_UP);
        
        log.debug("计算请假天数: startDate={}, endDate={}, hours={}, days={}", startDate, endDate, hours, days);
        return days;
    }

    /**
     * 分配审批人
     * 
     * Requirement 5.5: 根据组织规则分配审批人
     * 
     * 当前实现：简化版本，返回固定的审批人ID（1L）
     * TODO: 后续需要根据实际的组织架构和审批流程来实现
     * 可能的实现方式：
     * 1. 查询用户所属部门
     * 2. 查询部门负责人
     * 3. 根据请假类型和天数确定审批人级别
     *
     * @param userId 申请人ID
     * @return 审批人ID
     */
    private Long assignApprover(Long userId) {
        // 简化实现：返回固定的审批人ID
        // 在实际应用中，这里应该根据组织架构和审批规则来确定审批人
        Long approverId = 1L;
        
        log.debug("分配审批人: userId={}, approverId={}", userId, approverId);
        return approverId;
    }

    /**
     * 审批请假申请
     * 
     * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
     *
     * @param leaveRequestId 请假申请ID
     * @param dto 审批DTO
     * @param approverId 审批人ID
     * @return 请假申请VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO approveLeaveRequest(Long leaveRequestId, LeaveApprovalDTO dto, Long approverId) {
        log.info("开始审批请假申请: leaveRequestId={}, approved={}, approverId={}", 
                leaveRequestId, dto.getApproved(), approverId);

        // 1. 查询请假申请
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(leaveRequestId);
        AssertUtils.notNull(leaveRequest, AttendanceResultCodeEnum.LEAVE_REQUEST_NOT_FOUND);

        // 2. 验证状态转换 (Requirement 6.3)
        validateStatusTransition(leaveRequest);

        // 3. 验证审批权限 (Requirement 6.4)
        validateApproverPermission(leaveRequest, approverId);

        // 4. 根据审批结果更新状态
        if (Boolean.TRUE.equals(dto.getApproved())) {
            // 批准 (Requirement 6.1)
            leaveRequest.setStatus(LeaveStatus.APPROVED);
            leaveRequest.setApproverId(approverId);
            leaveRequest.setApprovalNote(dto.getReason());
            leaveRequest.setApprovedAt(java.time.LocalDateTime.now());
            
            log.info("请假申请已批准: leaveRequestId={}, approverId={}", leaveRequestId, approverId);
        } else {
            // 拒绝 (Requirement 6.2)
            leaveRequest.setStatus(LeaveStatus.REJECTED);
            leaveRequest.setApproverId(approverId);
            leaveRequest.setApprovalNote(dto.getReason());
            leaveRequest.setApprovedAt(java.time.LocalDateTime.now());
            
            log.info("请假申请已拒绝: leaveRequestId={}, approverId={}, reason={}", 
                    leaveRequestId, approverId, dto.getReason());
        }

        // 5. 更新数据库
        leaveRequestMapper.updateById(leaveRequest);

        // 6. 如果批准，同步请假与考勤 (Requirements 7.1, 7.2, 7.4, 7.5)
        if (Boolean.TRUE.equals(dto.getApproved())) {
            syncLeaveWithAttendance(leaveRequestId);
        }

        // 7. 发送通知 (Requirement 6.5)
        sendApprovalNotification(leaveRequest);

        // 8. 转换为 VO 返回
        return BeanConvertUtil.to(leaveRequest, LeaveRequestVO.class);
    }

    /**
     * 拒绝请假申请
     * 
     * Requirements: 6.2, 6.3, 6.4, 6.5
     *
     * @param leaveRequestId 请假申请ID
     * @param dto 审批DTO
     * @param approverId 审批人ID
     * @return 请假申请VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO rejectLeaveRequest(Long leaveRequestId, LeaveApprovalDTO dto, Long approverId) {
        log.info("开始拒绝请假申请: leaveRequestId={}, approverId={}", leaveRequestId, approverId);

        // 调用 approveLeaveRequest 方法，传入 approved=false
        LeaveApprovalDTO rejectDto = new LeaveApprovalDTO();
        rejectDto.setApproved(false);
        rejectDto.setReason(dto.getReason());

        return approveLeaveRequest(leaveRequestId, rejectDto, approverId);
    }

    /**
     * 验证状态转换
     * 
     * Requirement 6.3: 只有待审核状态的请假申请才能被审批
     *
     * @param leaveRequest 请假申请
     */
    private void validateStatusTransition(LeaveRequest leaveRequest) {
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            log.warn("请假申请状态无效，无法审批: leaveRequestId={}, currentStatus={}", 
                    leaveRequest.getId(), leaveRequest.getStatus());
            AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_STATUS_INVALID);
        }
        
        log.debug("状态转换验证通过: leaveRequestId={}, status={}", 
                leaveRequest.getId(), leaveRequest.getStatus());
    }

    /**
     * 验证审批权限
     * 
     * Requirement 6.4: 验证审批人是否有权限审批该请假申请
     * 
     * 当前实现：管理员（拥有审批权限的用户）可以审批所有请假申请
     * 不再限制只能审批指定给自己的请假
     *
     * @param leaveRequest 请假申请
     * @param approverId 审批人ID
     */
    private void validateApproverPermission(LeaveRequest leaveRequest, Long approverId) {
        // 管理员拥有审批权限即可审批所有请假，不再校验是否是指定审批人
        // 权限校验已在 Controller 层通过 @SiaeAuthorize 完成
        log.debug("审批权限验证通过: leaveRequestId={}, approverId={}", 
                leaveRequest.getId(), approverId);
    }

    /**
     * 发送审批通知
     * 
     * Requirement 6.5: 审批后发送通知给申请人
     * 
     * 当前实现：简化版本，只记录日志
     * TODO: 后续需要集成实际的通知服务
     * 可能的实现方式：
     * 1. 通过 RabbitMQ 发送消息到通知服务
     * 2. 调用通知服务的 REST API
     * 3. 使用 Spring Event 发布事件
     *
     * @param leaveRequest 请假申请
     */
    private void sendApprovalNotification(LeaveRequest leaveRequest) {
        // 简化实现：只记录日志
        // 在实际应用中，这里应该调用通知服务发送通知
        String statusText = leaveRequest.getStatus() == LeaveStatus.APPROVED ? "已批准" : "已拒绝";
        
        log.info("发送审批通知: userId={}, leaveRequestId={}, status={}, approvalNote={}", 
                leaveRequest.getUserId(), leaveRequest.getId(), statusText, leaveRequest.getApprovalNote());
        
        // TODO: 集成通知服务
        // notificationService.sendLeaveApprovalNotification(leaveRequest);
    }

    /**
     * 撤销请假申请
     * 
     * Requirement 5.1: 成员可以撤销自己的请假申请
     * 
     * 业务规则：
     * 1. 只有申请人本人可以撤销
     * 2. 只有待审核(PENDING)状态的请假可以撤销
     * 3. 已批准(APPROVED)或已拒绝(REJECTED)的请假不能撤销
     * 4. 撤销后状态变为已撤销(CANCELLED)
     *
     * @param leaveRequestId 请假申请ID
     * @param userId 申请人ID
     * @return 是否撤销成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelLeaveRequest(Long leaveRequestId, Long userId) {
        log.info("开始撤销请假申请: leaveRequestId={}, userId={}", leaveRequestId, userId);

        // 1. 查询请假申请
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(leaveRequestId);
        AssertUtils.notNull(leaveRequest, AttendanceResultCodeEnum.LEAVE_REQUEST_NOT_FOUND);

        // 2. 验证是否是申请人本人
        if (!leaveRequest.getUserId().equals(userId)) {
            log.warn("非申请人尝试撤销请假: leaveRequestId={}, requestUserId={}, actualUserId={}", 
                    leaveRequestId, leaveRequest.getUserId(), userId);
            AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_APPROVAL_PERMISSION_DENIED);
        }

        // 3. 验证请假状态是否可以撤销
        // 只有待审核状态的请假可以撤销
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            log.warn("请假状态不允许撤销: leaveRequestId={}, currentStatus={}", 
                    leaveRequestId, leaveRequest.getStatus());
            AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_STATUS_INVALID);
        }

        // 4. 更新状态为已撤销
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        int updateCount = leaveRequestMapper.updateById(leaveRequest);

        if (updateCount > 0) {
            log.info("请假申请撤销成功: leaveRequestId={}, userId={}", leaveRequestId, userId);
            return true;
        } else {
            log.error("请假申请撤销失败: leaveRequestId={}, userId={}", leaveRequestId, userId);
            return false;
        }
    }

    /**
     * 更新请假申请
     *
     * @param id 请假申请ID
     * @param dto 更新DTO
     * @param currentUserId 当前用户ID
     * @return 请假申请VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO updateLeaveRequest(Long id, LeaveRequestUpdateDTO dto, Long currentUserId) {
        log.info("更新请假申请: id={}, currentUserId={}", id, currentUserId);

        // 查询请假申请
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        AssertUtils.notNull(leaveRequest, AttendanceResultCodeEnum.LEAVE_REQUEST_NOT_FOUND);

        // 检查权限 - 只有申请人可以更新
        if (!leaveRequest.getUserId().equals(currentUserId)) {
            log.warn("用户 {} 无权更新请假申请 {}", currentUserId, id);
            AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_APPROVAL_PERMISSION_DENIED);
        }

        // 只有待审核状态的请假可以更新
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            log.warn("请假申请状态不允许更新: id={}, status={}", id, leaveRequest.getStatus());
            AssertUtils.fail(AttendanceResultCodeEnum.LEAVE_STATUS_INVALID);
        }

        // 更新字段
        if (dto.getLeaveType() != null) {
            leaveRequest.setLeaveType(dto.getLeaveType());
        }
        if (dto.getStartDate() != null) {
            leaveRequest.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            leaveRequest.setEndDate(dto.getEndDate());
        }
        if (dto.getReason() != null) {
            leaveRequest.setReason(dto.getReason());
        }
        if (dto.getAttachmentFileIds() != null) {
            leaveRequest.setAttachmentFileIds(dto.getAttachmentFileIds());
        }

        // 重新计算天数
        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            BigDecimal days = calculateLeaveDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());
            leaveRequest.setDays(days);
        }

        leaveRequestMapper.updateById(leaveRequest);

        log.info("请假申请更新成功: id={}", id);
        return BeanConvertUtil.to(leaveRequest, LeaveRequestVO.class);
    }

    /**
     * 查询请假申请详情
     *
     * @param id 请假申请ID
     * @param currentUserId 当前用户ID
     * @return 请假申请详细信息
     */
    @Override
    public LeaveRequestDetailVO getLeaveRequest(Long id, Long currentUserId) {
        log.info("查询请假申请详情: id={}, currentUserId={}", id, currentUserId);

        // 查询请假申请
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(id);
        AssertUtils.notNull(leaveRequest, AttendanceResultCodeEnum.LEAVE_REQUEST_NOT_FOUND);

        // 转换为 VO 并填充用户信息和附件URL
        LeaveRequestDetailVO detailVO = BeanConvertUtil.to(leaveRequest, LeaveRequestDetailVO.class);
        enrichLeaveRequestDetailVO(detailVO);
        return detailVO;
    }

    /**
     * 分页查询请假申请
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageVO<LeaveRequestVO> pageQuery(PageDTO<LeaveQueryDTO> pageDTO) {
        log.info("分页查询请假申请: pageNum={}, pageSize={}", pageDTO.getPageNum(), pageDTO.getPageSize());

        LeaveQueryDTO queryDTO = pageDTO.getParams();

        // 构建查询条件
        LambdaQueryWrapper<LeaveRequest> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            if (queryDTO.getUserId() != null) {
                queryWrapper.eq(LeaveRequest::getUserId, queryDTO.getUserId());
            }
            if (queryDTO.getUserIds() != null && !queryDTO.getUserIds().isEmpty()) {
                queryWrapper.in(LeaveRequest::getUserId, queryDTO.getUserIds());
            }
            if (queryDTO.getLeaveType() != null) {
                queryWrapper.eq(LeaveRequest::getLeaveType, queryDTO.getLeaveType());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(LeaveRequest::getStatus, queryDTO.getStatus());
            }
            if (queryDTO.getApproverId() != null) {
                queryWrapper.eq(LeaveRequest::getApproverId, queryDTO.getApproverId());
            }
            if (queryDTO.getStartDate() != null) {
                queryWrapper.ge(LeaveRequest::getStartDate, queryDTO.getStartDate());
            }
            if (queryDTO.getEndDate() != null) {
                queryWrapper.le(LeaveRequest::getEndDate, queryDTO.getEndDate());
            }
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(LeaveRequest::getCreatedAt);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<LeaveRequest> page = 
                PageConvertUtil.toPage(pageDTO);
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> resultPage = 
                leaveRequestMapper.selectPage(page, queryWrapper);

        // 转换为 VO 并填充用户信息
        PageVO<LeaveRequestVO> result = PageConvertUtil.convert(resultPage, LeaveRequestVO.class);
        enrichLeaveRequestVOList(result.getRecords());
        return result;
    }

    /**
     * 查询待审核请假列表
     *
     * @param approverId 审批人ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageVO<LeaveRequestVO> getPendingLeaves(Long approverId, Integer pageNum, Integer pageSize) {
        log.info("查询待审核请假列表: approverId={}, pageNum={}, pageSize={}", approverId, pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<LeaveRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveRequest::getApproverId, approverId)
                .eq(LeaveRequest::getStatus, LeaveStatus.PENDING)
                .orderByAsc(LeaveRequest::getCreatedAt);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<LeaveRequest> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> resultPage = 
                leaveRequestMapper.selectPage(page, queryWrapper);

        // 转换为 VO 并填充用户信息
        PageVO<LeaveRequestVO> result = PageConvertUtil.convert(resultPage, LeaveRequestVO.class);
        enrichLeaveRequestVOList(result.getRecords());
        return result;
    }

    /**
     * 查询个人请假历史
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param status 请假状态
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageVO<LeaveRequestVO> getMyLeaves(Long userId, LocalDateTime startDate, LocalDateTime endDate,
                                               LeaveStatus status, Integer pageNum, Integer pageSize) {
        log.info("查询个人请假历史: userId={}, startDate={}, endDate={}, status={}, pageNum={}, pageSize={}",
                userId, startDate, endDate, status, pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<LeaveRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveRequest::getUserId, userId);

        if (startDate != null) {
            queryWrapper.ge(LeaveRequest::getStartDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(LeaveRequest::getEndDate, endDate);
        }
        if (status != null) {
            queryWrapper.eq(LeaveRequest::getStatus, status);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(LeaveRequest::getCreatedAt);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<LeaveRequest> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> resultPage = 
                leaveRequestMapper.selectPage(page, queryWrapper);

        // 转换为 VO 并填充用户信息
        PageVO<LeaveRequestVO> result = PageConvertUtil.convert(resultPage, LeaveRequestVO.class);
        enrichLeaveRequestVOList(result.getRecords());
        return result;
    }

    /**
     * 同步请假与考勤
     * 
     * Requirements: 7.1, 7.2, 7.4, 7.5
     * 
     * 当请假被批准时：
     * 1. 标记请假期间的考勤记录为准假（通过备注字段）
     * 2. 抑制请假期间的缺勤异常（设置 suppressedByLeave 字段）
     * 3. 维护请假与考勤记录之间的引用完整性
     * 
     * 业务逻辑：
     * - 查询请假期间内的所有考勤记录
     * - 在备注中标记为"准假"
     * - 查询请假期间内的所有缺勤异常
     * - 将这些异常标记为被请假抑制
     *
     * @param leaveRequestId 请假申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLeaveWithAttendance(Long leaveRequestId) {
        log.info("开始同步请假与考勤: leaveRequestId={}", leaveRequestId);

        // 1. 查询请假申请 (Requirement 7.5: 维护引用完整性)
        LeaveRequest leaveRequest = leaveRequestMapper.selectById(leaveRequestId);
        AssertUtils.notNull(leaveRequest, AttendanceResultCodeEnum.LEAVE_REQUEST_NOT_FOUND);

        // 2. 只处理已批准的请假
        if (leaveRequest.getStatus() != LeaveStatus.APPROVED) {
            log.warn("请假申请未批准，跳过同步: leaveRequestId={}, status={}", 
                    leaveRequestId, leaveRequest.getStatus());
            return;
        }

        Long userId = leaveRequest.getUserId();
        LocalDateTime startDate = leaveRequest.getStartDate();
        LocalDateTime endDate = leaveRequest.getEndDate();

        log.info("处理已批准的请假: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 3. 标记考勤期间为准假 (Requirement 7.1)
        markExcusedAbsence(userId, startDate, endDate, leaveRequestId);

        // 4. 抑制缺勤异常 (Requirement 7.2)
        suppressAbsenceAnomalies(userId, startDate, endDate, leaveRequestId);

        log.info("请假与考勤同步完成: leaveRequestId={}, userId={}, affectedDays={}", 
                leaveRequestId, userId, ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    /**
     * 标记考勤期间为准假
     * 
     * Requirement 7.1: 批准的请假应标记所有考勤期间为准假
     * 
     * 实现方式：
     * 1. 查询请假期间内的所有考勤记录
     * 2. 在备注字段中添加"准假"标记
     * 3. 如果该日期没有考勤记录，不创建新记录（因为可能是休息日）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param leaveRequestId 请假申请ID
     */
    private void markExcusedAbsence(Long userId, LocalDateTime startDate, LocalDateTime endDate, Long leaveRequestId) {
        log.debug("标记准假: userId={}, startDate={}, endDate={}, leaveRequestId={}", 
                userId, startDate, endDate, leaveRequestId);

        // 查询请假期间内的所有考勤记录
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, userId)
                .ge(AttendanceRecord::getAttendanceDate, startDate.toLocalDate())
                .le(AttendanceRecord::getAttendanceDate, endDate.toLocalDate());

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(queryWrapper);

        // 更新每条记录的备注，标记为准假
        for (AttendanceRecord record : records) {
            String originalRemark = record.getRemark() != null ? record.getRemark() : "";
            String excusedMark = String.format("[准假-请假ID:%d]", leaveRequestId);
            
            // 如果备注中还没有准假标记，则添加
            if (!originalRemark.contains(excusedMark)) {
                String newRemark = originalRemark.isEmpty() ? excusedMark : originalRemark + " " + excusedMark;
                record.setRemark(newRemark);
                attendanceRecordMapper.updateById(record);
                
                log.debug("考勤记录已标记为准假: recordId={}, attendanceDate={}", 
                        record.getId(), record.getAttendanceDate());
            }
        }

        log.info("准假标记完成: userId={}, affectedRecords={}", userId, records.size());
    }

    /**
     * 抑制缺勤异常
     * 
     * Requirement 7.2: 检测到缺勤异常时，如果有批准的请假覆盖该日期，应抑制异常
     * 
     * 实现方式：
     * 1. 查询请假期间内的所有缺勤类型异常
     * 2. 设置 suppressedByLeave 字段为请假申请ID
     * 3. 将 resolved 字段设置为 true（表示已通过请假解决）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param leaveRequestId 请假申请ID
     */
    private void suppressAbsenceAnomalies(Long userId, LocalDateTime startDate, LocalDateTime endDate, Long leaveRequestId) {
        log.debug("抑制缺勤异常: userId={}, startDate={}, endDate={}, leaveRequestId={}", 
                userId, startDate, endDate, leaveRequestId);

        // 查询请假期间内的所有缺勤异常
        LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceAnomaly::getUserId, userId)
                .eq(AttendanceAnomaly::getAnomalyType, AnomalyType.ABSENCE)
                .ge(AttendanceAnomaly::getAnomalyDate, startDate.toLocalDate())
                .le(AttendanceAnomaly::getAnomalyDate, endDate.toLocalDate())
                .isNull(AttendanceAnomaly::getSuppressedByLeave); // 只处理未被抑制的异常

        List<AttendanceAnomaly> anomalies = attendanceAnomalyMapper.selectList(queryWrapper);

        // 批量更新异常记录
        for (AttendanceAnomaly anomaly : anomalies) {
            anomaly.setSuppressedByLeave(leaveRequestId);
            anomaly.setResolved(true);
            anomaly.setHandlerNote(String.format("已通过请假抑制（请假ID: %d）", leaveRequestId));
            attendanceAnomalyMapper.updateById(anomaly);
            
            log.debug("缺勤异常已抑制: anomalyId={}, anomalyDate={}", 
                    anomaly.getId(), anomaly.getAnomalyDate());
        }

        log.info("缺勤异常抑制完成: userId={}, suppressedAnomalies={}", userId, anomalies.size());
    }

    // ==================== 新增方法：简化版本（自动获取当前用户） ====================

    /**
     * 更新请假申请（自动获取当前用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO updateLeaveRequest(Long id, LeaveRequestUpdateDTO dto) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return updateLeaveRequest(id, dto, currentUserId);
    }

    /**
     * 撤销请假申请（自动获取当前用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelLeaveRequest(Long id) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return cancelLeaveRequest(id, currentUserId);
    }

    /**
     * 审批请假申请（自动获取当前用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LeaveRequestVO approveLeaveRequest(Long id, LeaveApprovalDTO dto) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return approveLeaveRequest(id, dto, currentUserId);
    }

    /**
     * 查询请假申请详情（自动获取当前用户）
     */
    @Override
    public LeaveRequestDetailVO getLeaveRequestDetail(Long id) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return getLeaveRequest(id, currentUserId);
    }

    /**
     * 查询待审核请假列表（管理员查看所有待审核请假）
     */
    @Override
    public com.hngy.siae.core.dto.PageVO<LeaveRequestVO> getPendingLeaves(com.hngy.siae.core.dto.PageDTO<Void> pageDTO) {
        log.info("查询所有待审核请假列表: pageNum={}, pageSize={}", pageDTO.getPageNum(), pageDTO.getPageSize());

        // 构建查询条件 - 只查询待审核状态，不限制审批人
        LambdaQueryWrapper<LeaveRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaveRequest::getStatus, LeaveStatus.PENDING)
                .orderByAsc(LeaveRequest::getCreatedAt);

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<LeaveRequest> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        com.baomidou.mybatisplus.core.metadata.IPage<LeaveRequest> resultPage = 
                leaveRequestMapper.selectPage(page, queryWrapper);

        // 转换为 VO 并填充用户信息
        PageVO<LeaveRequestVO> result = PageConvertUtil.convert(resultPage, LeaveRequestVO.class);
        enrichLeaveRequestVOList(result.getRecords());
        return result;
    }

    /**
     * 查询个人请假历史（自动获取当前用户）
     */
    @Override
    public com.hngy.siae.core.dto.PageVO<LeaveRequestVO> getMyLeaves(com.hngy.siae.core.dto.PageDTO<LeaveQueryDTO> pageDTO) {
        Long currentUserId = securityUtil.getCurrentUserId();
        LeaveQueryDTO params = pageDTO.getParams();
        LocalDateTime startDate = params != null ? params.getStartDate() : null;
        LocalDateTime endDate = params != null ? params.getEndDate() : null;
        LeaveStatus status = params != null ? params.getStatus() : null;
        return getMyLeaves(currentUserId, startDate, endDate, status, pageDTO.getPageNum(), pageDTO.getPageSize());
    }

    // ==================== 辅助方法：填充用户信息和附件URL ====================

    /**
     * 批量填充请假申请VO的用户信息（申请人和审批人的姓名、头像）
     *
     * @param voList 请假申请VO列表
     */
    private void enrichLeaveRequestVOList(List<LeaveRequestVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        for (LeaveRequestVO vo : voList) {
            if (vo.getUserId() != null) {
                userIds.add(vo.getUserId());
            }
            if (vo.getApproverId() != null) {
                userIds.add(vo.getApproverId());
            }
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        Map<Long, UserProfileSimpleVO> userMap = fetchUserProfiles(userIds);

        // 填充用户信息
        for (LeaveRequestVO vo : voList) {
            // 填充申请人信息
            if (vo.getUserId() != null) {
                UserProfileSimpleVO userProfile = userMap.get(vo.getUserId());
                if (userProfile != null) {
                    vo.setUserName(userProfile.getNickname() != null ? userProfile.getNickname() : userProfile.getUsername());
                    vo.setUserAvatarUrl(userProfile.getAvatarUrl());
                }
            }
            // 填充审批人信息
            if (vo.getApproverId() != null) {
                UserProfileSimpleVO approverProfile = userMap.get(vo.getApproverId());
                if (approverProfile != null) {
                    vo.setApproverName(approverProfile.getNickname() != null ? approverProfile.getNickname() : approverProfile.getUsername());
                    vo.setApproverAvatarUrl(approverProfile.getAvatarUrl());
                }
            }
        }
    }

    /**
     * 填充请假详情VO的用户信息和附件URL
     *
     * @param detailVO 请假详情VO
     */
    private void enrichLeaveRequestDetailVO(LeaveRequestDetailVO detailVO) {
        if (detailVO == null) {
            return;
        }

        // 收集用户ID
        Set<Long> userIds = new HashSet<>();
        if (detailVO.getUserId() != null) {
            userIds.add(detailVO.getUserId());
        }
        if (detailVO.getApproverId() != null) {
            userIds.add(detailVO.getApproverId());
        }

        // 批量查询用户信息
        if (!userIds.isEmpty()) {
            Map<Long, UserProfileSimpleVO> userMap = fetchUserProfiles(userIds);

            // 填充申请人信息
            if (detailVO.getUserId() != null) {
                UserProfileSimpleVO userProfile = userMap.get(detailVO.getUserId());
                if (userProfile != null) {
                    detailVO.setUserName(userProfile.getNickname() != null ? userProfile.getNickname() : userProfile.getUsername());
                    detailVO.setUserAvatarUrl(userProfile.getAvatarUrl());
                }
            }
            // 填充审批人信息
            if (detailVO.getApproverId() != null) {
                UserProfileSimpleVO approverProfile = userMap.get(detailVO.getApproverId());
                if (approverProfile != null) {
                    detailVO.setApproverName(approverProfile.getNickname() != null ? approverProfile.getNickname() : approverProfile.getUsername());
                    detailVO.setApproverAvatarUrl(approverProfile.getAvatarUrl());
                }
            }
        }

        // 获取附件URL
        if (detailVO.getAttachmentFileIds() != null && !detailVO.getAttachmentFileIds().isEmpty()) {
            List<String> attachmentUrls = fetchFileUrls(detailVO.getAttachmentFileIds());
            detailVO.setAttachmentUrls(attachmentUrls);
        }
    }

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户ID -> 用户信息的映射
     */
    private Map<Long, UserProfileSimpleVO> fetchUserProfiles(Set<Long> userIds) {
        try {
            return userFeignClient.batchGetUserProfiles(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败: userIds={}, error={}", userIds, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 批量获取文件URL
     *
     * @param fileIds 文件ID列表
     * @return 文件URL列表（与fileIds顺序对应）
     */
    private List<String> fetchFileUrls(List<String> fileIds) {
        try {
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(fileIds);
            request.setExpirySeconds(86400); // 24小时有效期
            BatchUrlVO response = mediaFeignClient.batchGetFileUrls(request);
            
            if (response != null && response.getUrls() != null) {
                // 按原始fileIds顺序返回URL
                return fileIds.stream()
                        .map(fileId -> response.getUrls().get(fileId))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("批量获取文件URL失败: fileIds={}, error={}", fileIds, e.getMessage());
            return Collections.emptyList();
        }
    }
}
