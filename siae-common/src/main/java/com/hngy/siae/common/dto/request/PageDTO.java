package com.hngy.siae.common.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用分页请求参数（可携带查询参数）
 * @param <Q> 查询条件参数的类型
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
     */
    private Q params;

    /**
     * 转换为 MyBatis-Plus 的分页对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(this.pageNum, this.pageSize);
    }
}
