package com.hngy.siae.api.content.dto.response;

import com.hngy.siae.api.content.enums.ContentTypeEnum;
import com.hngy.siae.api.content.enums.ContentStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容VO
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentVO<T extends ContentDetailVO> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    @NotNull(message = "标题不能为空")
    private String title;
    
    @NotNull(message = "类型不能为空")
    private ContentTypeEnum type;
    
    private String description;
    
    @NotNull(message = "上传者ID不能为空")
    private Long uploadedBy;
    
    private String authorNickname;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    private String categoryName;
    
    @NotNull(message = "状态不能为空")
    private ContentStatusEnum status;
    
    private StatisticsVO statistics;
    
    private List<Long> tagIds;
    
    private T detail;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
