package com.hngy.siae.content.dto.request.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签更新请求DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "标签更新请求")
public class TagUpdateDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    @Schema(description = "标签名称", example = "Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "标签描述不能为空")
    @Size(max = 200, message = "标签描述长度不能超过200个字符")
    @Schema(description = "标签描述", example = "Spring Boot 框架相关内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}
