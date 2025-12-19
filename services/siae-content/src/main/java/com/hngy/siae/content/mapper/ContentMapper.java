package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.api.ai.dto.response.ContentInfo;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.content.ContentQueryResultVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    /**
     * 查询内容详情（包含分类、标签、统计信息）
     *
     * @param contentId 内容ID
     * @return 内容详情DTO
     */
    ContentQueryResultVO selectContentDetailById(@Param("contentId") Long contentId);

    /**
     * 分页查询内容列表（联表查询分类名称和作者昵称）
     *
     * @param page 分页参数
     * @param content 查询条件
     * @return 内容VO分页结果
     */
    Page<ContentVO<EmptyDetailVO>> selectContentPageWithDetails(Page<ContentVO<EmptyDetailVO>> page, @Param("content") Content content);

    /**
     * 分页查询内容列表（已发布的内容 + 当前用户的草稿和待审核）
     *
     * @param page 分页参数
     * @param query 查询条件DTO
     * @param currentUserId 当前用户ID
     * @return 内容VO分页结果
     */
    Page<ContentVO<EmptyDetailVO>> selectContentPageByQuery(Page<ContentVO<EmptyDetailVO>> page, @Param("query") ContentQueryDTO query, @Param("currentUserId") Long currentUserId);

    /**
     * 管理员分页查询待审核内容（不包括草稿）
     *
     * @param page 分页参数
     * @param query 查询条件DTO
     * @return 内容VO分页结果
     */
    Page<ContentVO<EmptyDetailVO>> selectPendingContentPage(Page<ContentVO<EmptyDetailVO>> page, @Param("query") ContentQueryDTO query);

    // ==================== AI 服务接口 ====================

    /**
     * AI搜索内容
     */
    List<ContentInfo> searchForAi(@Param("keyword") String keyword, @Param("categoryName") String categoryName, @Param("limit") Integer limit);

    /**
     * AI获取热门内容
     */
    List<ContentInfo> getHotContentForAi(@Param("limit") Integer limit);

    /**
     * AI获取最新内容
     */
    List<ContentInfo> getLatestContentForAi(@Param("limit") Integer limit);

    /**
     * 统计收藏夹中已发布内容的数量
     *
     * @param folderId 收藏夹ID
     * @return 已发布内容数量
     */
    Long countPublishedFavoriteItems(@Param("folderId") Long folderId);
}
