package com.hngy.siae.content.dto.response;

import java.util.Date;

import com.hngy.siae.common.enums.status.AuditStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    private Long contentId;
    private Long userId;
    private Long parentId;
    private String content;
    private AuditStatusEnum status;
    private Date createTime;
    private Date updateTime;

}
