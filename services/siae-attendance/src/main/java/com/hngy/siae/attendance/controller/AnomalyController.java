package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.AnomalyHandleDTO;
import com.hngy.siae.attendance.dto.request.AnomalyQueryDTO;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.service.IAnomalyDetectionService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.AttendancePermissions.Anomaly;

/**
 * 考勤异常控制器
 *
 * @author SIAE Team
 */
@Tag(name = "考勤异常管理")
@RestController
@RequestMapping("/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final IAnomalyDetectionService anomalyDetectionService;

    /**
     * 查询考勤异常详情
     * 权限要求：拥有查看权限或是异常记录所有者
     */
    @Operation(summary = "查询考勤异常详情")
    @GetMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + Anomaly.VIEW + "') or isOwner(#id)")
    public Result<AttendanceAnomalyVO> getAnomaly(@PathVariable Long id) {
        AttendanceAnomalyVO result = anomalyDetectionService.getAnomaly(id);
        return Result.success(result);
    }

    /**
     * 分页查询考勤异常
     * 权限要求：拥有列表查询权限
     */
    @Operation(summary = "分页查询考勤异常")
    @PostMapping("/page")
    @SiaeAuthorize("hasPermission('" + Anomaly.LIST + "')")
    public Result<PageVO<AttendanceAnomalyVO>> pageQuery(@Valid @RequestBody PageDTO<AnomalyQueryDTO> pageDTO) {
        PageVO<AttendanceAnomalyVO> result = anomalyDetectionService.pageQuery(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询个人考勤异常
     * 权限要求：已认证用户（查询自己的数据）
     */
    @Operation(summary = "查询个人考勤异常")
    @PostMapping("/my-anomalies")
    @SiaeAuthorize("isAuthenticated()")
    public Result<PageVO<AttendanceAnomalyVO>> getMyAnomalies(@Valid @RequestBody PageDTO<Void> pageDTO) {
        PageVO<AttendanceAnomalyVO> result = anomalyDetectionService.getMyAnomalies(pageDTO);
        return Result.success(result);
    }

    /**
     * 查询未处理的考勤异常
     * 权限要求：拥有处理权限
     */
    @Operation(summary = "查询未处理的考勤异常")
    @PostMapping("/unresolved")
    @SiaeAuthorize("hasPermission('" + Anomaly.HANDLE + "')")
    public Result<PageVO<AttendanceAnomalyVO>> getUnresolvedAnomalies(@Valid @RequestBody PageDTO<Void> pageDTO) {
        PageVO<AttendanceAnomalyVO> result = anomalyDetectionService.getUnresolvedAnomalies(pageDTO);
        return Result.success(result);
    }

    /**
     * 处理考勤异常
     * 权限要求：拥有处理权限
     */
    @Operation(summary = "处理考勤异常")
    @PostMapping("/{id}/handle")
    @SiaeAuthorize("hasPermission('" + Anomaly.HANDLE + "')")
    @OperationLog(type = "HANDLE_ANOMALY", module = "ANOMALY", description = "处理考勤异常")
    public Result<AttendanceAnomalyVO> handleAnomaly(
            @PathVariable Long id,
            @Valid @RequestBody AnomalyHandleDTO dto) {
        AttendanceAnomalyVO result = anomalyDetectionService.handleAnomaly(id, dto);
        return Result.success(result);
    }
}
