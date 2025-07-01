package com.hngy.siae.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO<T> {
    private Integer page;
    private Integer pageSize;
    private Integer total;
    private List<T> records;
}
