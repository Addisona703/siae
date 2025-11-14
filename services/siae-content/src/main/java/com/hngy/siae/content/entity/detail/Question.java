package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.hngy.siae.content.enums.status.QuestionStatusEnum;
import lombok.Data;

/**
 * 问题详情表
 * @TableName content_question
 */
@Data
public class Question {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的内容ID，外键，指向 content 表
     */
    private Long contentId;

    /**
     * 问题详细描述
     */
    private String content;

    /**
     * 回答数量
     */
    private Integer answerCount = 0;

    /**
     * 是否已解决：0未解决，1已解决
     */
    private QuestionStatusEnum solved = QuestionStatusEnum.UNSOLVED;

    /**
     * 创建时间，默认当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，默认当前时间，更新时自动刷新
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}