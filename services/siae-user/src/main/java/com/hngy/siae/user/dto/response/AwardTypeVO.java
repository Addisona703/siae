package com.hngy.siae.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项类型响应 VO
 *
 * @author KEYKB
 */
@Data
@Schema(description = "奖项类型视图对象")
public class AwardTypeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "奖项类型ID", example = "1")
    private Long id;

    @Schema(description = "奖项类型名称", example = "学科竞赛")
    private String name;
}
