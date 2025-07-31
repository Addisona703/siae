package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项类型查询数据传输对象
 * <p>
 * 用于奖项类型分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
 *
 * @author KEYKB
 */
@Data
public class AwardTypeQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 奖项类型ID
     */
    private Long id;

    /**
     * 奖项类型名称（支持模糊查询）
     */
    private String name;
}
