package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.facade.ContentFacade;
import com.hngy.siae.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.ContentPermissions.*;


/**
 * 内容控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/18
 */
@Tag(name = "内容管理", description = "内容的发布、编辑、删除和查询等操作")
@RestController
@RequestMapping("/")
@Validated
@RequiredArgsConstructor
public class ContentController {

    private final ContentFacade contentFacade;
    private final ContentService contentService;


    /**
     * 发布内容
     *
     * @param contentDTO 内容发布请求DTO
     * @return 发布的内容详情
     */
    @Operation(summary = "发布内容", description = "创建并发布新的内容，支持文章、笔记、提问、文件、视频等多种类型")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "发布成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ContentVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要内容发布权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/")
    @PreAuthorize("hasAuthority('" + SYSTEM_CONTENT_PUBLISH + "')")
    public Result<ContentVO<ContentDetailVO>> publishContent(
            @Parameter(description = "内容发布请求数据，包含标题、类型、描述、分类等信息", required = true)
            @RequestBody @Validated(CreateGroup.class) ContentDTO contentDTO) {
        return contentFacade.publishContent(contentDTO);
    }


    /**
     * 编辑内容
     *
     * @param contentDTO 内容编辑请求DTO
     * @return 编辑后的内容详情
     */
    @Operation(summary = "编辑内容", description = "修改已存在的内容信息，包括标题、描述、分类等，需要提供内容ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "编辑成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ContentVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或内容ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要内容编辑权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "内容不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/")
    @PreAuthorize("hasAuthority('" + SYSTEM_CONTENT_EDIT + "')")
    public Result<ContentVO<ContentDetailVO>> editContent(
            @Parameter(description = "内容编辑请求数据，必须包含内容ID和要修改的字段", required = true)
            @RequestBody @Validated(UpdateGroup.class) ContentDTO contentDTO) {
        return contentFacade.editContent(contentDTO);
    }


    /**
     * 删除内容
     *
     * @param id 内容ID
     * @param isTrash 是否移至垃圾箱（0-永久删除，1-移至垃圾箱）
     * @return 删除结果
     */
    @Operation(summary = "删除内容", description = "删除指定的内容，可选择永久删除或移至垃圾箱")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误，ID或isTrash参数无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要内容删除权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "内容不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/")
    @PreAuthorize("hasAuthority('" + SYSTEM_CONTENT_DELETE + "')")
    public Result<Void> deleteContent(
            @Parameter(description = "内容ID", required = true, example = "1")
            @NotNull @RequestParam Integer id,
            @Parameter(description = "是否移至垃圾箱，0表示永久删除，1表示移至垃圾箱", required = true, example = "1")
            @NotNull @RequestParam Integer isTrash) {
        return contentService.deleteContent(id, isTrash);
    }


    /**
     * 查询内容详情
     *
     * @param contentId 内容ID
     * @return 内容详情信息
     */
    @Operation(summary = "查询内容详情", description = "根据内容ID获取内容的详细信息，包括基本信息和具体内容详情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ContentVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，内容ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要内容查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "内容不存在或已被删除",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/query/{contentId}")
    @PreAuthorize("hasAuthority('" + SYSTEM_CONTENT_QUERY + "')")
    public Result<ContentVO<ContentDetailVO>> queryContent(
            @Parameter(description = "内容ID，用于唯一标识要查询的内容", required = true, example = "123456")
            @NotNull @PathVariable("contentId") Long contentId) {
        return contentFacade.queryContent(contentId);
    }


//    @GetMapping("/")
//    public Result<PageVO<ContentVO<EmptyDetailVO>>> queryContentList(
//            @RequestBody @Valid PageDTO<ContentPageDTO> contentPageDTO) {
//        return contentService.getContentPage(contentPageDTO);
//    }


//    @GetMapping("/hot")
//    public Result<List<HotContentVO>> queryHotContent(
//            @Valid ContentHotPageDTO contentHotPageDTO) {
//        return contentFacade.queryHotContent(contentHotPageDTO);
//    }
}