package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.ActionDTO;
import com.hngy.siae.content.service.InteractionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.ContentPermissions.*;

/**
 * 内容互动管理控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/21
 */
@Tag(name = "用户交互", description = "用户与内容的交互操作，如点赞、收藏、浏览等")
@RestController
@RequestMapping("/interactions")
@Validated
@RequiredArgsConstructor
public class InteractionsController {

    private final InteractionsService interactionsService;

    /**
     * 记录用户行为
     *
     * @param actionDTO 用户行为请求DTO
     * @return 操作结果
     */
    @Operation(summary = "记录用户行为", description = "记录用户对内容的交互行为，如点赞、收藏、浏览等")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "记录成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要交互记录权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/action")
    @PreAuthorize("hasAuthority('" + CONTENT_INTERACTION_RECORD + "')")
    public Result<Void> recordAction(
            @Parameter(description = "用户行为请求数据，包含用户ID、目标ID、行为类型等", required = true)
            @Valid @RequestBody ActionDTO actionDTO) {
        return interactionsService.recordAction(actionDTO);
    }

    /**
     * 取消用户行为
     *
     * @param actionDTO 用户行为请求DTO
     * @return 操作结果
     */
    @Operation(summary = "取消用户行为", description = "取消用户对内容的交互行为，如取消点赞、取消收藏等")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取消成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要交互取消权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "行为记录不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/action")
    @PreAuthorize("hasAuthority('" + CONTENT_INTERACTION_CANCEL + "')")
    public Result<Void> cancelAction(
            @Parameter(description = "用户行为请求数据，包含用户ID、目标ID、行为类型等", required = true)
            @Valid @RequestBody ActionDTO actionDTO) {
        return interactionsService.cancelAction(actionDTO);
    }
}