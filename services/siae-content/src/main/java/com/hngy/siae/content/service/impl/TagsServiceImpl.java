package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.TagDTO;
import com.hngy.siae.content.dto.response.TagVO;
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
    public Result<TagVO> createTag(TagDTO tagDTO) {
        // 检查标签是否存在
        boolean exists = lambdaQuery()
                .eq(Tag::getName, tagDTO.getName())
                .or()
                .eq(Tag::getDescription, tagDTO.getDescription())
                .exists();
        AssertUtils.isFalse(exists, "标签已存在");

        // 插入标签
        Tag tag = BeanUtil.copyProperties(tagDTO, Tag.class);
        AssertUtils.isTrue(this.save(tag), "标签保存失败，请重试");

        // 封装vo返回
        TagVO vo = BeanUtil.copyProperties(tag, TagVO.class);
        return Result.success(vo);
    }


    @Override
    public Result<TagVO> updateTag(Long id, TagDTO tagDTO) {
        // 如果 ID 为空或查不到对应标签，则创建新标签
        Tag existingTag = ObjectUtil.isNull(id) ? null : this.getById(id);
        if (ObjectUtil.isNull(existingTag)) {
            return createTag(tagDTO);
        }

        // 检查标签名称或描述是否和其他标签重复（排除自己）
        boolean exists = lambdaQuery()
                .eq(Tag::getName, tagDTO.getName())
                .or()
                .eq(Tag::getDescription, tagDTO.getDescription())
                .ne(Tag::getId, id)
                .exists();
        AssertUtils.isFalse(exists, "标签已存在，不要重复更新");

        // 拷贝属性并更新
        Tag tag = BeanUtil.copyProperties(tagDTO, Tag.class);
        tag.setId(id);
        AssertUtils.isTrue(this.updateById(tag), "更新标签失败");

        TagVO vo = BeanUtil.copyProperties(tag, TagVO.class);
        return Result.success(vo);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteTag(Long id) {
        // 检查标签是否存在
        Tag existingTag = this.getById(id);
        AssertUtils.notNull(existingTag, "标签不存在");

        // 删除标签-内容关联表数据
        tagRelationService.deleteRelations(id);

        // 删除标签
        AssertUtils.isTrue(this.removeById(id), "删除标签失败");
        return Result.success();
    }


//    @Override
//    public Result<PageVO<TagVO>> listTags(PageDTO pageDTO) {
//        // 构建分页对象
//        Page<Tag> page = new Page<>(pageDTO.getPage(), pageDTO.getPageSize());
//
//        // 构建查询条件
//        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
//        String keyword = pageDTO.getKeyword();
//        if (StrUtil.isNotBlank(keyword)) {
//            queryWrapper.and(wrapper ->
//                    wrapper.like(Tag::getName, keyword)
//                            .or()
//                            .like(Tag::getDescription, keyword)
//            );
//        }
//
//        // 执行分页查询
//        Page<Tag> result = baseMapper.selectPage(page, queryWrapper);
//
//        // 转换结果为 VO 对象列表
//        List<TagVO> tagVOList = result.getRecords().stream()
//                .map(tag -> BeanUtil.copyProperties(tag, TagVO.class))
//                .collect(Collectors.toList());
//
//        // 封装分页响应结果
//        PageVO<TagVO> pageVO = new PageVO<>();
//        pageVO.setPage((int) result.getCurrent());
//        pageVO.setPageSize((int) result.getSize());
//        pageVO.setTotal((int) result.getTotal());
//        pageVO.setRecords(tagVOList);
//
//        return Result.success(pageVO);
//    }
}
