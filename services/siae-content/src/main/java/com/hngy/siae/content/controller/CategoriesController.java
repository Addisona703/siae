package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.category.CategoryCreateDTO;
import com.hngy.siae.content.dto.request.category.CategoryUpdateDTO;
import com.hngy.siae.content.dto.request.category.CategoryQueryDTO;
import com.hngy.siae.content.dto.request.category.CategoryEnableDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.service.CategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.ContentPermissions.*;


/**
 * 分类管理控制器
 *
 * @author KEYKB
 */
@Tag(name = "分类管理", description = "内容分类的创建、编辑、删除和查询等操作")
@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;


    @Operation(summary = "创建分类", description = "创建新的内容分类")
    @PostMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_CREATE + "')")
    public Result<CategoryVO> createCategory(
            @Parameter(description = "分类创建请求数据，包含分类名称、编码、父分类ID等信息", required = true)
            @Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryVO categoryVO = categoriesService.createCategory(categoryCreateDTO);
        return Result.success(categoryVO);
    }


    @Operation(summary = "更新分类", description = "修改已存在的分类信息")
    @PutMapping
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_EDIT + "')")
    public Result<CategoryVO> updateCategory(
            @Parameter(description = "分类更新请求数据，必须包含分类ID和要修改的字段", required = true)
            @Valid @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        CategoryVO categoryVO = categoriesService.updateCategory(categoryUpdateDTO);
        return Result.success(categoryVO);
    }

    @Operation(summary = "删除分类", description = "删除指定的分类")
    @DeleteMapping("/{categoryId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_DELETE + "')")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true, example = "1")
            @NotNull @PathVariable Integer categoryId) {
        categoriesService.deleteCategory(categoryId);
        return Result.success();
    }


    @Operation(summary = "查询分类详情", description = "根据分类ID获取分类的详细信息")
    @GetMapping("/detail/{categoryId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_VIEW + "')")
    public Result<CategoryVO> queryCategory(
            @Parameter(description = "分类ID，用于唯一标识要查询的分类", required = true, example = "1")
            @NotNull @PathVariable long categoryId) {
        CategoryVO categoryVO = categoriesService.queryCategory(categoryId);
        return Result.success(categoryVO);
    }


    @Operation(summary = "分页查询分类列表", description = "获取分类的分页列表，支持关键词搜索和条件筛选")
    @PostMapping("/page")
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_VIEW + "')")
    public Result<PageVO<CategoryVO>> queryCategoryList(
            @Parameter(description = "分页查询参数，包含查询条件")
            @Valid @RequestBody PageDTO<CategoryQueryDTO> pageDTO) {
        PageVO<CategoryVO> pageVO = categoriesService.listCategories(pageDTO);
        return Result.success(pageVO);
    }


    @Operation(summary = "启用/禁用分类", description = "切换分类的启用状态")
    @PostMapping("/toggle-enable")
    @SiaeAuthorize("hasAuthority('" + CONTENT_CATEGORY_TOGGLE + "')")
    public Result<Void> updateStatus(
            @Parameter(description = "分类状态切换请求数据，包含分类ID和启用状态", required = true)
            @Valid @RequestBody CategoryEnableDTO dto) {
        categoriesService.updateStatus(dto.getId(), dto.getEnable());
        return Result.success();
    }
}
