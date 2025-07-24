package com.hngy.siae.user.dto.request;

import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项类型创建/更新用 DTO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "奖项类型请求数据")
public class AwardTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "奖项类型ID，更新时需要提供", example = "1")
    @NotNull(message = "奖项类型ID不能为空", groups = {UpdateGroup.class})
    private Long id;

    @NotBlank(message = "奖项类型名称不能为空", groups = {CreateGroup.class})
    @Schema(description = "奖项类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "学科竞赛")
    private String name;
}
