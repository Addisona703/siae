package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.detail.EmptyDetailVO;
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
     * 删除内容（带权限校验）
     *
     * @param id            内容id
     * @param isTrash       是否放入回收站
     * @param currentUserId 当前用户ID
     * @param isAdmin       是否为管理员
     */
    void deleteContent(Integer id, Integer isTrash, Long currentUserId, boolean isAdmin);

    /**
     * 获取内容列表
     *
     * @param dto 分页查询参数
     * @return {@link PageVO }<{@link ContentVO }<{@link EmptyDetailVO }>>
     */
    PageVO<ContentVO<EmptyDetailVO>> getContentPage(PageDTO<ContentPageDTO> dto);
}
