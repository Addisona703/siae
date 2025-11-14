package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.detail.EmptyDetailVO;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.facade.ContentFacade;
import com.hngy.siae.content.service.ContentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * @param contentCreateDTO 内容发布请求DTO
     * @return 发布的内容详情
     */
    @Operation(summary = "发布内容", description = "创建并发布新的内容，支持文章、笔记、提问、文件、视频等多种类型")
    @PostMapping("/")
    @SiaeAuthorize("hasAuthority('" + CONTENT_PUBLISH + "')")
    public Result<ContentVO<ContentDetailVO>> publishContent(
            @Parameter(description = "内容发布请求数据，包含标题、类型、描述、分类等信息", required = true)
            @Valid @RequestBody ContentCreateDTO contentCreateDTO) {
        return Result.success(contentFacade.publishContent(contentCreateDTO));
    }


    /**
     * 编辑内容
     *
     * @param contentUpdateDTO 内容编辑请求DTO
     * @return 编辑后的内容详情
     */
    @Operation(summary = "编辑内容", description = "修改已存在的内容信息，包括标题、描述、分类等，需要提供内容ID")
    @PutMapping("/")
    @SiaeAuthorize("hasAuthority('" + CONTENT_EDIT + "')")
    public Result<ContentVO<ContentDetailVO>> editContent(
            @Parameter(description = "内容编辑请求数据，必须包含内容ID和要修改的字段", required = true)
            @Valid @RequestBody ContentUpdateDTO contentUpdateDTO) {
        return Result.success(contentFacade.editContent(contentUpdateDTO));
    }


    /**
     * 删除内容
     * 权限说明：非管理员只能删除自己创建的内容
     *
     * @param id 内容ID
     * @param isTrash 是否移至垃圾箱（0-永久删除，1-移至垃圾箱）
     * @return 删除结果
     */
    @Operation(summary = "删除内容", description = "删除指定的内容，可选择永久删除或移至垃圾箱。非管理员只能删除自己创建的内容")
    @DeleteMapping("/")
    @SiaeAuthorize("hasAuthority('" + CONTENT_DELETE + "')")
    public Result<Void> deleteContent(
            @Parameter(description = "内容ID", required = true, example = "1")
            @NotNull @RequestParam Integer id,
            @Parameter(description = "是否移至垃圾箱，0表示永久删除，1表示移至垃圾箱", required = true, example = "1")
            @NotNull @RequestParam Integer isTrash) {
        // 从Security上下文获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 获取用户ID（从Details中获取，由ServiceAuthenticationFilter设置）
        Long currentUserId = (Long) authentication.getDetails();
        
        // 判断是否为管理员（检查是否有ROLE_ADMIN或ROLE_ROOT角色）
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_ROOT"));
        
        contentService.deleteContent(id, isTrash, currentUserId, isAdmin);
        return Result.success();
    }


    /**
     * 查询内容详情
     *
     * @param contentId 内容ID
     * @return 内容详情信息
     */
    @Operation(summary = "查询内容详情", description = "根据内容ID获取内容的详细信息，包括基本信息和具体内容详情")
    @GetMapping("/query/{contentId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_QUERY + "')")
    public Result<ContentVO<ContentDetailVO>> queryContent(
            @Parameter(description = "内容ID，用于唯一标识要查询的内容", required = true, example = "123456")
            @NotNull @PathVariable("contentId") Long contentId) {
        return Result.success(contentFacade.queryContent(contentId));
    }


    @Operation(summary = "查询内容列表", description = "分页查询内容列表，支持按分类、标签、状态等条件筛选")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + CONTENT_LIST_VIEW + "')")
    public Result<PageVO<ContentVO<EmptyDetailVO>>> queryContentList(
            @Parameter(description = "分页查询参数", required = true)
            @RequestBody @Valid PageDTO<ContentPageDTO> contentPageDTO) {
        return Result.success(contentService.getContentPage(contentPageDTO));
    }


    @GetMapping("/hot")
    public Result<PageVO<HotContentVO>> queryHotContent(
            @Valid ContentHotPageDTO contentHotPageDTO) {
        return Result.success(contentFacade.queryHotContent(contentHotPageDTO));
    }
}