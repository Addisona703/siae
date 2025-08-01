package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.category.CategoryCreateDTO;
import com.hngy.siae.content.dto.request.category.CategoryUpdateDTO;
import com.hngy.siae.content.dto.request.category.CategoryQueryDTO;
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
     * @param categoryCreateDTO 分类创建参数
     * @return 创建的分类信息
     */
    CategoryVO createCategory(CategoryCreateDTO categoryCreateDTO);

    /**
     * 更新分类
     *
     * @param categoryUpdateDTO 分类更新参数
     * @return 更新后的分类信息
     */
    CategoryVO updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 删除分类
     *
     * @param categoryId 分类ID
     */
    void deleteCategory(Integer categoryId);

    /**
     * 分页查询分类列表
     *
     * @param pageDTO 分页查询参数，包含查询条件
     * @return 分类分页列表
     */
    PageVO<CategoryVO> listCategories(PageDTO<CategoryQueryDTO> pageDTO);

    /**
     * 查询分类详情
     *
     * @param categoryId 分类ID
     * @return 分类详情信息
     */
    CategoryVO queryCategory(long categoryId);

    /**
     * 更新分类状态
     *
     * @param categoryId 分类ID
     * @param enable     是否启用
     */
    void updateStatus(long categoryId, boolean enable);
}