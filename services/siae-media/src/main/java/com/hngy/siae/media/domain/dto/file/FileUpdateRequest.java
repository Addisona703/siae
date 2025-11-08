package com.hngy.siae.media.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 文件更新请求 DTO
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "文件元数据更新参数")
public class FileUpdateRequest {

    @Schema(description = "新的业务标签集合，完全覆盖原值")
    private List<String> bizTags;

    @Schema(description = "访问控制设置，留空则保持不变")
    private Map<String, Object> acl;

    @Schema(description = "扩展字段，键值对形式，留空则不更新")
    private Map<String, Object> ext;

}
