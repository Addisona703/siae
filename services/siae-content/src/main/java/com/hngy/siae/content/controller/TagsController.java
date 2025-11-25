package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.TagCreateDTO;
import com.hngy.siae.content.dto.request.TagUpdateDTO;
import com.hngy.siae.content.dto.request.TagQueryDTO;
import com.hngy.siae.content.dto.response.TagVO;
import com.hngy.siae.content.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.ContentPermissions.*;


/**
 * 标签控制器
 *
 * @author KEYKB
 */
@Slf4j
@Tag(name = "标签管理", description = "内容标签的创建、编辑、删除和查询等操作")
@RestController
@RequestMapping("/tags")
@Validated
@RequiredArgsConstructor
public class TagsController {

    private final TagsService tagsService;


    @Operation(summary = "创建标签", description = "创建新的内容标签")
    @PostMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_TAG_CREATE + "')")
    public Result<TagVO> createTag(
            @Parameter(description = "标签创建请求数据，包含标签名称、描述等信息", required = true)
            @Valid @RequestBody TagCreateDTO tagCreateDTO) {
        TagVO tagVO = tagsService.createTag(tagCreateDTO);
        return Result.success(tagVO);
    }


    @Operation(summary = "更新标签", description = "修改已存在的标签信息")
    @PutMapping("/{id}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_TAG_EDIT + "')")
    public Result<TagVO> updateTag(
            @Parameter(description = "标签ID", required = true, example = "1")
            @PathVariable("id") @NotNull Long id,
            @Parameter(description = "标签更新请求数据，包含要修改的字段", required = true)
            @Valid @RequestBody TagUpdateDTO tagUpdateDTO) {
        TagVO tagVO = tagsService.updateTag(id, tagUpdateDTO);
        return Result.success(tagVO);
    }


    @Operation(summary = "删除标签", description = "删除指定的标签")
    @DeleteMapping("/{id}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_TAG_DELETE + "')")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID", required = true, example = "1")
            @PathVariable("id") @NotNull Long id) {
        tagsService.deleteTag(id);
        return Result.success();
    }


    @Operation(summary = "分页查询标签列表", description = "获取标签的分页列表，支持关键词搜索和条件筛选")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + CONTENT_TAG_VIEW + "')")
    public Result<PageVO<TagVO>> listTags(
            @Parameter(description = "分页查询参数，包含查询条件") @Valid @RequestBody PageDTO<TagQueryDTO> pageDTO) {
        PageVO<TagVO> pageVO = tagsService.listTags(pageDTO);
        return Result.success(pageVO);
    }
}