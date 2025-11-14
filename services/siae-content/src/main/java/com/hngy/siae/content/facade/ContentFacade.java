package com.hngy.siae.content.facade;

import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.response.HotContentVO;
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
     * @param contentCreateDTO 内容创建dto
     * @return {@link ContentVO }<{@link ContentDetailVO }>
     */
    ContentVO<ContentDetailVO> publishContent(ContentCreateDTO contentCreateDTO);

    /**
     * 编辑内容
     *
     * @param contentUpdateDTO 内容更新dto
     * @return {@link ContentVO }<{@link ContentDetailVO }>
     */
    ContentVO<ContentDetailVO> editContent(ContentUpdateDTO contentUpdateDTO);

    /**
     * 查询内容
     *
     * @param contentId 内容ID
     * @return {@link ContentVO }<{@link ContentDetailVO }>
     */
    ContentVO<ContentDetailVO> queryContent(@NotNull Long contentId);

    /**
     * 查询热门内容
     *
     * @param contentHotPageDTO 内容热点页面dto
     * @return {@link PageVO }<{@link HotContentVO }>
     */
    PageVO<HotContentVO> queryHotContent(@NotNull ContentHotPageDTO contentHotPageDTO);
}
