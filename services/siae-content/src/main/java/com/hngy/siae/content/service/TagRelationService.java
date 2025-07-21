package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.content.entity.TagRelation;

import java.util.List;

/**
 * 标签关系服务
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public interface TagRelationService extends IService<TagRelation> {

    /**
     * 创建标签对内容关联
     *
     * @param contentId 内容ID
     * @param tagIds    标签id
     */
    void createRelations(Long contentId, List<Long> tagIds);

    /**
     * 删除标签对内容关联
     *
     * @param contentId 内容ID
     */
    void deleteRelations(Long contentId);

    /**
     * 更新标签对内容关联
     *
     * @param contentId 内容ID
     * @param tagIds    标签id
     */
    void updateRelations(Long contentId, List<Long> tagIds);
}

