package com.hngy.siae.content.facade;

import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.HotContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import jakarta.validation.constraints.NotNull;

/**
 * 内容读操作外观接口
 * 负责查询、热门内容、搜索等读操作
 *
 * @author KEYKB
 * &#064;date  2025/11/27
 */
public interface ContentReadFacade {

    /**
     * 查询内容详情
     *
     * @param contentId 内容ID
     * @return {@link ContentVO}<{@link ContentDetailVO}>
     */
    ContentVO<ContentDetailVO> queryContent(@NotNull Long contentId);

    /**
     * 查询热门内容
     *
     * @param contentHotPageDTO 热门内容查询参数
     * @return {@link PageVO}<{@link HotContentVO}>
     */
    PageVO<HotContentVO> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO);

    /**
     * 搜索内容（已发布的内容 + 当前用户的草稿和待审核）
     *
     * @param contentPageDTO 内容分页查询参数
     * @return {@link PageVO}<{@link ContentVO}<{@link EmptyDetailVO}>>
     */
    PageVO<ContentVO<EmptyDetailVO>> searchContent(@NotNull PageDTO<ContentQueryDTO> contentPageDTO);

    /**
     * 管理员查询待审核内容（不包括草稿）
     *
     * @param contentPageDTO 内容分页查询参数
     * @return {@link PageVO}<{@link ContentVO}<{@link EmptyDetailVO}>>
     */
    PageVO<ContentVO<EmptyDetailVO>> searchPendingContent(@NotNull PageDTO<ContentQueryDTO> contentPageDTO);
}
