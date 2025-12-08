package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.category.CategoryVO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.statistics.StatisticsVO;
import com.hngy.siae.content.dto.response.tag.TagVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.service.CategoriesService;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.content.service.StatisticsService;
import com.hngy.siae.content.service.TagsService;
import com.hngy.siae.content.facade.ContentReadFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 内容服务Feign接口控制器
 * 供其他服务内部调用，无需权限验证
 *
 * @author KEYKB
 */
@Tag(name = "内容Feign接口", description = "内容服务间调用API")
@RestController
@RequestMapping("/feign")
@Validated
@RequiredArgsConstructor
public class ContentFeignController {

    private final ContentReadFacade contentReadFacade;
    private final ContentService contentService;
    private final CategoriesService categoriesService;
    private final TagsService tagsService;
    private final StatisticsService statisticsService;

    // ==================== 内容查询接口 ====================

    /**
     * 根据内容ID查询内容详情
     * 供其他服务调用，用于获取内容基本信息
     *
     * @param contentId 内容ID
     * @return 内容详情
     */
    @Operation(summary = "查询内容详情", description = "根据内容ID获取内容详情，供Feign调用")
    @GetMapping("/content/{contentId}")
    public Result<ContentVO<ContentDetailVO>> getContentById(
            @Parameter(description = "内容ID", required = true, example = "123456")
            @NotNull @PathVariable Long contentId) {
        return Result.success(contentReadFacade.queryContent(contentId));
    }

    /**
     * 分页查询内容列表
     * 供其他服务查询内容列表
     *
     * @param contentPageDTO 分页查询参数
     * @return 内容分页列表
     */
    @Operation(summary = "分页查询内容列表", description = "分页查询内容列表，供Feign调用")
    @PostMapping("/content/page")
    public Result<PageVO<ContentVO<EmptyDetailVO>>> getContentPage(
            @Parameter(description = "分页查询参数", required = true)
            @RequestBody @Valid PageDTO<ContentQueryDTO> contentPageDTO) {
        return Result.success(contentService.getContentPage(contentPageDTO));
    }

    // ==================== 分类查询接口 ====================

    /**
     * 根据分类ID查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @Operation(summary = "查询分类详情", description = "根据分类ID获取分类详情，供Feign调用")
    @GetMapping("/category/{categoryId}")
    public Result<CategoryVO> getCategoryById(
            @Parameter(description = "分类ID", required = true, example = "1")
            @NotNull @PathVariable Long categoryId) {
        return Result.success(categoriesService.queryCategory(categoryId));
    }

    // ==================== 标签查询接口 ====================

    /**
     * 根据标签ID查询标签详情
     *
     * @param tagId 标签ID
     * @return 标签详情
     */
    @Operation(summary = "查询标签详情", description = "根据标签ID获取标签详情，供Feign调用")
    @GetMapping("/tag/{tagId}")
    public Result<TagVO> getTagById(
            @Parameter(description = "标签ID", required = true, example = "1")
            @NotNull @PathVariable Long tagId) {
        return Result.success(tagsService.getTagById(tagId));
    }

    // ==================== 统计查询接口 ====================

    /**
     * 获取内容统计信息
     *
     * @param contentId 内容ID
     * @return 统计信息
     */
    @Operation(summary = "获取内容统计信息", description = "获取内容的统计数据，供Feign调用")
    @GetMapping("/statistics/{contentId}")
    public Result<StatisticsVO> getStatistics(
            @Parameter(description = "内容ID", required = true, example = "123456")
            @NotNull @PathVariable Long contentId) {
        return Result.success(statisticsService.getStatistics(contentId));
    }
}
