package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.api.ai.dto.response.ContentInfo;
import com.hngy.siae.content.dto.response.content.ContentQueryResultVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.enums.status.ContentStatusEnum;

import java.util.List;


/**
 * 内容服务
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
public interface ContentService extends IService<Content> {

    /**
     * 发布内容
     *
     * @param contentCreateDTO 内容创建dto
     * @return {@link Content }
     */
    Content createContent(ContentCreateDTO contentCreateDTO);

    /**
     * 更新内容
     *
     * @param contentUpdateDTO 内容更新dto
     * @return {@link Content }
     */
    Content updateContent(ContentUpdateDTO contentUpdateDTO);

    /**
     * 删除内容
     *
     * @param id      内容id
     * @param isTrash 是否放入回收站
     */
    void deleteContent(Integer id, Integer isTrash);

    /**
     * 恢复内容（从回收站恢复）
     *
     * @param contentId 内容ID
     */
    void restoreContent(Long contentId);

    /**
     * 获取内容列表（已发布的内容 + 当前用户的草稿和待审核）
     *
     * @param dto 分页查询参数
     * @return {@link PageVO }<{@link ContentVO }<{@link EmptyDetailVO }>>
     */
    PageVO<ContentVO<EmptyDetailVO>> getContentPage(PageDTO<ContentQueryDTO> dto);

    /**
     * 管理员获取待审核内容列表（不包括草稿）
     *
     * @param dto 分页查询参数
     * @return {@link PageVO }<{@link ContentVO }<{@link EmptyDetailVO }>>
     */
    PageVO<ContentVO<EmptyDetailVO>> getPendingContentPage(PageDTO<ContentQueryDTO> dto);

    /**
     * 更新内容状态（使用乐观锁）
     * 用于审核策略模式
     *
     * @param contentId 内容ID
     * @param status    目标状态
     * @return 是否更新成功（乐观锁冲突时返回 false）
     */
    boolean updateStatus(Long contentId, ContentStatusEnum status);

    /**
     * 查询内容详情（包含分类、标签、统计信息）
     *
     * @param contentId 内容ID
     * @return 内容详情DTO
     */
    ContentQueryResultVO getContentDetail(Long contentId);

    // ==================== AI 服务接口 ====================

    /**
     * AI搜索内容
     *
     * @param keyword 关键词
     * @param categoryName 分类名称
     * @param limit 数量限制
     * @return 内容信息列表
     */
    List<ContentInfo> searchForAi(String keyword, String categoryName, Integer limit);

    /**
     * AI获取热门内容
     *
     * @param limit 数量限制
     * @return 热门内容列表
     */
    List<ContentInfo> getHotContentForAi(Integer limit);

    /**
     * AI获取最新内容
     *
     * @param limit 数量限制
     * @return 最新内容列表
     */
    List<ContentInfo> getLatestContentForAi(Integer limit);
}
