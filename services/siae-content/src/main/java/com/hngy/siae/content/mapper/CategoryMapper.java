package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 31833
* @description 针对表【content_category(内容分类表)】的数据库操作Mapper
* @createDate 2025-05-15 16:48:34
* @Entity com.hngy.siae.com.hngy.siae.content.model.entity.Category
*/
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}




