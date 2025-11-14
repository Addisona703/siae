package com.hngy.siae.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.entity.Tag;
import com.hngy.siae.content.entity.TagRelation;
import com.hngy.siae.content.mapper.TagMapper;
import com.hngy.siae.content.mapper.TagRelationMapper;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hngy.siae.core.asserts.AssertUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签关系服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Service
@RequiredArgsConstructor
public class TagRelationServiceImpl
        extends ServiceImpl<TagRelationMapper, TagRelation>
        implements TagRelationService {

    private final TagMapper tagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRelations(Long contentId, List<Long> tagIds) {
        // 空标签直接跳过
        if (CollUtil.isEmpty(tagIds)) {
            return;
        }

        // 验证标签是否存在
        Set<Long> validTagIds = tagMapper.selectBatchIds(tagIds)
                .stream().map(Tag::getId).collect(Collectors.toSet());
        AssertUtils.isTrue(validTagIds.size() == tagIds.size(), ContentResultCodeEnum.TAG_PARTIAL_NOT_EXISTS);

        // 查出已存在的关系，避免重复插入
        Set<Long> existingTagIds = this.lambdaQuery()
                .eq(TagRelation::getContentId, contentId)
                .in(TagRelation::getTagId, tagIds)
                .list().stream()
                .map(TagRelation::getTagId)
                .collect(Collectors.toSet());

        // 构建待插入对象
        List<TagRelation> toInsert = tagIds.stream()
                .filter(id -> !existingTagIds.contains(id))
                .map(id -> TagRelation.builder()
                        .contentId(contentId)
                        .tagId(id)
                        .build())
                .toList();

        if (CollUtil.isNotEmpty(toInsert)) {
            boolean success = this.saveBatch(toInsert);
            AssertUtils.isTrue(success, ContentResultCodeEnum.TAG_RELATION_INSERT_FAILED);
        }
    }

    @Override
    public void deleteRelations(Long contentId) {
        boolean success = this.remove(new LambdaQueryWrapper<TagRelation>()
                .eq(TagRelation::getContentId, contentId));
        AssertUtils.isTrue(success, ContentResultCodeEnum.TAG_RELATION_DELETE_FAILED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRelations(Long contentId, List<Long> tagIds) {
        if(CollUtil.isEmpty(tagIds)) {
            return;
        }
        this.deleteRelations(contentId);
        this.createRelations(contentId, tagIds);
    }
}


