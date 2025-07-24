package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.utils.PageConvertUtil;
import com.hngy.siae.content.common.enums.status.CategoryStatusEnum;
import com.hngy.siae.content.dto.request.category.CategoryDTO;
import com.hngy.siae.content.dto.request.category.CategoryPageDTO;
import com.hngy.siae.content.dto.response.CategoryVO;
import com.hngy.siae.content.entity.Category;
import com.hngy.siae.content.mapper.CategoryMapper;
import com.hngy.siae.content.service.CategoriesService;
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
    public Result<CategoryVO> createCategory(CategoryDTO categoryDTO) {
        // 判断是否已存在同名分类
        boolean exists = lambdaQuery()
                .eq(Category::getName, categoryDTO.getName())
                .or()
                .eq(Category::getCode, categoryDTO.getCode())
                .exists();
        AssertUtils.isFalse(exists, "分类名称已存在");

        // 插入分类
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        AssertUtils.isTrue(baseMapper.insert(category) > 0, "新增分类失败");

        // 封装返回
        CategoryVO categoryVO = BeanUtil.copyProperties(category, CategoryVO.class);
        return Result.success(categoryVO);
    }


    @Override
    public Result<CategoryVO> updateCategory(CategoryDTO categoryDTO) {
        // 查询分类是否存在
        Category existingCategory = this.getById(categoryDTO.getId());
        AssertUtils.notNull(existingCategory, "分类不存在");

        // 检查名称是否重复（排除自身）
        boolean exists = lambdaQuery()
                .eq(Category::getName, categoryDTO.getName())
                .or()
                .eq(Category::getCode, categoryDTO.getCode())
                .ne(Category::getId, categoryDTO.getId())
                .exists();
        AssertUtils.isFalse(exists, "分类名称或编码已存在");

        // 存在则更新分类
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        AssertUtils.isTrue(this.updateById(category), "更新分类失败");

        // 转换成VO返回
        CategoryVO categoryVO = BeanUtil.copyProperties(category, CategoryVO.class);
        return Result.success(categoryVO);
    }


    @Override
    public Result<Void> deleteCategory(Integer categoryId) {
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

        return Result.success();
    }


    @Override
    public Result<PageVO<CategoryVO>> listCategories(CategoryPageDTO categoryPageDTO) {
        // 构建分页对象
        Page<Category> page = categoryPageDTO.<Category>toPage();

        // 构建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotNull(categoryPageDTO.getStatus()), Category::getStatus, categoryPageDTO.getStatus())
                .eq(ObjectUtil.isNotNull(categoryPageDTO.getParentId()), Category::getParentId, categoryPageDTO.getParentId())
                .ne(Category::getStatus, CategoryStatusEnum.DELETED);

        // 模糊查询 name
        if (StrUtil.isNotBlank(categoryPageDTO.getKeyword())) {
            queryWrapper.and(wrapper ->
                    wrapper.like(Category::getName, categoryPageDTO.getKeyword()));
        }

        // 执行分页查询
        Page<Category> result = baseMapper.selectPage(page, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果
        PageVO<CategoryVO> pageVO = PageConvertUtil.convert(result, CategoryVO.class);

        return Result.success(pageVO);
    }


    @Override
    public Result<CategoryVO> queryCategory(long categoryId) {
        Category category = this.getById(categoryId);
        AssertUtils.isFalse(category == null
                || category.getStatus() == CategoryStatusEnum.DELETED
                ,"分类不存在");

        CategoryVO categoryVO = BeanUtil.copyProperties(category, CategoryVO.class);
        return Result.success(categoryVO);
    }

    @Override
    public Result<Void> updateStatus(long categoryId, boolean enable) {
        Category category = this.getById(categoryId);
        AssertUtils.notNull(category, "该分类不存在");

        // 如果已是目标状态，不进行更新
        CategoryStatusEnum targetStatus = enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED;
        if (category.getStatus().equals(targetStatus)) {
            return Result.success();// "分类状态已是目标状态"
        }

        category.setStatus(enable ? CategoryStatusEnum.ENABLED : CategoryStatusEnum.DISABLED);
        AssertUtils.isTrue(this.updateById(category), "分类状态更新失败");
        return Result.success();
    }
}