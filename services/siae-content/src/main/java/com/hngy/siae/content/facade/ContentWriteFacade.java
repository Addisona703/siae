package com.hngy.siae.content.facade;

import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import jakarta.validation.constraints.NotNull;

/**
 * 内容写操作外观接口
 * 负责发布、编辑、删除等写操作
 *
 * @author KEYKB
 * @date 2025/11/27
 */
public interface ContentWriteFacade {

    /**
     * 发布内容
     *
     * @param contentCreateDTO 内容创建DTO
     * @return {@link ContentVO}<{@link ContentDetailVO}>
     */
    ContentVO<ContentDetailVO> publishContent(@NotNull ContentCreateDTO contentCreateDTO);

    /**
     * 编辑内容
     *
     * @param contentUpdateDTO 内容更新DTO
     * @return {@link ContentVO}<{@link ContentDetailVO}>
     */
    ContentVO<ContentDetailVO> editContent(@NotNull ContentUpdateDTO contentUpdateDTO);

    /**
     * 删除内容
     *
     * @param contentId 内容ID
     * @param isTrash   是否放入回收站（0-永久删除，1-移至回收站）
     */
    void deleteContent(@NotNull Long contentId, @NotNull Integer isTrash);

    /**
     * 恢复内容
     *
     * @param contentId 内容ID
     */
    void restoreContent(@NotNull Long contentId);
}
