package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 31833
* @description 针对表【content_statistics(内容统计表)】的数据库操作Mapper
* @createDate 2025-05-15 16:48:34
* @Entity com.hngy.siae.com.hngy.siae.content.model.entity.Statistics
*/
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {

    /**
     * 查询热门内容列表
     *
     * @param categoryId 分类ID
     * @param type 内容类型
     * @param sortBy 排序字段
     * @param pageSize 每页大小
     * @param offset 偏移量
     * @return 热门内容列表
     */
    List<HotContentVO> selectHotContent(
            @Param("categoryId") Long categoryId,
            @Param("type") String type,
            @Param("sortBy") String sortBy,
            @Param("pageSize") Integer pageSize,
            @Param("offset") Integer offset
    );
}




