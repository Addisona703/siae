package com.hngy.siae.content.facade;

import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import jakarta.validation.constraints.NotNull;

/**
 * 内容外观
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
public interface ContentFacade {

    /**
     * 发布内容
     *
     * @param contentDTO 内容dto
     * @return {@link Result }<{@link ContentVO }<{@link ContentDetailVO }>>
     */
    Result<ContentVO<ContentDetailVO>> publishContent(ContentDTO contentDTO);

    /**
     * 编辑内容
     *
     * @param contentDTO 内容dto
     * @return {@link Result }<{@link ContentVO }<{@link ContentDetailVO }>>
     */
    Result<ContentVO<ContentDetailVO>> editContent(ContentDTO contentDTO);

    /**
     * 查询内容
     *
     * @param contentId 内容ID
     * @return {@link Result }<{@link ContentVO }<{@link ContentDetailVO }>>
     */
    Result<ContentVO<ContentDetailVO>> queryContent(@NotNull Long contentId);

    /**
     * 查询热门内容
     *
     * @param contentHotPageDTO 内容热点页面dto
     * @return {@link Result }<{@link PageVO }<{@link HotContentVO }>>
     */
    Result<PageVO<HotContentVO>> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO);
}
