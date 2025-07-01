package com.hngy.siae.content.dto.response.detail;

import com.hngy.siae.model.dto.response.ContentDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteVO implements ContentDetailVO {
    private Long id;
    private Long contentId;
    private String content;
    private String format;
    private Date createTime;
    private Date updateTime;
}