package com.hngy.siae.auth.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO
 * 
 * @author KEYKB
 */
@Data
public class PageResult<T> {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 创建分页结果
     * 
     * @param list  数据列表
     * @param total 总记录数
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        return result;
    }
} 