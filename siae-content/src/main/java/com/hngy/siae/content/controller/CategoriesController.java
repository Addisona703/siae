package com.hngy.siae.content.controller;

import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.content.dto.request.category.CategoryDTO;
import com.hngy.siae.content.dto.request.category.CategoryEnableDTO;
import com.hngy.siae.content.dto.request.category.CategoryPageDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.service.CategoriesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 类别控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;


    @PostMapping
    public Result<CategoryVO> createCategory(@RequestBody @Validated(CreateGroup.class) CategoryDTO categoryDTO) {
        return categoriesService.createCategory(categoryDTO);
    }


    @PutMapping
    public Result<CategoryVO> updateCategory(@RequestBody @Validated(UpdateGroup.class) CategoryDTO categoryDTO) {
        return categoriesService.updateCategory(categoryDTO);
    }


    @DeleteMapping
    public Result<Void> deleteCategory(@NotNull @RequestParam Integer categoryId) {
        return categoriesService.deleteCategory(categoryId);
    }


    @GetMapping
    public Result<PageVO<CategoryVO>> listCategories(@Valid CategoryPageDTO categoryPageDTO) {
        return categoriesService.listCategories(categoryPageDTO);
    }


    @GetMapping("/{categoryId}")
    public Result<CategoryVO> queryCategory(@NotNull @PathVariable long categoryId) {
        return categoriesService.queryCategory(categoryId);
    }


    @PostMapping("/toggle-enable")
    public Result<Void> updateStatus(@RequestBody CategoryEnableDTO dto) {
        return categoriesService.updateStatus(dto.getId(), dto.getEnable());
    }
}