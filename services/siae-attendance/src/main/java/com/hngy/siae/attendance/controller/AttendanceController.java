package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.AttendanceQueryDTO;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordDetailVO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.attendance.service.IAttendanceService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.attendance.permissions.AttendancePermissions;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.hngy.siae.attendance.permissions.AttendancePermissions.*;

/**
 * 考勤记录控制器
 *
 * @author SIAE Team
 */
@Tag(name = "考勤记录管理")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;

    /**
     * 签到
     * 权限要求：已认证用户
     */
    @Operation(summary = "签到")
    @PostMapping("/check-in")
    @SiaeAuthorize("isAuthenticated()")
    @OperationLog(type = "CHECK_IN", module = "ATTENDANCE", description = "签到")
    public Result<AttendanceRecordVO> checkIn(@Valid @RequestBody CheckInDTO dto) {
        AttendanceRecordVO result = attendanceService.checkIn(dto);
        return Result.success(result);
    }

    /**
     * 签退
     * 权限要求：已认证用户
     */
    @Operation(summary = "签退")
    @PostMapping("/check-out")
    @SiaeAuthorize("isAuthenticated()")
    @OperationLog(type = "CHECK_OUT", module = "ATTENDANCE", description = "签退")
    public Result<AttendanceRecordVO> checkOut(@Valid @RequestBody CheckOutDTO dto) {
        AttendanceRecordVO result = attendanceService.checkOut(dto);
        return Result.success(result);
    }

    /**
     * 查询考勤记录详情
     * 权限要求：拥有查看权限或是记录所有者
     */
    @Operation(summary = "查询考勤记录详情")
    @GetMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Record.VIEW + "') or isOwner(#id)")
    public Result<AttendanceRecordDetailVO> getRecord(@PathVariable Long id) {
        AttendanceRecordDetailVO result = attendanceService.getRecordDetail(id);
        return Result.success(result);
    }

    /**
     * 分页查询考勤记录
     * 权限要求：拥有列表查询权限
     */
    @Operation(summary = "分页查询考勤记录")
    @PostMapping("/page")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Record.LIST + "')")
    public Result<PageVO<AttendanceRecordVO>> pageQuery(@Valid @RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
        PageVO<AttendanceRecordVO> result = attendanceService.pageQuery(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询个人考勤历史
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人考勤历史")
    @PostMapping("/my-history")
    @SiaeAuthorize("isAuthenticated()")
    public Result<PageVO<AttendanceRecordVO>> getMyHistory(@Valid @RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
        PageVO<AttendanceRecordVO> result = attendanceService.getMyHistory(pageDTO);
        return Result.success(result);
    }

    /**
     * 导出考勤记录
     * 权限要求：拥有导出权限
     */
    @Operation(summary = "导出考勤记录")
    @GetMapping("/export")
    @SiaeAuthorize("hasPermission('" + AttendancePermissions.Record.EXPORT + "')")
    @OperationLog(type = "EXPORT_RECORDS", module = "ATTENDANCE", description = "导出考勤记录")
    public void exportRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String memberIds,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) {
        attendanceService.exportRecords(startDate, endDate, memberIds, format, response);
    }
}
