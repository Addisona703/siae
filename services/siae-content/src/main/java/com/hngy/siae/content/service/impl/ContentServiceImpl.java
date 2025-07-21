package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.asserts.AssertUtils;

import com.hngy.siae.content.common.enums.BaseEnum;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.common.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final ContentMapper contentMapper;


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
        Content content = BeanUtil.copyProperties(contentDTO, Content.class, "type");
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


//    @Override
//    public Result<PageVO<ContentVO<EmptyDetailVO>>> getContentPage(PageDTO<ContentPageDTO> dto) {
//        // 创建分页参数
//        Page<Content> page = dto.toPage();
//        // 查询数据
//        List<Content> list = contentMapper.selectByCondition(dto);
//        AssertUtils.notNull(list, "这里没有任何内容");
//        // 查询总数
//        long total = contentMapper.countByCondition(dto.getParams());
//
//        // TODO: 整合的时候直接改成用mybatis-plus写多表分页查询
//
//        // 转换成 VO
//        List<ContentVO<EmptyDetailVO>> voList = list.stream().map(content -> {
//            ContentVO<EmptyDetailVO> vo = new ContentVO<>();
//            BeanUtils.copyProperties(content, vo);
//            vo.setDetail(new EmptyDetailVO());
//            return vo;
//        }).collect(Collectors.toList());
//
//        // 封装分页结果
//        PageVO<ContentVO<EmptyDetailVO>> pageVO = new PageVO<>();
//        pageVO.setPage((int) page.getCurrent());
//        pageVO.setPageSize((int) page.getSize());
//        pageVO.setTotal((int) total);
//        pageVO.setRecords(voList);
//
//        return Result.success(pageVO);
//    }
}