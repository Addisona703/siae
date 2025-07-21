package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.content.dto.request.TagDTO;
import com.hngy.siae.content.dto.response.TagVO;
import com.hngy.siae.content.service.TagsService;
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
 * 标签控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Tag(name = "标签管理", description = "内容标签的创建、编辑、删除和查询等操作")
@RestController
@RequestMapping("/tags")
@Validated
@RequiredArgsConstructor
public class  TagsController {

    private final TagsService tagsService;

    /**
     * 创建标签
     *
     * @param tagDTO 标签创建请求DTO
     * @return 创建的标签信息
     */
    @Operation(summary = "创建标签", description = "创建新的内容标签")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TagVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或标签名称已存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要标签创建权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('" + CONTENT_TAG_CREATE + "')")
    public Result<TagVO> createTag(
            @Parameter(description = "标签创建请求数据，包含标签名称、描述等信息", required = true)
            @Validated(CreateGroup.class) @RequestBody TagDTO tagDTO) {
        return tagsService.createTag(tagDTO);
    }

    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param tagDTO 标签更新请求DTO
     * @return 更新后的标签信息
     */
    @Operation(summary = "更新标签", description = "修改已存在的标签信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TagVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或标签ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要标签编辑权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "标签不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping
    @PreAuthorize("hasAuthority('" + CONTENT_TAG_EDIT + "')")
    public Result<TagVO> updateTag(
            @Parameter(description = "标签ID，可选参数", required = false, example = "1")
            @RequestParam(required = false) Long id,
            @Parameter(description = "标签更新请求数据，包含要修改的字段", required = true)
            @Valid @RequestBody TagDTO tagDTO) {
        return tagsService.updateTag(id, tagDTO);
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除结果
     */
    @Operation(summary = "删除标签", description = "删除指定的标签")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误，标签ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要标签删除权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "标签不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping
    @PreAuthorize("hasAuthority('" + CONTENT_TAG_DELETE + "')")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID", required = true, example = "1")
            @NotNull @RequestParam Long id) {
        return tagsService.deleteTag(id);
    }


//    @GetMapping
//    public Result<PageVO<TagVO>> listTags(PageDTO pageDTO) {
//        return tagsService.listTags(pageDTO);
//    }
}