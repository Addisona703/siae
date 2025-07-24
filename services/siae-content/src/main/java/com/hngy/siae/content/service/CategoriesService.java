package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.category.CategoryDTO;
import com.hngy.siae.content.dto.request.category.CategoryPageDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.entity.Category;


/**
 * 分类服务
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public interface CategoriesService extends IService<Category> {

    /**
     * 创建分类
     *
     * @param categoryDTO 类别dto
     * @return {@link Result }<{@link CategoryVO }>
     */
    Result<CategoryVO> createCategory(CategoryDTO categoryDTO);

    /**
     * 更新分类
     *
     * @param categoryDTO 类别dto
     * @return {@link Result }<{@link CategoryVO }>
     */
    Result<CategoryVO> updateCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param categoryId 类别ID
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> deleteCategory(Integer categoryId);

    /**
     * 查询分类列表
     *
     * @param categoryPageDTO 类别分页dto
     * @return {@link Result }<{@link PageVO }<{@link CategoryVO }>>
     */
    Result<PageVO<CategoryVO>> listCategories(CategoryPageDTO categoryPageDTO);

    /**
     * 查询分类详情
     *
     * @param categoryId 类别ID
     * @return {@link Result }<{@link CategoryVO }>
     */
    Result<CategoryVO> queryCategory(long categoryId);

    /**
     * 更新状态
     *
     * @param categoryId 分类ID
     * @param enable     是否启用
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> updateStatus(long categoryId, boolean enable);
}