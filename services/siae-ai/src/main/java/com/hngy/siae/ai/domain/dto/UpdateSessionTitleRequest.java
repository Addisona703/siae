package com.hngy.siae.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新会话标题请求
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "更新会话标题请求")
public class UpdateSessionTitleRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "新标题", required = true, example = "关于Vue3的讨论")
    private String title;
}
