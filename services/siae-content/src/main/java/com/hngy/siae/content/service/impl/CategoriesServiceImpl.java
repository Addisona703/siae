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
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.content.enums.status.CategoryStatusEnum;
import com.hngy.siae.content.dto.request.category.CategoryCreateDTO;
import com.hngy.siae.content.dto.request.category.CategoryUpdateDTO;
import com.hngy.siae.content.dto.request.category.CategoryQueryDTO;
import com.hngy.siae.content.dto.response.category.CategoryVO;
import com.hngy.siae.content.entity.Category;
import com.hngy.siae.content.mapper.CategoryMapper;
import com.hngy.siae.content.service.CategoriesService;
import com.hngy.siae.core.utils.PageConvertUtil;
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
        AssertUtils.isFalse(exists, ContentResultCodeEnum.CATEGORY_NAME_EXISTS);

        // 插入分类
        Category category = BeanConvertUtil.to(categoryCreateDTO, Category.class);
        AssertUtils.isTrue(baseMapper.insert(category) > 0, ContentResultCodeEnum.CATEGORY_CREATE_FAILED);

        // 返回VO对象
        return BeanConvertUtil.to(category, CategoryVO.class);
    }


    @Override
    public CategoryVO updateCategory(CategoryUpdateDTO categoryUpdateDTO) {
        // 查询分类是否存在
        Category existingCategory = this.getById(categoryUpdateDTO.getId());
        AssertUtils.notNull(existingCategory, ContentResultCodeEnum.CATEGORY_NOT_FOUND);

        // 只有当名称或编码发生变化时才检查重复
        if (categoryUpdateDTO.getName() != null && !categoryUpdateDTO.getName().equals(existingCategory.getName())) {
            boolean nameExists = lambdaQuery()
                    .eq(Category::getName, categoryUpdateDTO.getName())
                    .ne(Category::getId, categoryUpdateDTO.getId())
                    .exists();
            AssertUtils.isFalse(nameExists, ContentResultCodeEnum.CATEGORY_NAME_OR_CODE_EXISTS);
        }

        if (categoryUpdateDTO.getCode() != null && !categoryUpdateDTO.getCode().equals(existingCategory.getCode())) {
            boolean codeExists = lambdaQuery()
                    .eq(Category::getCode, categoryUpdateDTO.getCode())
                    .ne(Category::getId, categoryUpdateDTO.getId())
                    .exists();
            AssertUtils.isFalse(codeExists, ContentResultCodeEnum.CATEGORY_NAME_OR_CODE_EXISTS);
        }

        // 存在则更新分类
        Category category = BeanConvertUtil.to(categoryUpdateDTO, Category.class);
        AssertUtils.isTrue(this.updateById(category), ContentResultCodeEnum.CATEGORY_UPDATE_FAILED);

        // 返回VO对象
        return BeanConvertUtil.to(category, CategoryVO.class);
    }


    @Override
    public void deleteCategory(Integer categoryId) {
        // 查询分类是否存在
        Category existingCategory = this.getById(categoryId);
        AssertUtils.notNull(existingCategory, ContentResultCodeEnum.CATEGORY_NOT_FOUND);

        // 检查是否有子分类
        boolean hasChildren = lambdaQuery()
                .eq(Category::getParentId, categoryId)
                .ne(Category::getStatus, CategoryStatusEnum.DELETED)
                .exists();
        AssertUtils.isFalse(hasChildren, ContentResultCodeEnum.CATEGORY_HAS_CHILDREN);

        // 逻辑删除
        Category category = new Category();
        category.setId(existingCategory.getId());
        category.setStatus(CategoryStatusEnum.DELETED);
        AssertUtils.isTrue(this.updateById(category), ContentResultCodeEnum.CATEGORY_DELETE_FAILED);
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
            // 关键词搜索（模糊匹配名称或编码）
            if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
                queryWrapper.and(wrapper ->
                        wrapper.like(Category::getName, queryDTO.getKeyword())
                                .or()
                                .like(Category::getCode, queryDTO.getKeyword())
                );
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
                , ContentResultCodeEnum.CATEGORY_NOT_FOUND);

        return BeanConvertUtil.to(category, CategoryVO.class);
    }

    @Override
    public void updateStatus(long categoryId, boolean enable) {
        Category category = this.getById(categoryId);
        AssertUtils.notNull(category, ContentResultCodeEnum.CATEGORY_NOT_FOUND);

        // 如果已是目标状态，不进行更新
        CategoryStatusEnum targetStatus = enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED;
        if (category.getStatus().equals(targetStatus)) {
            return; // 分类状态已是目标状态
        }

        category.setStatus(enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED);
        AssertUtils.isTrue(this.updateById(category), ContentResultCodeEnum.CATEGORY_UPDATE_STATUS_FAILED);
    }
}