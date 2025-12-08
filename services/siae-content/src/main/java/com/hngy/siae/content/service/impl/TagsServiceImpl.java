package com.hngy.siae.content.service.impl;

import com.hngy.siae.core.utils.BeanConvertUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.PageConvertUtil;
import com.hngy.siae.content.dto.request.tag.TagCreateDTO;
import com.hngy.siae.content.dto.request.tag.TagUpdateDTO;
import com.hngy.siae.content.dto.request.tag.TagQueryDTO;
import com.hngy.siae.content.dto.response.tag.TagVO;
import com.hngy.siae.content.entity.Tag;
import com.hngy.siae.content.mapper.TagMapper;
import com.hngy.siae.content.service.TagRelationService;
import com.hngy.siae.content.service.TagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@Service
@RequiredArgsConstructor
public class TagsServiceImpl
        extends ServiceImpl<TagMapper, Tag>
        implements TagsService {

    private final TagRelationService tagRelationService;


    @Override
    public TagVO createTag(TagCreateDTO tagCreateDTO) {
        // 检查标签是否存在
        boolean exists = lambdaQuery()
                .eq(Tag::getName, tagCreateDTO.getName())
                .or()
                .eq(Tag::getDescription, tagCreateDTO.getDescription())
                .exists();
        AssertUtils.isFalse(exists, ContentResultCodeEnum.TAG_ALREADY_EXISTS);

        // 插入标签
        Tag tag = BeanConvertUtil.to(tagCreateDTO, Tag.class);
        AssertUtils.isTrue(this.save(tag), ContentResultCodeEnum.TAG_SAVE_FAILED);

        // 返回VO对象
        return BeanConvertUtil.to(tag, TagVO.class);
    }


    @Override
    public TagVO updateTag(Long id, TagUpdateDTO tagUpdateDTO) {
        // 如果 ID 为空或查不到对应标签，则抛出异常
        Tag existingTag = ObjectUtil.isNull(id) ? null : this.getById(id);
        AssertUtils.notNull(existingTag, ContentResultCodeEnum.TAG_NOT_FOUND);

        // 检查标签名称或描述是否和其他标签重复（排除自己）
        boolean exists = lambdaQuery()
                .eq(Tag::getName, tagUpdateDTO.getName())
                .or()
                .eq(Tag::getDescription, tagUpdateDTO.getDescription())
                .ne(Tag::getId, id)
                .exists();
        AssertUtils.isFalse(exists, ContentResultCodeEnum.TAG_ALREADY_EXISTS);

        // 拷贝属性并更新
        Tag tag = BeanConvertUtil.to(tagUpdateDTO, Tag.class);
        tag.setId(id);
        AssertUtils.isTrue(this.updateById(tag), ContentResultCodeEnum.TAG_UPDATE_FAILED);

        return BeanConvertUtil.to(tag, TagVO.class);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        // 检查标签是否存在
        Tag existingTag = this.getById(id);
        AssertUtils.notNull(existingTag, ContentResultCodeEnum.TAG_NOT_FOUND);

        // 删除标签-内容关联表数据
        tagRelationService.deleteRelations(id);

        // 删除标签
        AssertUtils.isTrue(this.removeById(id), ContentResultCodeEnum.TAG_DELETE_FAILED);
    }


    @Override
    public TagVO getTagById(Long id) {
        // 查询标签
        Tag tag = this.getById(id);
        AssertUtils.notNull(tag, ContentResultCodeEnum.TAG_NOT_FOUND);

        // 转换为VO对象
        return BeanConvertUtil.to(tag, TagVO.class);
    }


    @Override
    public PageVO<TagVO> listTags(PageDTO<TagQueryDTO> pageDTO) {
        // 构建分页对象
        Page<Tag> page = PageConvertUtil.toPage(pageDTO);

        // 获取查询条件
        TagQueryDTO queryDTO = pageDTO.getParams();

        // 构建查询条件
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Tag::getId);

        if (queryDTO != null) {
            // 关键词搜索（搜索名称或描述）
            if (StrUtil.isNotBlank(queryDTO.getName())) {
                queryWrapper.and(wrapper ->
                        wrapper.like(Tag::getName, queryDTO.getName())
//                                .or()
//                                .like(Tag::getDescription, queryDTO.getName())
                );
            }

//            // 精确匹配标签名称
//            if (StrUtil.isNotBlank(queryDTO.getName())) {
//                queryWrapper.eq(Tag::getName, queryDTO.getName());
//            }

            // 创建时间范围查询
            if (StrUtil.isNotBlank(queryDTO.getCreatedAtStart())) {
                queryWrapper.ge(Tag::getCreateTime, queryDTO.getCreatedAtStart());
            }
            if (StrUtil.isNotBlank(queryDTO.getCreatedAtEnd())) {
                queryWrapper.le(Tag::getCreateTime, queryDTO.getCreatedAtEnd());
            }
        }

        // 执行分页查询
        Page<Tag> result = baseMapper.selectPage(page, queryWrapper);

        // 使用 PageConvertUtil 转换分页结果并返回
        return PageConvertUtil.convert(result, TagVO.class);
    }
}
