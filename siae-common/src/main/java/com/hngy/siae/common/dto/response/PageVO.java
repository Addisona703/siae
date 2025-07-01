package com.hngy.siae.common.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应结果
 * @param <T> 列表中元素类型
 */
@Data
public class PageVO<T> {

    private Long total;        // 总条数
    private Integer pageNum;   // 当前页
    private Integer pageSize;  // 每页条数
    private List<T> records;   // 当前页数据列表
}
