package com.hngy.siae.content.dto.request.category;

import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    @NotNull(groups = UpdateGroup.class)
    private Long id;
    @NotBlank(groups = CreateGroup.class)
    private String name;
    @NotBlank(groups = CreateGroup.class)
    private String code;
    private Long parentId;
}
