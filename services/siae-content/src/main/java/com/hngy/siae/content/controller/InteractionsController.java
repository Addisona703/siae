package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.interaction.ActionDTO;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.service.InteractionsService;
import com.hngy.siae.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 内容互动管理控制器
 *
 * @author KEYKB
 */
@Tag(name = "用户交互", description = "用户与内容的交互操作，如点赞、收藏、浏览等")
@RestController
@RequestMapping("/interactions")
@Validated
@RequiredArgsConstructor
public class InteractionsController {

    private final InteractionsService interactionsService;
    private final SecurityUtil securityUtil;


    @Operation(summary = "记录用户行为", description = "记录用户对内容的交互行为，如点赞、收藏、浏览等")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/action")
    public Result<Void> recordAction(
            @Parameter(description = "用户行为请求数据，包含目标ID、行为类型等", required = true)
            @Valid @RequestBody ActionDTO actionDTO) {
        actionDTO.setUserId(securityUtil.getCurrentUserId());
        interactionsService.recordAction(actionDTO);
        return Result.success();
    }


    @Operation(summary = "取消用户行为", description = "取消用户对内容的交互行为，如取消点赞、取消收藏等")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/action")
    public Result<Void> cancelAction(
            @Parameter(description = "用户行为请求数据，包含目标ID、行为类型等", required = true)
            @Valid @RequestBody ActionDTO actionDTO) {
        actionDTO.setUserId(securityUtil.getCurrentUserId());
        interactionsService.cancelAction(actionDTO);
        return Result.success();
    }


    @Operation(summary = "批量查询用户点赞状态", description = "查询当前用户对指定目标的点赞状态")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/liked")
    public Result<Set<Long>> getLikedIds(
            @Parameter(description = "目标ID列表", required = true)
            @RequestBody List<Long> targetIds,
            @Parameter(description = "目标类型：CONTENT或COMMENT", required = true)
            @RequestParam TypeEnum targetType) {
        Long userId = securityUtil.getCurrentUserId();
        Set<Long> likedIds = interactionsService.getActivatedTargetIds(userId, targetIds, targetType, ActionTypeEnum.LIKE);
        return Result.success(likedIds);
    }
}