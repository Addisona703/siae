package com.hngy.siae.content.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.content.dto.request.category.CategoryDTO;
import com.hngy.siae.content.dto.request.category.CategoryEnableDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.service.CategoriesService;
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
 * 类别控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Tag(name = "分类管理", description = "内容分类的创建、编辑、删除和查询等操作")
@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;

    /**
     * 创建分类
     *
     * @param categoryDTO 分类创建请求DTO
     * @return 创建的分类信息
     */
    @Operation(summary = "创建分类", description = "创建新的内容分类")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CategoryVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或分类名称已存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要分类创建权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('" + CONTENT_CATEGORY_CREATE + "')")
    public Result<CategoryVO> createCategory(
            @Parameter(description = "分类创建请求数据，包含分类名称、编码、父分类ID等信息", required = true)
            @RequestBody @Validated(CreateGroup.class) CategoryDTO categoryDTO) {
        return categoriesService.createCategory(categoryDTO);
    }

    /**
     * 更新分类
     *
     * @param categoryDTO 分类更新请求DTO
     * @return 更新后的分类信息
     */
    @Operation(summary = "更新分类", description = "修改已存在的分类信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CategoryVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或分类ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要分类编辑权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "分类不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping
    @PreAuthorize("hasAuthority('" + CONTENT_CATEGORY_EDIT + "')")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "分类更新请求数据，必须包含分类ID和要修改的字段", required = true)
            @RequestBody @Validated(UpdateGroup.class) CategoryDTO categoryDTO) {
        return categoriesService.updateCategory(categoryDTO);
    }

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @Operation(summary = "删除分类", description = "删除指定的分类")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误，分类ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要分类删除权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "分类不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping
    @PreAuthorize("hasAuthority('" + CONTENT_CATEGORY_DELETE + "')")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true, example = "1")
            @NotNull @RequestParam Integer categoryId) {
        return categoriesService.deleteCategory(categoryId);
    }


//    @GetMapping
//    public Result<PageVO<CategoryVO>> listCategories(@Valid CategoryPageDTO categoryPageDTO) {
//        return categoriesService.listCategories(categoryPageDTO);
//    }

    /**
     * 查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情信息
     */
    @Operation(summary = "查询分类详情", description = "根据分类ID获取分类的详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CategoryVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，分类ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要分类查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "分类不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('" + CONTENT_CATEGORY_VIEW + "')")
    public Result<CategoryVO> queryCategory(
            @Parameter(description = "分类ID，用于唯一标识要查询的分类", required = true, example = "1")
            @NotNull @PathVariable long categoryId) {
        return categoriesService.queryCategory(categoryId);
    }

    /**
     * 启用/禁用分类
     *
     * @param dto 分类状态切换请求DTO
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用分类", description = "切换分类的启用状态")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "操作成功",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要分类状态切换权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "分类不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/toggle-enable")
    @PreAuthorize("hasAuthority('" + CONTENT_CATEGORY_TOGGLE + "')")
    public Result<Void> updateStatus(
            @Parameter(description = "分类状态切换请求数据，包含分类ID和启用状态", required = true)
            @RequestBody CategoryEnableDTO dto) {
        return categoriesService.updateStatus(dto.getId(), dto.getEnable());
    }
}