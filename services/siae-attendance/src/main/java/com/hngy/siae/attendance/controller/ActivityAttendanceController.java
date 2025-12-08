package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.AttendanceQueryDTO;
import com.hngy.siae.attendance.dto.request.AttendanceRuleCreateDTO;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.dto.response.ActivityAttendanceStatisticsVO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.attendance.dto.response.AttendanceRuleVO;
import com.hngy.siae.attendance.service.IAttendanceService;
import com.hngy.siae.attendance.service.IRuleService;
import com.hngy.siae.attendance.service.IStatisticsService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.attendance.permissions.AttendancePermissions.*;

/**
 * 活动考勤控制器
 *
 * @author SIAE Team
 */
@Tag(name = "活动考勤管理")
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityAttendanceController {

    private final IAttendanceService attendanceService;
    private final IRuleService ruleService;
    private final IStatisticsService statisticsService;

    /**
     * 活动签到
     * 权限要求：已认证用户
     */
    @Operation(summary = "活动签到")
    @PostMapping("/check-in")
    @SiaeAuthorize("isAuthenticated()")
    @OperationLog(type = "ACTIVITY_CHECK_IN", module = "ACTIVITY_ATTENDANCE", description = "活动签到")
    public Result<AttendanceRecordVO> checkIn(@Valid @RequestBody CheckInDTO dto) {
        AttendanceRecordVO result = attendanceService.checkIn(dto);
        return Result.success(result);
    }

    /**
     * 活动签退
     * 权限要求：已认证用户
     */
    @Operation(summary = "活动签退")
    @PostMapping("/check-out")
    @SiaeAuthorize("isAuthenticated()")
    @OperationLog(type = "ACTIVITY_CHECK_OUT", module = "ACTIVITY_ATTENDANCE", description = "活动签退")
    public Result<AttendanceRecordVO> checkOut(@Valid @RequestBody CheckOutDTO dto) {
        AttendanceRecordVO result = attendanceService.checkOut(dto);
        return Result.success(result);
    }

    /**
     * 分页查询活动考勤记录
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "分页查询活动考勤记录")
    @PostMapping("/records/page")
    @SiaeAuthorize("hasPermission('" + Activity.LIST + "')")
    public Result<PageVO<AttendanceRecordVO>> pageQueryActivityRecords(@Valid @RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
        PageVO<AttendanceRecordVO> result = attendanceService.pageQueryActivityRecords(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询活动考勤记录列表
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "查询活动考勤记录列表")
    @GetMapping("/{activityId}/records")
    @SiaeAuthorize("hasPermission('" + Activity.VIEW + "')")
    public Result<List<AttendanceRecordVO>> listActivityRecords(
            @PathVariable Long activityId,
            @RequestParam(required = false) Long userId) {
        List<AttendanceRecordVO> result = attendanceService.listActivityRecords(activityId, userId);
        return Result.success(result);
    }

    /**
     * 查询个人活动考勤历史
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人活动考勤历史")
    @PostMapping("/my-history")
    @SiaeAuthorize("isAuthenticated()")
    public Result<PageVO<AttendanceRecordVO>> getMyActivityHistory(@Valid @RequestBody PageDTO<AttendanceQueryDTO> pageDTO) {
        PageVO<AttendanceRecordVO> result = attendanceService.getMyActivityHistory(pageDTO);
        return Result.success(result);
    }

    /**
     * 创建活动考勤规则
     * 权限要求：拥有创建权限
     */
    @Operation(summary = "创建活动考勤规则")
    @PostMapping("/rules")
    @SiaeAuthorize("hasPermission('" + Rule.CREATE + "')")
    @OperationLog(type = "CREATE_ACTIVITY_RULE", module = "ACTIVITY_ATTENDANCE", description = "创建活动考勤规则")
    public Result<AttendanceRuleVO> createActivityRule(@Valid @RequestBody AttendanceRuleCreateDTO dto) {
        AttendanceRuleVO result = ruleService.createActivityRule(dto);
        return Result.success(result);
    }

    /**
     * 查询活动考勤规则列表
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "查询活动考勤规则列表")
    @GetMapping("/rules")
    @SiaeAuthorize("hasPermission('" + Rule.LIST + "')")
    public Result<List<AttendanceRuleVO>> listActivityRules(@RequestParam(required = false) Long activityId) {
        List<AttendanceRuleVO> result = ruleService.listActivityRules(activityId);
        return Result.success(result);
    }

    /**
     * 查询活动考勤统计
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "查询活动考勤统计")
    @GetMapping("/{activityId}/statistics")
    @SiaeAuthorize("hasPermission('" + Statistics.VIEW + "')")
    public Result<ActivityAttendanceStatisticsVO> getActivityStatistics(@PathVariable Long activityId) {
        ActivityAttendanceStatisticsVO result = statisticsService.calculateActivityStatistics(activityId);
        return Result.success(result);
    }
}
