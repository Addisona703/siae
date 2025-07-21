package com.hngy.siae.content.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.QuestionVO;
import com.hngy.siae.content.entity.detail.Question;
import com.hngy.siae.content.mapper.QuestionMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.hngy.siae.core.asserts.AssertUtils;

import java.util.List;

@StrategyType(ContentTypeEnum.QUESTION)
@Component
@RequiredArgsConstructor
public class QuestionContentStrategy
        extends ServiceImpl<QuestionMapper, Question>
        implements ContentStrategy {

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Question question = BeanUtil.copyProperties(dto, Question.class);
        question.setContentId(contentId);
        AssertUtils.isTrue(this.save(question), "问答详情插入失败");
        return BeanUtil.copyProperties(question, QuestionVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        Question question = BeanUtil.copyProperties(dto, Question.class);
        question.setContentId(contentId);
        AssertUtils.isTrue(this.updateById(question), "问答详情更新失败");
        return BeanUtil.copyProperties(question, QuestionVO.class);
    }

    @Override
    public void delete(Long contentId) {
        this.remove(new LambdaQueryWrapper<Question>().eq(Question::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return this.remove(new LambdaQueryWrapper<Question>().in(Question::getContentId, contentIds));
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Question question = this.getOne(new LambdaQueryWrapper<Question>().eq(Question::getContentId, contentId));
        AssertUtils.notNull(question, "获取详情失败，该内容详情不存在");
        return BeanUtil.copyProperties(question, QuestionVO.class);
    }
}