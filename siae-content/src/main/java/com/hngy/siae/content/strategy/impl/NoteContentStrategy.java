package com.hngy.siae.content.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.NoteVO;
import com.hngy.siae.content.entity.detail.Note;
import com.hngy.siae.content.mapper.NoteMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 笔记内容策略
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@StrategyType(ContentTypeEnum.NOTE)
@Component
@RequiredArgsConstructor
public class NoteContentStrategy
        extends ServiceImpl<NoteMapper, Note>
        implements ContentStrategy {

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Note note = BeanUtil.copyProperties(dto, Note.class);
        note.setContentId(contentId);
        AssertUtil.isTrue(this.save(note), "笔记详情插入失败");
        return BeanUtil.copyProperties(note, NoteVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        Note note = BeanUtil.copyProperties(dto, Note.class);
        note.setContentId(contentId);
        AssertUtil.isTrue(this.updateById(note), "笔记详情更新失败");
        return BeanUtil.copyProperties(note, NoteVO.class);
    }

    @Override
    public void delete(Long contentId) {
        this.remove(new LambdaQueryWrapper<Note>().eq(Note::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return this.remove(new LambdaQueryWrapper<Note>().in(Note::getContentId, contentIds));
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Note note = this.getOne(new LambdaQueryWrapper<Note>().eq(Note::getContentId, contentId));
        AssertUtil.notNull(note, "获取详情失败，该内容详情不存在");
        return BeanUtil.copyProperties(note, NoteVO.class);
    }
}