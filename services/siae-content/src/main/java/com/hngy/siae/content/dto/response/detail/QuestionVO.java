package com.hngy.siae.content.dto.response.detail;

import com.hngy.siae.content.common.enums.status.QuestionStatusEnum;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionVO implements ContentDetailVO {
    private Long id;
    private Long contentId;
    private String content;
    private Integer answerCount;
    private QuestionStatusEnum solved;
    private Date createTime;
    private Date updateTime;
}