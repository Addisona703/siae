package com.hngy.siae.attendance.controller;

import com.hngy.siae.attendance.annotation.OperationLog;
import com.hngy.siae.attendance.dto.request.AttendanceRuleCreateDTO;
import com.hngy.siae.attendance.dto.request.AttendanceRuleUpdateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRuleDetailVO;
import com.hngy.siae.attendance.dto.response.AttendanceRuleVO;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.service.IRuleService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.attendance.permissions.AttendancePermissions.Rule;

/**
 * 考勤规则控制器
 *
 * @author SIAE Team
 */
@Tag(name = "考勤规则管理")
@RestController
@RequestMapping("/rules")
@RequiredArgsConstructor
public class RuleController {

    private final IRuleService ruleService;

    /**
     * 创建考勤规则
     * 权限要求：拥有创建权限
     */
    @Operation(summary = "创建考勤规则")
    @PostMapping
    @SiaeAuthorize("hasPermission('" + Rule.CREATE + "')")
    @OperationLog(type = "CREATE_RULE", module = "RULE", description = "创建考勤规则")
    public Result<AttendanceRuleVO> createRule(@Valid @RequestBody AttendanceRuleCreateDTO dto) {
        AttendanceRuleVO result = ruleService.createRule(dto);
        return Result.success(result);
    }

    /**
     * 更新考勤规则
     * 权限要求：拥有更新权限
     */
    @Operation(summary = "更新考勤规则")
    @PutMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + Rule.UPDATE + "')")
    @OperationLog(type = "UPDATE_RULE", module = "RULE", description = "更新考勤规则")
    public Result<AttendanceRuleVO> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRuleUpdateDTO dto) {
        AttendanceRuleVO result = ruleService.updateRule(id, dto);
        return Result.success(result);
    }

    /**
     * 删除考勤规则
     * 权限要求：拥有删除权限
     */
    @Operation(summary = "删除考勤规则")
    @DeleteMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + Rule.DELETE + "')")
    @OperationLog(type = "DELETE_RULE", module = "RULE", description = "删除考勤规则")
    public Result<Boolean> deleteRule(@PathVariable Long id) {
        Boolean result = ruleService.deleteRule(id);
        return Result.success(result);
    }

    /**
     * 查询规则详情
     * 权限要求：拥有查看权限
     */
    @Operation(summary = "查询规则详情")
    @GetMapping("/{id}")
    @SiaeAuthorize("hasPermission('" + Rule.VIEW + "')")
    public Result<AttendanceRuleDetailVO> getRuleDetail(@PathVariable Long id) {
        AttendanceRuleDetailVO result = ruleService.getRuleDetail(id);
        return Result.success(result);
    }

    /**
     * 查询规则列表
     * 权限要求：拥有列表查询权限
     */
    @Operation(summary = "查询规则列表")
    @GetMapping
    @SiaeAuthorize("hasPermission('" + Rule.LIST + "')")
    public Result<List<AttendanceRuleVO>> listRules(
            @RequestParam(required = false) RuleStatus status,
            @RequestParam(required = false) String attendanceType,
            @RequestParam(required = false) String targetType) {
        List<AttendanceRuleVO> result = ruleService.listRules(status, attendanceType, targetType);
        return Result.success(result);
    }

    /**
     * 启用规则
     * 权限要求：拥有更新权限
     */
    @Operation(summary = "启用规则")
    @PostMapping("/{id}/enable")
    @SiaeAuthorize("hasPermission('" + Rule.UPDATE + "')")
    @OperationLog(type = "ENABLE_RULE", module = "RULE", description = "启用考勤规则")
    public Result<Boolean> enableRule(@PathVariable Long id) {
        Boolean result = ruleService.enableRule(id);
        return Result.success(result);
    }

    /**
     * 禁用规则
     * 权限要求：拥有更新权限
     */
    @Operation(summary = "禁用规则")
    @PostMapping("/{id}/disable")
    @SiaeAuthorize("hasPermission('" + Rule.UPDATE + "')")
    @OperationLog(type = "DISABLE_RULE", module = "RULE", description = "禁用考勤规则")
    public Result<Boolean> disableRule(@PathVariable Long id) {
        Boolean result = ruleService.disableRule(id);
        return Result.success(result);
    }
}
