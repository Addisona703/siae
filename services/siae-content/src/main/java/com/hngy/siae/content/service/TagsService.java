package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.content.dto.request.TagCreateDTO;
import com.hngy.siae.content.dto.request.TagUpdateDTO;
import com.hngy.siae.content.dto.request.TagQueryDTO;
import com.hngy.siae.content.dto.response.TagVO;
import com.hngy.siae.content.entity.Tag;

/**
 * 标签接口
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public interface TagsService extends IService<Tag> {

    /**
     * 创建标签
     *
     * @param tagCreateDTO 标签创建参数
     * @return 创建的标签信息
     */
    TagVO createTag(TagCreateDTO tagCreateDTO);

    /**
     * 更新标签
     *
     * @param id           标签ID
     * @param tagUpdateDTO 标签更新参数
     * @return 更新后的标签信息
     */
    TagVO updateTag(Long id, TagUpdateDTO tagUpdateDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 分页查询标签列表
     *
     * @param pageDTO 分页查询参数，包含查询条件
     * @return 标签分页列表
     */
    PageVO<TagVO> listTags(PageDTO<TagQueryDTO> pageDTO);
}