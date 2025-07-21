package com.hngy.siae.content.dto.response.detail;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.hngy.siae.content.dto.response.ContentDetailVO;

/**
 * 空细节vo，用作占位
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EmptyDetailVO implements ContentDetailVO {
}
