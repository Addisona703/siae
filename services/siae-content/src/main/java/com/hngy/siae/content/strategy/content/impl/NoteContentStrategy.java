package com.hngy.siae.content.strategy.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.detail.NoteVO;
import com.hngy.siae.content.entity.detail.Note;
import com.hngy.siae.content.mapper.NoteMapper;
import com.hngy.siae.content.strategy.content.ContentStrategy;
import com.hngy.siae.content.strategy.content.ContentType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 笔记内容策略
 * 使用组合方式，直接注入 Mapper
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
@ContentType(ContentTypeEnum.NOTE)
@Component
@RequiredArgsConstructor
public class NoteContentStrategy implements ContentStrategy {

    private final NoteMapper noteMapper;

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Note note = BeanConvertUtil.to(dto, Note.class);
        note.setContentId(contentId);
        int rows = noteMapper.insert(note);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.NOTE_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(note, NoteVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        // 先查询现有记录
        Note existingNote = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>().eq(Note::getContentId, contentId));
        AssertUtils.notNull(existingNote, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        
        // 复制 DTO 属性到现有记录
        BeanConvertUtil.to(dto, existingNote);
        existingNote.setContentId(contentId);
        
        int rows = noteMapper.updateById(existingNote);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.NOTE_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(existingNote, NoteVO.class);
    }

    @Override
    public void delete(Long contentId) {
        noteMapper.delete(new LambdaQueryWrapper<Note>().eq(Note::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return noteMapper.delete(new LambdaQueryWrapper<Note>().in(Note::getContentId, contentIds)) > 0;
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Note note = noteMapper.selectOne(new LambdaQueryWrapper<Note>().eq(Note::getContentId, contentId));
        AssertUtils.notNull(note, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(note, NoteVO.class);
    }
}
