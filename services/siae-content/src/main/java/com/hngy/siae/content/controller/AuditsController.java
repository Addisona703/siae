package com.hngy.siae.content.controller;

import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.response.AuditVO;
import com.hngy.siae.content.service.AuditsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.ContentPermissions.*;

/**
 * 审核内容控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Tag(name = "审核管理", description = "内容审核的处理和查询操作")
@RestController
@RequestMapping("/audits")
@Validated
@RequiredArgsConstructor
public class AuditsController {

    private final AuditsService auditsService;

    /**
     * 处理审核
     *
     * @param id 审核记录ID
     * @param auditDTO 审核处理请求DTO
     * @return 处理结果
     */
    @Operation(summary = "处理审核", description = "处理内容审核，包括审核通过、拒绝等操作")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "处理成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要审核处理权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "审核记录不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + CONTENT_AUDIT_HANDLE + "')")
    public Result<Void> handleAudit(
            @Parameter(description = "审核记录ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "审核处理请求数据，包含审核结果、审核意见等", required = true)
            @Valid @RequestBody AuditDTO auditDTO) {
        return auditsService.handleAudit(id, auditDTO);
    }


    @GetMapping("/pending")
    public Result<PageVO<AuditVO>> getPendingAudits(
            @NotNull @RequestParam Integer page,
            @NotNull @RequestParam Integer pageSize,
            @RequestParam(required = false) TypeEnum targetType,
            @RequestParam(required = false) AuditStatusEnum auditStatus) {
        return auditsService.getAuditPage(page, pageSize, targetType, auditStatus);
    }

    /**
     * 获取审核记录
     *
     * @param targetId 目标对象ID
     * @param targetType 目标对象类型
     * @return 审核记录信息
     */
    @Operation(summary = "获取审核记录", description = "根据目标对象ID和类型获取审核记录详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AuditVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要审核查看权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "审核记录不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('" + CONTENT_AUDIT_VIEW + "')")
    public Result<AuditVO> getAuditRecord(
            @Parameter(description = "目标对象ID，如内容ID", required = true, example = "123456")
            @NotNull @RequestParam Long targetId,
            @Parameter(description = "目标对象类型，如CONTENT、COMMENT等", required = true)
            @NotNull @RequestParam TypeEnum targetType) {
        return auditsService.getAuditRecord(targetId, targetType);
    }
}
