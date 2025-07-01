package com.hngy.siae.content.controller;

import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.response.AuditVO;
import com.hngy.siae.content.service.AuditsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审核内容控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@RestController
@RequestMapping("/audits")
@Validated
@RequiredArgsConstructor
public class AuditsController {

    private final AuditsService auditsService;


    @PutMapping("/{id}")
    public Result<Void> handleAudit(@PathVariable Long id,
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


    @GetMapping
    public Result<AuditVO> getAuditRecord(
            @NotNull @RequestParam Long targetId,
            @NotNull @RequestParam TypeEnum targetType) {
        return auditsService.getAuditRecord(targetId, targetType);
    }
}
