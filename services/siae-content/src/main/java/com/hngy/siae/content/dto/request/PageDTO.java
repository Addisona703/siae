package com.hngy.siae.content.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO {
    private String keyword;
    @NotNull
    private Integer page;
    @NotNull
    private Integer pageSize;
}
