package com.hngy.siae.user.dto.request;

import com.hngy.siae.common.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 奖项等级创建/更新用 DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "奖项等级请求实体")
public class AwardLevelDTO {

    @Schema(description = "奖项等级ID", example = "1")
    @NotNull(message = "奖项ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    @Schema(description = "奖项等级名称", example = "国家级")
    @NotBlank(message = "奖项等级名称不能为空")
    private String name;
}
