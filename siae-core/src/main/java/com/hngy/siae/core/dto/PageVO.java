package com.hngy.siae.core.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应结果
 * 
 * @author SIAE开发团队
 * @param <T> 列表中元素类型
 */
@Data
public class PageVO<T> {

    private Long total;        // 总条数
    private Integer pageNum;   // 当前页
    private Integer pageSize;  // 每页条数
    private List<T> records;   // 当前页数据列表

    // 兼容旧版本的字段名和类型
    public Integer getPage() {
        return this.pageNum;
    }

    public void setPage(Integer page) {
        this.pageNum = page;
    }

    public Integer getTotal() {
        return this.total != null ? this.total.intValue() : 0;
    }
}
