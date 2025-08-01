package com.hngy.siae.content.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签查询DTO
 *
 * @author KEYKB
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagQueryDTO {

    /**
     * 标签名称
     */
    private String name;

    /**
     * 创建时间范围 - 开始时间
     */
    private String createdAtStart;

    /**
     * 创建时间范围 - 结束时间
     */
    private String createdAtEnd;
}
