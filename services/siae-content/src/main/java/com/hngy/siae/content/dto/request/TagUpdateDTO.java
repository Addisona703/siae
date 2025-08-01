package com.hngy.siae.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签更新DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateDTO {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;

    /**
     * 标签描述
     */
    @NotBlank(message = "标签描述不能为空")
    @Size(max = 200, message = "标签描述长度不能超过200个字符")
    private String description;
}
