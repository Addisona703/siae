package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.dto.response.content.HotContentVO;
import com.hngy.siae.content.facade.ContentReadFacade;
import com.hngy.siae.content.facade.ContentWriteFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.content.permissions.ContentPermissions.*;


/**
 * 内容控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/18
 */
@Tag(name = "内容管理", description = "内容的发布、编辑、删除和查询等操作")
@RestController
@RequestMapping
@Validated
@RequiredArgsConstructor
public class ContentController {

    private final ContentWriteFacade contentWriteFacade;
    private final ContentReadFacade contentReadFacade;


    @Operation(summary = "发布内容", description = "创建并发布新的内容，支持文章、笔记、提问、文件、视频等多种类型")
    @PostMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_PUBLISH + "')")
    public Result<ContentVO<ContentDetailVO>> createContent(
            @Parameter(description = "内容发布请求数据，包含标题、类型、描述、分类等信息", required = true)
            @Valid @RequestBody ContentCreateDTO contentCreateDTO) {
        return Result.success(contentWriteFacade.publishContent(contentCreateDTO));
    }


    @Operation(summary = "编辑内容", description = "修改已存在的内容信息，包括标题、描述、分类等，需要提供内容ID")
    @PutMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_EDIT + "')")
    public Result<ContentVO<ContentDetailVO>> updateContent(
            @Parameter(description = "内容编辑请求数据，必须包含内容ID和要修改的字段", required = true)
            @Valid @RequestBody ContentUpdateDTO contentUpdateDTO) {
        return Result.success(contentWriteFacade.editContent(contentUpdateDTO));
    }


    @Operation(summary = "删除内容", description = "删除指定的内容，可选择永久删除或移至垃圾箱")
    @DeleteMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_DELETE + "')")
    public Result<Void> deleteContent(
            @Parameter(description = "内容ID", required = true, example = "1")
            @NotNull @RequestParam Integer id,
            @Parameter(description = "是否移至垃圾箱，0表示永久删除，1表示移至垃圾箱", required = true, example = "1")
            @NotNull @RequestParam Integer isTrash) {
        contentWriteFacade.deleteContent(id.longValue(), isTrash);
        return Result.success();
    }


    @Operation(summary = "恢复内容", description = "从回收站恢复已删除的内容")
    @PostMapping("/restore/{id}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_EDIT + "')")
    public Result<Void> restoreContent(
            @Parameter(description = "内容ID", required = true, example = "1")
            @NotNull @PathVariable Long id) {
        contentWriteFacade.restoreContent(id);
        return Result.success();
    }


    @Operation(summary = "查询内容详情", description = "根据内容ID获取内容的详细信息，包括基本信息和具体内容详情")
    @GetMapping("/query/{contentId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_QUERY + "')")
    public Result<ContentVO<ContentDetailVO>> queryContent(
            @Parameter(description = "内容ID，用于唯一标识要查询的内容", required = true, example = "123456")
            @NotNull @PathVariable("contentId") Long contentId) {
        return Result.success(contentReadFacade.queryContent(contentId));
    }


    @Operation(summary = "查询内容列表", description = "查询已发布的内容 + 当前用户自己的草稿和待审核内容")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + CONTENT_LIST_VIEW + "')")
    public Result<PageVO<ContentVO<EmptyDetailVO>>> queryContentList(
            @Parameter(description = "分页查询参数", required = true)
            @RequestBody @Valid PageDTO<ContentQueryDTO> contentPageDTO) {
        return Result.success(contentReadFacade.searchContent(contentPageDTO));
    }


    @Operation(summary = "管理员查询待审核内容", description = "管理员查询所有待审核的内容（不包括草稿）")
    @PostMapping("/admin/pending")
    @SiaeAuthorize("hasAuthority('" + CONTENT_AUDIT_VIEW + "')")
    public Result<PageVO<ContentVO<EmptyDetailVO>>> queryPendingContent(
            @Parameter(description = "分页查询参数", required = true)
            @RequestBody @Valid PageDTO<ContentQueryDTO> contentPageDTO) {
        return Result.success(contentReadFacade.searchPendingContent(contentPageDTO));
    }


    @GetMapping("/hot")
    public Result<PageVO<HotContentVO>> queryHotContent(
            @Valid ContentHotPageDTO contentHotPageDTO) {
        return Result.success(contentReadFacade.queryHotContent(contentHotPageDTO));
    }
}