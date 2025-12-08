package com.hngy.siae.content.strategy.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.detail.QuestionVO;
import com.hngy.siae.content.entity.detail.Question;
import com.hngy.siae.content.mapper.QuestionMapper;
import com.hngy.siae.content.strategy.content.ContentStrategy;
import com.hngy.siae.content.strategy.content.ContentType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 问答内容策略
 * 使用组合方式，直接注入 Mapper
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
@ContentType(ContentTypeEnum.QUESTION)
@Component
@RequiredArgsConstructor
public class QuestionContentStrategy implements ContentStrategy {

    private final QuestionMapper questionMapper;

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Question question = BeanConvertUtil.to(dto, Question.class);
        question.setContentId(contentId);
        int rows = questionMapper.insert(question);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.QUESTION_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(question, QuestionVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        // 先查询现有记录
        Question existingQuestion = questionMapper.selectOne(
                new LambdaQueryWrapper<Question>().eq(Question::getContentId, contentId));
        AssertUtils.notNull(existingQuestion, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        
        // 复制 DTO 属性到现有记录
        BeanConvertUtil.to(dto, existingQuestion);
        existingQuestion.setContentId(contentId);
        
        int rows = questionMapper.updateById(existingQuestion);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.QUESTION_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(existingQuestion, QuestionVO.class);
    }

    @Override
    public void delete(Long contentId) {
        questionMapper.delete(new LambdaQueryWrapper<Question>().eq(Question::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return questionMapper.delete(new LambdaQueryWrapper<Question>().in(Question::getContentId, contentIds)) > 0;
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Question question = questionMapper.selectOne(new LambdaQueryWrapper<Question>().eq(Question::getContentId, contentId));
        AssertUtils.notNull(question, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(question, QuestionVO.class);
    }
}
