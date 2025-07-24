package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.content.dto.request.TagDTO;
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
     * @param tagDTO 标签dto
     * @return {@link Result }<{@link TagVO }>
     */
    Result<TagVO> createTag(TagDTO tagDTO);

    /**
     * 更新标签
     *
     * @param id     标签id
     * @param tagDTO 标签dto
     * @return {@link Result }<{@link TagVO }>
     */
    Result<TagVO> updateTag(Long id, TagDTO tagDTO);

    /**
     * 删除标签
     *
     * @param id 标签id
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> deleteTag(Long id);

    /**
     * 查询标签列表
     *
     * @param pageDTO 第dto页
     * @return {@link Result }<{@link PageVO }<{@link TagVO }>>
     */
    Result<PageVO<TagVO>> listTags(PageDTO<?> pageDTO);
}