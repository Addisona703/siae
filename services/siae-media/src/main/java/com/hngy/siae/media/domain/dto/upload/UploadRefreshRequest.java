package com.hngy.siae.media.domain.dto.upload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 上传刷新请求 DTO
 * 用于刷新预签名 URL 或追加分片
 *
 * @author SIAE Team
 */
@Data
@Schema(description = "刷新上传会话所需的分片信息")
public class UploadRefreshRequest {

    @Schema(description = "需要重新获取URL的分片列表，为空则刷新全部")
    private List<PartRequest> parts;

    @Data
    @Schema(description = "待刷新分片")
    public static class PartRequest {

        @Schema(description = "分片序号，从1开始")
        private Integer partNumber;
    }

}
