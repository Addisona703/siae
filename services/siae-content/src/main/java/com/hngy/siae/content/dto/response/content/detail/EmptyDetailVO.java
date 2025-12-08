package com.hngy.siae.content.dto.response.content.detail;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 空详情 VO，用作占位
 *
 * @author KEYKB
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "空详情响应对象（占位用）")
public class EmptyDetailVO implements ContentDetailVO {
}
