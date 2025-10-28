package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.entity.Content;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容表 Mapper
 *
 * @author KEYKB
 * @description 针对表【content(内容主表)】的数据库操作Mapper
 * @createDate 2025-05-15 16:48:34
 * @Entity com.hngy.siae.content.entity.Content
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {
}
