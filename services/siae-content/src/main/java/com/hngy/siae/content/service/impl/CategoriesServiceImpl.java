package com.hngy.siae.content.service.impl;

import com.hngy.siae.core.utils.BeanConvertUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.common.enums.status.CategoryStatusEnum;
import com.hngy.siae.content.dto.request.category.CategoryCreateDTO;
import com.hngy.siae.content.dto.request.category.CategoryUpdateDTO;
import com.hngy.siae.content.dto.request.category.CategoryQueryDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.entity.Category;
import com.hngy.siae.content.mapper.CategoryMapper;
import com.hngy.siae.content.service.CategoriesService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * 分类服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */

@Service
@RequiredArgsConstructor
public class CategoriesServiceImpl
        extends ServiceImpl<CategoryMapper, Category>
        implements CategoriesService {


    @Override
    public CategoryVO createCategory(CategoryCreateDTO categoryCreateDTO) {
        // 判断是否已存在同名分类
        boolean exists = lambdaQuery()
                .eq(Category::getName, categoryCreateDTO.getName())
                .or()
                .eq(Category::getCode, categoryCreateDTO.getCode())
                .exists();
        AssertUtils.isFalse(exists, "分类名称已存在");

        // 插入分类
        Category category = BeanConvertUtil.to(categoryCreateDTO, Category.class);
        AssertUtils.isTrue(baseMapper.insert(category) > 0, "新增分类失败");

        // 返回VO对象
        return BeanConvertUtil.to(category, CategoryVO.class);
    }


    @Override
    public CategoryVO updateCategory(CategoryUpdateDTO categoryUpdateDTO) {
        // 查询分类是否存在
        Category existingCategory = this.getById(categoryUpdateDTO.getId());
        AssertUtils.notNull(existingCategory, "分类不存在");

        // 检查名称是否重复（排除自身）
        boolean exists = lambdaQuery()
                .eq(Category::getName, categoryUpdateDTO.getName())
                .or()
                .eq(Category::getCode, categoryUpdateDTO.getCode())
                .ne(Category::getId, categoryUpdateDTO.getId())
                .exists();
        AssertUtils.isFalse(exists, "分类名称或编码已存在");

        // 存在则更新分类
        Category category = BeanConvertUtil.to(categoryUpdateDTO, Category.class);
        AssertUtils.isTrue(this.updateById(category), "更新分类失败");

        // 返回VO对象
        return BeanConvertUtil.to(category, CategoryVO.class);
    }


    @Override
    public void deleteCategory(Integer categoryId) {
        // 查询分类是否存在
        Category existingCategory = this.getById(categoryId);
        AssertUtils.notNull(existingCategory, "分类不存在");

        // 检查是否有子分类
        boolean hasChildren = lambdaQuery()
                .eq(Category::getParentId, categoryId)
                .ne(Category::getStatus, CategoryStatusEnum.DELETED)
                .exists();
        AssertUtils.isFalse(hasChildren, "该分类下存在子分类，无法删除");

        // 逻辑删除
        Category category = new Category();
        category.setId(existingCategory.getId());
        category.setStatus(CategoryStatusEnum.DELETED);
        AssertUtils.isTrue(this.updateById(category), "删除分类失败");
    }


    @Override
    public PageVO<CategoryVO> listCategories(PageDTO<CategoryQueryDTO> pageDTO) {
        // 构建分页对象
        Page<Category> page = PageConvertUtil.toPage(pageDTO);

        // 获取查询条件
        CategoryQueryDTO queryDTO = pageDTO.getParams();

        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Category::getStatus, CategoryStatusEnum.DELETED);

        if (queryDTO != null) {
            // 关键词搜索（搜索名称或编码）
            if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
                queryWrapper.and(wrapper ->
                        wrapper.like(Category::getName, queryDTO.getKeyword())
                                .or()
                                .like(Category::getCode, queryDTO.getKeyword())
                );
            }

            // 精确匹配分类名称
            if (StrUtil.isNotBlank(queryDTO.getName())) {
                queryWrapper.eq(Category::getName, queryDTO.getName());
            }

            // 精确匹配分类编码
            if (StrUtil.isNotBlank(queryDTO.getCode())) {
                queryWrapper.eq(Category::getCode, queryDTO.getCode());
            }

            // 分类状态
            if (ObjectUtil.isNotNull(queryDTO.getStatus())) {
                queryWrapper.eq(Category::getStatus, queryDTO.getStatus());
            }

            // 父分类ID
            if (ObjectUtil.isNotNull(queryDTO.getParentId())) {
                queryWrapper.eq(Category::getParentId, queryDTO.getParentId());
            }

            // 创建时间范围查询
            if (StrUtil.isNotBlank(queryDTO.getCreatedAtStart())) {
                queryWrapper.ge(Category::getCreateTime, queryDTO.getCreatedAtStart());
            }
            if (StrUtil.isNotBlank(queryDTO.getCreatedAtEnd())) {
                queryWrapper.le(Category::getCreateTime, queryDTO.getCreatedAtEnd());
            }
        }

        // 兼容原有的keyword查询（从pageDTO中获取）
        String keyword = pageDTO.getKeyword();
        if (StrUtil.isNotBlank(keyword) && (queryDTO == null || StrUtil.isBlank(queryDTO.getKeyword()))) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Category::getName, keyword)
                            .or()
                            .like(Category::getCode, keyword)
            );
        }

        // 执行分页查询
        Page<Category> result = baseMapper.selectPage(page, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果并返回
        return PageConvertUtil.convert(result, CategoryVO.class);
    }


    @Override
    public CategoryVO queryCategory(long categoryId) {
        Category category = this.getById(categoryId);
        AssertUtils.isFalse(category == null
                || category.getStatus() == CategoryStatusEnum.DELETED
                ,"分类不存在");

        return BeanConvertUtil.to(category, CategoryVO.class);
    }

    @Override
    public void updateStatus(long categoryId, boolean enable) {
        Category category = this.getById(categoryId);
        AssertUtils.notNull(category, "该分类不存在");

        // 如果已是目标状态，不进行更新
        CategoryStatusEnum targetStatus = enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED;
        if (category.getStatus().equals(targetStatus)) {
            return; // 分类状态已是目标状态
        }

        category.setStatus(enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED);
        AssertUtils.isTrue(this.updateById(category), "分类状态更新失败");
    }
}