package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 31833
* @description 针对表【content(内容主表)】的数据库操作Mapper
* @createDate 2025-05-15 16:48:34
* @Entity com.hngy.siae.com.hngy.siae.content.model.entity.Content
*/
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 按条件选择
     *
     * @param page 分页条件
     * @param dto  查询条件
     * @return {@link IPage }<{@link ContentVO }<{@link ? }>>
     */
    List<Content> selectByCondition(Page<?> page, @Param("dto") ContentPageDTO dto);

    /**
     * 按条件计数
     *
     * @param dto 查询参数
     * @return {@link Long }
     */
    Long countByCondition(@Param("dto") ContentPageDTO dto);
}




