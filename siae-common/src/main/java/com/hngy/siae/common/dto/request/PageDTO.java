package com.hngy.siae.common.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用分页请求参数基类
 * 支持继承和泛型两种使用方式
 */
@Data
public class PageDTO<Q> {

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数必须大于0")
    private Integer pageSize = 10;

    /**
     * 查询条件对象，可为 null
     * 当使用继承方式时，此字段通常为 null，查询条件直接作为子类字段
     */
    private Q params;

    /**
     * 关键字搜索（兼容旧版本）
     */
    private String keyword;

    /**
     * 转换为 MyBatis-Plus 的分页对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(this.pageNum, this.pageSize);
    }

    // 兼容旧版本的字段名
    public Integer getPage() {
        return this.pageNum;
    }

    public void setPage(Integer page) {
        this.pageNum = page;
    }
}
