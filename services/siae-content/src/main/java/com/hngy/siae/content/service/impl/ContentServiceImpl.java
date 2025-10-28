package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.web.utils.PageConvertUtil;

import com.hngy.siae.content.common.enums.BaseEnum;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.detail.EmptyDetailVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
/** 
 * 内容服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl
        extends ServiceImpl<ContentMapper, Content>
        implements ContentService {

    @Override
    public Content createContent(ContentDTO dto) {
        // 1. 检查重复
        AssertUtils.isFalse(this.lambdaQuery()
                .eq(Content::getTitle, dto.getTitle())
                .eq(Content::getDescription, dto.getDescription())
                .exists(), "请勿重复提交相同内容");

        // 2. 类型转换
        ContentTypeEnum type = BaseEnum.fromDesc(ContentTypeEnum.class, dto.getType());
        AssertUtils.notNull(type, "不支持的类型：" + dto.getType());

        // 3. 插入主内容，直接按下面的方式拷贝不会报错，遇到类型不同，字段相同的会自动跳过
        // 在之前的写法中，因为两个类的Type字段类型不同所以开启跳过异常配置，然后在重新设置Type
        // 或者写出需要忽略的字段
        Content content = BeanUtil.copyProperties(dto, Content.class, "type");
        content.setType(type);
        // 自动回填主键，id
        AssertUtils.isTrue(this.save(content), "内容插入失败");

        return content;
    }


    @Override
    public Content updateContent(ContentDTO contentDTO) {
        // 1. 校验内容是否存在
        AssertUtils.isTrue(this.lambdaQuery()
                .eq(Content::getId, contentDTO.getId())
                .exists(), "这个内容不存在无法更新");

        // 2. 拷贝 DTO 到实体，并处理类型转换
        Content content = BeanConvertUtil.to(contentDTO, Content.class);
        Optional.ofNullable(contentDTO.getType())
                .map(typeStr -> {
                    ContentTypeEnum type = BaseEnum.fromDesc(ContentTypeEnum.class, typeStr);
                    AssertUtils.notNull(type, "不支持的内容类型: " + typeStr);
                    return type;
                })
                .ifPresent(content::setType);

        // 3. 更新数据库
        AssertUtils.isTrue(this.updateById(content), "内容更新失败");

        return content;
    }


    @Override
    public Result<Void> deleteContent(Integer id, Integer isTrash) {
        // 1. 参数校验
        AssertUtils.isTrue(isTrash == 0 || isTrash == 1, "无效操作");

        // 2. 查询内容是否存在
        Content content = this.getById(id);
        AssertUtils.notNull(content, "内容不存在，无法删除");

        // 3. 删除
        if (isTrash == 1) {
            // 放入回收站（软删除）
            content.setStatus(ContentStatusEnum.TRASH);
            AssertUtils.isTrue(this.updateById(content), "放入回收站操作失败");
        } else {
            // 不放入回收站（伪硬删除，通过定时任务实现硬删除）
            content.setStatus(ContentStatusEnum.DELETED);
            AssertUtils.isTrue(this.updateById(content), "永久删除操作失败");
        }

        return Result.success(null);
    }


    @Override
    public Result<PageVO<ContentVO<EmptyDetailVO>>> getContentPage(PageDTO<ContentPageDTO> dto) {
        // 创建分页参数
        Page<Content> page = PageConvertUtil.toPage(dto);

        LambdaQueryWrapper<Content> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Content::getId);

        ContentPageDTO params = dto.getParams();
        if (params != null) {
            if (params.getCategoryId() != null) {
                queryWrapper.eq(Content::getCategoryId, params.getCategoryId());
            }
            if (params.getStatus() != null) {
                queryWrapper.eq(Content::getStatus, params.getStatus());
            }
            if (params.getType() != null) {
                queryWrapper.eq(Content::getType, params.getType());
            }
            if (CollUtil.isNotEmpty(params.getTagIds())) {
                String tagIdSql = CollUtil.join(params.getTagIds(), ",");
                queryWrapper.inSql(Content::getId,
                        "SELECT ctr.content_id FROM content_tag_relation ctr WHERE ctr.tag_id IN (" + tagIdSql + ")");
            }
        }

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword();
            queryWrapper.and(wrapper ->
                    wrapper.like(Content::getTitle, keyword)
                            .or()
                            .like(Content::getDescription, keyword)
            );
        }

        queryWrapper.orderByDesc(Content::getCreateTime);

        Page<Content> resultPage = this.page(page, queryWrapper);

        PageVO<ContentVO<EmptyDetailVO>> pageVO = PageConvertUtil.convert(resultPage, content -> {
            ContentVO<EmptyDetailVO> vo = new ContentVO<>();
            BeanUtils.copyProperties(content, vo);
            vo.setDetail(new EmptyDetailVO());
            return vo;
        });

        return Result.success(pageVO);
    }
}
