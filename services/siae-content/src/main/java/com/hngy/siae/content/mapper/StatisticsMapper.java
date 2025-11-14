package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.entity.Statistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 内容统计表 Mapper
 *
 * @author 31833
 */
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {

    /**
     * 分页查询热门内容
     *
     * @param page 分页对象
     * @param categoryId 分类ID
     * @param type 内容类型
     * @param sortBy 排序字段
     * @param publishedStatus 发布状态
     * @return 热门内容分页结果
     */
    IPage<HotContentVO> selectHotContentPage(Page<HotContentVO> page,
                                             @Param("categoryId") Long categoryId,
                                             @Param("type") Integer type,
                                             @Param("sortBy") String sortBy,
                                             @Param("publishedStatus") Integer publishedStatus);
}
