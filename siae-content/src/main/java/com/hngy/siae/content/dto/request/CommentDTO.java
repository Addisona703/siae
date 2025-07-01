package com.hngy.siae.content.dto.request;

import com.hngy.siae.common.validation.CreateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    @NotNull(groups = CreateGroup.class)
    private Long userId;
    private Long parentId;
    @NotBlank
    private String content;
}
