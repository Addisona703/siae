package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.LeaveApprovalDTO;
import com.hngy.siae.attendance.dto.request.LeaveQueryDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestCreateDTO;
import com.hngy.siae.attendance.dto.request.LeaveRequestUpdateDTO;
import com.hngy.siae.attendance.dto.response.LeaveRequestDetailVO;
import com.hngy.siae.attendance.dto.response.LeaveRequestVO;
import com.hngy.siae.attendance.service.ILeaveService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.attendance.permissions.AttendancePermissions;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 请假管理控制器
 *
 * @author SIAE Team
 */
@Tag(name = "请假管理")
@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final ILeaveService leaveService;

    /**
     * 创建请假申请
     * 权限要求：已认证用户
     */
    @Operation(summary = "创建请假申请")
    @PostMapping
    @SiaeAuthorize("isAuthenticated()")
    @OperationLog(type = "CREATE_LEAVE", module = "LEAVE", description = "创建请假申请")
    public Result<LeaveRequestVO> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateDTO dto) {
        LeaveRequestVO result = leaveService.createLeaveRequest(dto);
        return Result.success(result);
    }

    /**
     * 更新请假申请
     * 权限要求：拥有更新权限或是申请人本人
     */
    @Operation(summary = "更新请假申请")
    @PutMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Leave.UPDATE + "') or isOwner(#id)")
    @OperationLog(type = "UPDATE_LEAVE", module = "LEAVE", description = "更新请假申请")
    public Result<LeaveRequestVO> updateLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestUpdateDTO dto) {
        LeaveRequestVO result = leaveService.updateLeaveRequest(id, dto);
        return Result.success(result);
    }

    /**
     * 撤销请假申请
     * 权限要求：申请人本人
     */
    @Operation(summary = "撤销请假申请")
    @PostMapping("/{id}/cancel")
    @SiaeAuthorize("isOwner(#id)")
    @OperationLog(type = "CANCEL_LEAVE", module = "LEAVE", description = "撤销请假申请")
    public Result<Boolean> cancelLeaveRequest(@PathVariable Long id) {
        Boolean result = leaveService.cancelLeaveRequest(id);
        return Result.success(result);
    }

    /**
     * 审批请假申请
     * 权限要求：拥有审批权限
     */
    @Operation(summary = "审批请假申请")
    @PostMapping("/{id}/approve")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Leave.APPROVE + "')")
    @OperationLog(type = "APPROVE_LEAVE", module = "LEAVE", description = "审批请假申请")
    public Result<LeaveRequestVO> approveLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalDTO dto) {
        LeaveRequestVO result = leaveService.approveLeaveRequest(id, dto);
        return Result.success(result);
    }

    /**
     * 查询请假申请详情
     * 权限要求：拥有查看权限或是申请人本人
     */
    @Operation(summary = "查询请假申请详情")
    @GetMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Leave.VIEW + "') or isOwner(#id)")
    public Result<LeaveRequestDetailVO> getLeaveRequest(@PathVariable Long id) {
        LeaveRequestDetailVO result = leaveService.getLeaveRequestDetail(id);
        return Result.success(result);
    }

    /**
     * 分页查询请假申请
     * 权限要求：拥有列表查询权限
     */
    @Operation(summary = "分页查询请假申请")
    @PostMapping("/page")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Leave.LIST + "')")
    public Result<PageVO<LeaveRequestVO>> pageQuery(@Valid @RequestBody PageDTO<LeaveQueryDTO> pageDTO) {
        PageVO<LeaveRequestVO> result = leaveService.pageQuery(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询待审核请假列表
     * 权限要求：拥有审批权限
     */
    @Operation(summary = "查询待审核请假列表")
    @PostMapping("/pending")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Leave.APPROVE + "')")
    public Result<PageVO<LeaveRequestVO>> getPendingLeaves(@Valid @RequestBody PageDTO<Void> pageDTO) {
        PageVO<LeaveRequestVO> result = leaveService.getPendingLeaves(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询个人请假历史
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人请假历史")
    @PostMapping("/my-leaves")
    @SiaeAuthorize("isAuthenticated()")
    public Result<PageVO<LeaveRequestVO>> getMyLeaves(@Valid @RequestBody PageDTO<LeaveQueryDTO> pageDTO) {
        PageVO<LeaveRequestVO> result = leaveService.getMyLeaves(pageDTO);
        return Result.success(result);
    }
}
