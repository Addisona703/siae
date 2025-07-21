package com.hngy.siae.content.dto.request.content.detail;

import com.hngy.siae.content.common.enums.status.QuestionStatusEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDetailDTO implements ContentDetailDTO {
    private String content;
    private QuestionStatusEnum status;
}
