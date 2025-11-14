package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.request.AuditQueryDTO;
import com.hngy.siae.content.dto.response.AuditVO;
import com.hngy.siae.content.service.AuditsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
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
     * 权限说明：需要审核处理权限，且必须是管理员
     *
     * @param id 审核记录ID
     * @param auditDTO 审核处理请求DTO
     * @return 处理结果
     */
    @Operation(summary = "处理审核", description = "处理内容审核，包括审核通过、拒绝等操作")
    @PutMapping("/{id}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_AUDIT_HANDLE + "')")
    public Result<Void> handleAudit(
            @Parameter(description = "审核记录ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "审核处理请求数据，包含审核结果、审核意见等", required = true)
            @Valid @RequestBody AuditDTO auditDTO) {
        auditsService.handleAudit(id, auditDTO);
        return Result.success();
    }


    /**
     * 获取审核列表
     * 权限说明：需要审核查看权限，且必须是管理员
     *
     * @param pageDTO 分页查询参数
     * @return 审核列表
     */
    @Operation(summary = "获取审核列表", description = "分页获取审核列表，支持按目标类型和审核状态筛选")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + CONTENT_AUDIT_VIEW + "')")
    public Result<PageVO<AuditVO>> getAuditPage(
            @Parameter(description = "分页查询参数", required = true)
            @Valid @RequestBody PageDTO<AuditQueryDTO> pageDTO) {
        return Result.success(auditsService.getAuditPage(pageDTO));
    }

    /**
     * 获取审核记录
     * 权限说明：需要审核查看权限，非管理员只能查看自己的审核记录
     *
     * @param queryDTO 查询参数
     * @return 审核记录信息
     */
    @Operation(summary = "获取审核记录", description = "根据目标对象ID和类型获取审核记录详情")
    @PostMapping("/record")
    @SiaeAuthorize("hasAuthority('" + CONTENT_AUDIT_VIEW + "')")
    public Result<AuditVO> getAuditRecord(
            @Parameter(description = "查询参数，包含目标对象ID和类型", required = true)
            @Valid @RequestBody AuditQueryDTO queryDTO) {
        return Result.success(auditsService.getAuditRecord(queryDTO));
    }
}
