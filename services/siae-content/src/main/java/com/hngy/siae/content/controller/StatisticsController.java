package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.dto.response.StatisticsVO;
import com.hngy.siae.content.service.StatisticsService;
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
 * 内容统计管理控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/21
 */
@Tag(name = "统计管理", description = "内容统计数据的查询和更新操作")
@RestController
@RequestMapping("/statistics")
@Validated
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取内容统计信息
     *
     * @param contentId 内容ID
     * @return 内容统计信息
     */
    @Operation(summary = "获取内容统计信息", description = "根据内容ID获取统计数据，包括浏览量、点赞数、收藏数、评论数等")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = StatisticsVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，内容ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要统计查看权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "内容不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{contentId}")
    @PreAuthorize("hasAuthority('" + CONTENT_STATISTICS_VIEW + "')")
    public Result<StatisticsVO> getStatistics(
            @Parameter(description = "内容ID，用于唯一标识要查询统计信息的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId) {
        return statisticsService.getStatistics(contentId);
    }

    /**
     * 更新内容统计信息
     *
     * @param contentId 内容ID
     * @param statisticsDTO 统计信息更新请求DTO
     * @return 更新后的统计信息
     */
    @Operation(summary = "更新内容统计信息", description = "更新指定内容的统计数据")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = StatisticsVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要统计更新权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "内容不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{contentId}")
    @PreAuthorize("hasAuthority('" + CONTENT_STATISTICS_UPDATE + "')")
    public Result<StatisticsVO> updateStatistics(
            @Parameter(description = "内容ID，用于唯一标识要更新统计信息的内容", required = true, example = "123456")
            @NotNull @PathVariable Long contentId,
            @Parameter(description = "统计信息更新请求数据，包含要更新的统计字段", required = true)
            @Valid @RequestBody StatisticsDTO statisticsDTO) {
        return statisticsService.updateStatistics(contentId, statisticsDTO);
    }
}