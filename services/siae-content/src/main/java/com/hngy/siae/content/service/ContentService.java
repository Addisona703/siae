package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.entity.Content;


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
     * @param contentDTO 内容dto
     * @return {@link Content }
     */
    Content createContent(ContentDTO contentDTO);

    /**
     * 更新内容
     *
     * @param contentDTO 内容dto
     * @return {@link Content }
     */
    Content updateContent(ContentDTO contentDTO);

    /**
     * 删除内容
     *
     * @param id      内容id
     * @param isTrash 是否放入回收站
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> deleteContent(Integer id, Integer isTrash);

//    /**
//     * 获取内容列表
//     *
//     * @param dto 分页查询参数
//     * @return {@link PageVO }<{@link ContentVO }<{@link EmptyDetailVO }>>
//     */
//    Result<PageVO<ContentVO<EmptyDetailVO>>> getContentPage(PageDTO<ContentPageDTO> dto);
}
